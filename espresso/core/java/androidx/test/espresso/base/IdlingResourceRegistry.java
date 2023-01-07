/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.base;

import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static kotlin.collections.CollectionsKt.listOf;
import static kotlin.collections.CollectionsKt.mutableListOf;
import static kotlin.collections.CollectionsKt.toMutableList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingPolicy;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.IdlingResource.ResourceCallback;
import androidx.test.espresso.util.TracingUtil;
import androidx.test.platform.tracing.Tracer;
import androidx.test.platform.tracing.Tracer.Span;
import androidx.test.platform.tracing.Tracing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.inject.Inject;
import javax.inject.Singleton;
import kotlin.collections.CollectionsKt;

/**
 * Keeps track of user-registered {@link IdlingResource IdlingResources}. Consider using {@link
 * androidx.test.espresso.IdlingRegistry} instead of this class.
 */
@Singleton
public final class IdlingResourceRegistry {
  private static final String TAG = IdlingResourceRegistry.class.getSimpleName();

  private static final int DYNAMIC_RESOURCE_HAS_IDLED = 1;
  private static final int TIMEOUT_OCCURRED = 2;
  private static final int IDLE_WARNING_REACHED = 3;
  private static final int POSSIBLE_RACE_CONDITION_DETECTED = 4;
  private static final Object TIMEOUT_MESSAGE_TAG = new Object();

  private static final IdleNotificationCallback NO_OP_CALLBACK =
      new IdleNotificationCallback() {

        @Override
        public void allResourcesIdle() {}

        @Override
        public void resourcesStillBusyWarning(List<String> busys) {}

        @Override
        public void resourcesHaveTimedOut(List<String> busys) {}
      };

  // IdlingStates should only be accessed on main thread
  private final List<IdlingState> idlingStates = new ArrayList<>();
  private final Looper looper;
  private final Handler handler;
  private final Dispatcher dispatcher;
  private final Tracing tracer;
  private IdleNotificationCallback idleNotificationCallback = NO_OP_CALLBACK;

  @Inject
  IdlingResourceRegistry(Looper looper, @NonNull Tracing tracer) {
    this.looper = looper;
    this.tracer = tracer;
    this.dispatcher = new Dispatcher();
    this.handler = new Handler(looper, dispatcher);
  }

  public IdlingResourceRegistry(Looper looper) {
    this(looper, Tracing.getInstance());
  }

  /**
   * Ensures that this idling resource registry is in sync with given resources by
   * registering/un-registering idling resources as needed.
   */
  public void sync(final Iterable<IdlingResource> resources, final Iterable<Looper> loopers) {
    if (Looper.myLooper() != looper) {
      runSynchronouslyOnMainThread(
          new Callable<Void>() {
            @Override
            public Void call() {
              sync(resources, loopers);
              return null;
            }
          });
    } else {
      Map<String, IdlingResource> resourcesToRegister = new HashMap<>();

      // Add everything from resources
      for (IdlingResource resource : resources) {
        if (resourcesToRegister.containsKey(resource.getName())) {
          logDuplicateRegistrationError(resource, resourcesToRegister.get(resource.getName()));
        } else {
          resourcesToRegister.put(resource.getName(), resource);
        }
      }

      // Convert all Loopers into IdlingResources and add them to the list of resourcesToRegister
      // in order for them to be considered part of the syncing logic.
      for (Looper looper : loopers) {
        IdlingResource resource = LooperIdlingResourceInterrogationHandler.forLooper(looper);
        if (resourcesToRegister.containsKey(resource.getName())) {
          logDuplicateRegistrationError(resource, resourcesToRegister.get(resource.getName()));
        } else {
          resourcesToRegister.put(resource.getName(), resource);
        }
      }

      // Loop through existing resources and figure out which resources should be unregistered.
      // At the same time figure which resources are already registered and shouldn't be attempted
      // to register again.
      List<IdlingResource> resourcesToUnRegister = new ArrayList<>();
      for (IdlingState oldState : idlingStates) {
        IdlingResource ir = resourcesToRegister.remove(oldState.resource.getName());
        if (null == ir) {
          resourcesToUnRegister.add(oldState.resource);
        } else if (oldState.resource != ir) {
          // Same name but NOT the same instance, un-register the current one
          // and register the new one
          resourcesToUnRegister.add(oldState.resource);
          resourcesToRegister.put(ir.getName(), ir);
        }
      }

      unregisterResources(resourcesToUnRegister);
      registerResources(toMutableList(resourcesToRegister.values()));
    }
  }

  /**
   * Registers the given resources. If any of the given resources are already registered, a warning
   * is logged.
   *
   * @return {@code true} if all resources were successfully registered
   */
  public boolean registerResources(final List<? extends IdlingResource> resourceList) {
    if (Looper.myLooper() != looper) {
      return runSynchronouslyOnMainThread(
          new Callable<Boolean>() {
            @Override
            public Boolean call() {
              return registerResources(resourceList);
            }
          });
    } else {
      boolean allRegisteredSuccessfully = true;
      for (IdlingResource resource : resourceList) {
        checkNotNull(resource.getName(), "IdlingResource.getName() should not be null");

        boolean duplicate = false;
        for (IdlingState oldState : idlingStates) {
          if (resource.getName().equals(oldState.resource.getName())) {
            // This does not throw an error to avoid leaving tests that register resource in test
            // setup in an undeterministic state (we cannot assume that everyone clears vm state
            // between each test run)
            logDuplicateRegistrationError(resource, oldState.resource);
            duplicate = true;
            break;
          }
        }

        if (!duplicate) {
          IdlingState is = new IdlingState(resource, handler);
          idlingStates.add(is);
          is.registerSelf();
        } else {
          allRegisteredSuccessfully = false;
        }
      }
      return allRegisteredSuccessfully;
    }
  }

  /**
   * Unregisters the given resources. If any of the given resources are not already registered, a
   * warning is logged.
   *
   * @return {@code true} if all resources were successfully unregistered
   */
  public boolean unregisterResources(final List<? extends IdlingResource> resourceList) {
    if (Looper.myLooper() != looper) {
      return runSynchronouslyOnMainThread(
          new Callable<Boolean>() {
            @Override
            public Boolean call() {
              return unregisterResources(resourceList);
            }
          });
    } else {
      boolean allUnregisteredSuccessfully = true;
      for (IdlingResource resource : resourceList) {
        boolean found = false;
        for (int i = 0; i < idlingStates.size(); i++) {
          if (idlingStates.get(i).resource.getName().equals(resource.getName())) {
            idlingStates.get(i).closeSpan();
            idlingStates.remove(i);
            found = true;
            break;
          }
        }

        if (!found) {
          allUnregisteredSuccessfully = false;
          Log.e(
              TAG,
              String.format(
                  Locale.ROOT,
                  "Attempted to unregister resource that is not registered: "
                      + "'%s'. Resource list: %s",
                  resource.getName(),
                  getResources()));
        }
      }
      return allUnregisteredSuccessfully;
    }
  }

  public void registerLooper(Looper looper, boolean considerWaitIdle) {
    checkNotNull(looper);
    checkArgument(Looper.getMainLooper() != looper, "Not intended for use with main looper!");

    registerResources(listOf(LooperIdlingResourceInterrogationHandler.forLooper(looper)));
  }

  /**
   * Returns a list of all currently registered {@link IdlingResource}s. This method is safe to call
   * from any thread.
   *
   * @return an immutable List of {@link IdlingResource}s.
   */
  public List<IdlingResource> getResources() {
    if (Looper.myLooper() != looper) {
      return runSynchronouslyOnMainThread(
          new Callable<List<IdlingResource>>() {
            @Override
            public List<IdlingResource> call() {
              return getResources();
            }
          });
    } else {
      List<IdlingResource> irs = mutableListOf();
      for (IdlingState is : idlingStates) {
        irs.add(is.resource);
      }
      return CollectionsKt.toList(irs);
    }
  }

  boolean allResourcesAreIdle() {
    checkState(Looper.myLooper() == looper);
    for (IdlingState is : idlingStates) {
      if (is.idle) {
        // ensure resource has not gone busy.
        is.setIdle(is.resource.isIdleNow());
      }
      // TODO(b/214584779): We should not return early here as this call also has the side effect of
      // updating all the current idle states so we may miss a chance to notice another resource
      // has gone busy and start a tracing span for it.
      if (!is.idle) {
        return false;
      }
    }
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "All idling resources are idle.");
    }
    return true;
  }

  interface IdleNotificationCallback {
    public void allResourcesIdle();

    public void resourcesStillBusyWarning(List<String> busyResourceNames);

    public void resourcesHaveTimedOut(List<String> busyResourceNames);
  }

  void notifyWhenAllResourcesAreIdle(IdleNotificationCallback callback) {
    checkNotNull(callback);
    checkState(Looper.myLooper() == looper);
    checkState(idleNotificationCallback == NO_OP_CALLBACK, "Callback has already been registered.");
    if (allResourcesAreIdle()) {
      callback.allResourcesIdle();
    } else {
      idleNotificationCallback = callback;
      scheduleTimeoutMessages();
    }
  }

  IdleNotifier<IdleNotificationCallback> asIdleNotifier() {
    return new IdleNotifier<IdleNotificationCallback>() {
      @Override
      public boolean isIdleNow() {
        return allResourcesAreIdle();
      }

      @Override
      public void cancelCallback() {
        cancelIdleMonitor();
      }

      @Override
      public void registerNotificationCallback(IdleNotificationCallback cb) {
        notifyWhenAllResourcesAreIdle(cb);
      }
    };
  }

  void cancelIdleMonitor() {
    dispatcher.deregister();
  }

  private <T> T runSynchronouslyOnMainThread(Callable<T> task) {
    FutureTask<T> futureTask = new FutureTask<T>(task);
    handler.post(futureTask);

    try {
      return futureTask.get();
    } catch (CancellationException ce) {
      throw new RuntimeException(ce);
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }

  private void scheduleTimeoutMessages() {
    IdlingPolicy warning = IdlingPolicies.getDynamicIdlingResourceWarningPolicy();
    Message timeoutWarning = handler.obtainMessage(IDLE_WARNING_REACHED, TIMEOUT_MESSAGE_TAG);
    handler.sendMessageDelayed(
        timeoutWarning, warning.getIdleTimeoutUnit().toMillis(warning.getIdleTimeout()));
    Message timeoutError = handler.obtainMessage(TIMEOUT_OCCURRED, TIMEOUT_MESSAGE_TAG);
    IdlingPolicy error = IdlingPolicies.getDynamicIdlingResourceErrorPolicy();

    handler.sendMessageDelayed(
        timeoutError, error.getIdleTimeoutUnit().toMillis(error.getIdleTimeout()));
  }

  List<String> getBusyResources() {
    List<String> busyResourceNames = mutableListOf();
    List<IdlingState> racyResources = mutableListOf();

    for (IdlingState state : idlingStates) {
      if (!state.idle) {
        if (state.resource.isIdleNow()) {
          // We have not been notified of a BUSY -> IDLE transition, but the resource is telling us
          // its that its idle. Either it's a race condition or is this resource buggy.
          racyResources.add(state);
        } else {
          busyResourceNames.add(state.resource.getName());
        }
      }
    }

    if (!racyResources.isEmpty()) {
      Message raceBuster =
          handler.obtainMessage(POSSIBLE_RACE_CONDITION_DETECTED, TIMEOUT_MESSAGE_TAG);
      raceBuster.obj = racyResources;
      handler.sendMessage(raceBuster);
      return null;
    } else {
      return busyResourceNames;
    }
  }

  private class IdlingState implements ResourceCallback {
    // on main
    final IdlingResource resource;
    // from anywhere
    final Handler handler;
    // on main
    private boolean idle;
    // on main
    Tracer.Span tracerSpan;

    private IdlingState(IdlingResource resource, Handler handler) {
      this.resource = resource;
      this.handler = handler;
    }

    private void registerSelf() {
      // on main, once at initialization.
      resource.registerIdleTransitionCallback(this);
      setIdle(resource.isIdleNow());
    }

    private void closeSpan() {
      // Resource is being unregistered. End the tracing span.
      if (tracerSpan != null) {
        tracerSpan.close();
        tracerSpan = null;
        if (!idle) {
          Log.w(TAG, "Closing span for resource not idle: " + resource.getName());
        }
      }
    }

    /** Must be invoked from main thread. */
    public void setIdle(boolean idle) {
      if (!idle && tracerSpan == null) {
        // Resource is busy. Start a tracing span if we haven't done so yet.
        tracerSpan =
            createUnmanagedTracerSpan(TracingUtil.getSpanName("IdleResource", resource.getName()));
      } else if (idle && tracerSpan != null) {
        // Resource is no longer busy. End any current tracing span.
        tracerSpan.close();
        tracerSpan = null;
      }

      this.idle = idle;
    }

    /**
     * Internal method to create a Span that does not enforce try-resource usage. Caller
     * <em>must</em> manually call {@link Span#close()} later on the resource.
     */
    @SuppressWarnings("MustBeClosedChecker")
    private Span createUnmanagedTracerSpan(String name) {
      return tracer.beginSpan(name);
    }

    @Override
    public void onTransitionToIdle() {
      // from app code - unknown thread
      Message m = handler.obtainMessage(DYNAMIC_RESOURCE_HAS_IDLED);
      m.obj = this;
      handler.sendMessage(m);
    }
  }

  private class Dispatcher implements Handler.Callback {
    @Override
    public boolean handleMessage(Message m) {
      switch (m.what) {
        case DYNAMIC_RESOURCE_HAS_IDLED:
          handleResourceIdled(m);
          break;
        case IDLE_WARNING_REACHED:
          handleTimeoutWarning();
          break;
        case TIMEOUT_OCCURRED:
          handleTimeout();
          break;
        case POSSIBLE_RACE_CONDITION_DETECTED:
          handleRaceCondition(m);
          break;
        default:
          Log.w(TAG, "Unknown message type: " + m);
          return false;
      }
      return true;
    }

    private void handleResourceIdled(Message m) {
      IdlingState is = (IdlingState) m.obj;
      is.setIdle(true);
      boolean unknownResource = true;
      boolean allIdle = true;
      for (IdlingState state : idlingStates) {
        allIdle = allIdle && state.idle;
        if (!unknownResource && !allIdle) {
          // we've made sure that we are actually monitoring this resource - and we've encountered
          // a different resource that is currently not idle. Lets stop checking the others.
          break;
        }
        if (unknownResource && state == is) {
          unknownResource = false;
        }
      }
      if (unknownResource) {
        Log.i(TAG, "Ignoring message from unregistered resource: " + is.resource);
        return;
      }
      if (allIdle) {
        try {
          idleNotificationCallback.allResourcesIdle();
        } finally {
          deregister();
        }
      }
    }

    private void handleTimeoutWarning() {
      List<String> busyResources = getBusyResources();
      if (busyResources == null) {
        // null indicates that there is either a race or a programming error
        // a race detector message has been inserted into the q.
        // reinsert the idle_warning_reached message into the q directly after it
        // so we generate warnings if the system is still sane.
        handler.sendMessage(handler.obtainMessage(IDLE_WARNING_REACHED, TIMEOUT_MESSAGE_TAG));
      } else {
        IdlingPolicy warning = IdlingPolicies.getDynamicIdlingResourceWarningPolicy();
        idleNotificationCallback.resourcesStillBusyWarning(busyResources);
        handler.sendMessageDelayed(
            handler.obtainMessage(IDLE_WARNING_REACHED, TIMEOUT_MESSAGE_TAG),
            warning.getIdleTimeoutUnit().toMillis(warning.getIdleTimeout()));
      }
    }

    private void handleTimeout() {
      List<String> busyResources = getBusyResources();
      if (busyResources == null) {
        // detected a possible race... we've enqueued a race busting message
        // so either that'll resolve the race or kill the app because it's buggy.
        // if the race resolves, we need to timeout properly.
        handler.sendMessage(handler.obtainMessage(TIMEOUT_OCCURRED, TIMEOUT_MESSAGE_TAG));
      } else {
        try {
          idleNotificationCallback.resourcesHaveTimedOut(busyResources);
        } finally {
          deregister();
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void handleRaceCondition(Message m) {
      for (IdlingState is : (List<IdlingState>) m.obj) {

        if (is.idle) {
          // it was a race... i is now idle, everything is fine...
        } else {
          throw new IllegalStateException(
              String.format(
                  Locale.ROOT,
                  "Resource %s isIdleNow() is returning true, but a message indicating that the "
                      + "resource has transitioned from busy to idle was never sent.",
                  is.resource.getName()));
        }
      }
    }

    private void deregister() {
      handler.removeCallbacksAndMessages(TIMEOUT_MESSAGE_TAG);
      idleNotificationCallback = NO_OP_CALLBACK;
    }
  }

  private void logDuplicateRegistrationError(
      IdlingResource newResource, IdlingResource oldResource) {
    Log.e(
        TAG,
        String.format(
            Locale.ROOT,
            "Attempted to register resource with same names:"
                + " %s. R1: %s R2: %s.\nDuplicate resource registration will be ignored.",
            newResource.getName(),
            newResource,
            oldResource));
  }
}
