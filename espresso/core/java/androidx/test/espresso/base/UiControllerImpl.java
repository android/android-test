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

import static androidx.test.espresso.util.Throwables.throwIfUnchecked;
import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static kotlin.collections.CollectionsKt.mutableListOf;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingPolicy;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.espresso.util.StringJoinerKt;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import kotlin.collections.CollectionsKt;

/** Implementation of {@link UiController}. */
@Singleton
final class UiControllerImpl
    implements InterruptableUiController, Handler.Callback, IdlingUiController {

  private static final String TAG = UiControllerImpl.class.getSimpleName();

  private static final Callable<Void> NO_OP =
      new Callable<Void>() {
        @Override
        public Void call() {
          return null;
        }
      };

  /** Responsible for signaling a particular condition is met / verifying that signal. */
  enum IdleCondition {
    DELAY_HAS_PAST,
    ASYNC_TASKS_HAVE_IDLED,
    COMPAT_TASKS_HAVE_IDLED,
    KEY_INJECT_HAS_COMPLETED,
    MOTION_INJECTION_HAS_COMPLETED,
    DYNAMIC_TASKS_HAVE_IDLED;

    /** Checks whether this condition has been signaled. */
    public boolean isSignaled(BitSet conditionSet) {
      return conditionSet.get(ordinal());
    }

    /** Resets the signal state for this condition. */
    public void reset(BitSet conditionSet) {
      conditionSet.set(ordinal(), false);
    }

    /** Creates a message that when sent will raise the signal of this condition. */
    public Message createSignal(Handler handler, int myGeneration) {
      return Message.obtain(handler, ordinal(), myGeneration, 0, null);
    }

    /**
     * Handles a message that is raising a signal and updates the condition set accordingly.
     * Messages from a previous generation will be ignored.
     */
    public static boolean handleMessage(
        Message message, BitSet conditionSet, int currentGeneration) {
      IdleCondition[] allConditions = values();
      if (message.what < 0 || message.what >= allConditions.length) {
        return false;
      } else {
        IdleCondition condition = allConditions[message.what];
        if (message.arg1 == currentGeneration) {
          condition.signal(conditionSet);
        } else {
          Log.w(
              TAG,
              "ignoring signal of: "
                  + condition
                  + " from previous generation: "
                  + message.arg1
                  + " current generation: "
                  + currentGeneration);
        }
        return true;
      }
    }

    public static BitSet createConditionSet() {
      return new BitSet(values().length);
    }

    /**
     * Requests that the given bitset be updated to indicate that this condition has been signaled.
     */
    protected void signal(BitSet conditionSet) {
      conditionSet.set(ordinal());
    }
  }

  /** Represents the status of {@link MainThreadInterrogation} */
  private enum InterrogationStatus {
    TIMED_OUT,
    COMPLETED,
    INTERRUPTED
  }

  private final EventInjector eventInjector;
  private final BitSet conditionSet;

  private final ExecutorService keyEventExecutor =
      Executors.newSingleThreadExecutor(
          new ThreadFactoryBuilder().setNameFormat("Espresso Key Event #%d").build());
  private final Looper mainLooper;
  private final IdlingResourceRegistry idlingResourceRegistry;
  private final Handler controllerHandler;

  // only updated on main thread.
  private MainThreadInterrogation interrogation;
  private int generation = 0;
  private IdleNotifier<Runnable> asyncIdle;
  private IdleNotifier<Runnable> compatIdle;
  private Provider<IdleNotifier<IdleNotificationCallback>> dynamicIdleProvider;

  @VisibleForTesting
  @Inject
  UiControllerImpl(
      EventInjector eventInjector,
      @SdkAsyncTask IdleNotifier<Runnable> asyncIdle,
      @CompatAsyncTask IdleNotifier<Runnable> compatIdle,
      Provider<IdleNotifier<IdleNotificationCallback>> dynamicIdle,
      Looper mainLooper,
      IdlingResourceRegistry idlingResourceRegistry) {
    this.eventInjector = checkNotNull(eventInjector);
    this.asyncIdle = checkNotNull(asyncIdle);
    this.compatIdle = checkNotNull(compatIdle);
    this.conditionSet = IdleCondition.createConditionSet();
    this.dynamicIdleProvider = checkNotNull(dynamicIdle);
    this.mainLooper = checkNotNull(mainLooper);
    this.idlingResourceRegistry = checkNotNull(idlingResourceRegistry);
    controllerHandler = new Handler(mainLooper, this);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean injectKeyEvent(final KeyEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    loopMainThreadUntilIdle();

    FutureTask<Boolean> injectTask =
        new SignalingTask<Boolean>(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                return eventInjector.injectKeyEvent(event);
              }
            },
            IdleCondition.KEY_INJECT_HAS_COMPLETED,
            generation);

    // Inject the key event.
    @SuppressWarnings("unused") // go/futurereturn-lsc
    Future<?> possiblyIgnoredError = keyEventExecutor.submit(injectTask);

    loopUntil(IdleCondition.KEY_INJECT_HAS_COMPLETED, dynamicIdleProvider.get());

    try {
      checkState(injectTask.isDone(), "Key injection was signaled - but it wasnt done.");
      return injectTask.get();
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof InjectEventSecurityException) {
        throw (InjectEventSecurityException) ee.getCause();
      } else {
        throw new RuntimeException(ee.getCause());
      }
    } catch (InterruptedException neverHappens) {
      // we only call get() after done() is signaled.
      // we should never block.
      throw new RuntimeException("impossible.", neverHappens);
    }
  }

  @Override
  public boolean injectMotionEvent(final MotionEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    FutureTask<Boolean> injectTask =
        new SignalingTask<Boolean>(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                return eventInjector.injectMotionEvent(event);
              }
            },
            IdleCondition.MOTION_INJECTION_HAS_COMPLETED,
            generation);
    Future<?> possiblyIgnoredError = keyEventExecutor.submit(injectTask);
    loopUntil(IdleCondition.MOTION_INJECTION_HAS_COMPLETED, dynamicIdleProvider.get());
    try {
      checkState(injectTask.isDone(), "Motion event injection was signaled - but it wasnt done.");
      return injectTask.get();
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof InjectEventSecurityException) {
        throw (InjectEventSecurityException) ee.getCause();
      } else {
        throwIfUnchecked(ee.getCause() != null ? ee.getCause() : ee);
        throw new RuntimeException(ee.getCause() != null ? ee.getCause() : ee);
      }
    } catch (InterruptedException neverHappens) {
      // we only call get() after done() is signaled.
      // we should never block.
      throw new RuntimeException(neverHappens);
    } finally {
      loopMainThreadUntilIdle();
    }
  }

  @Override
  public boolean injectMotionEventSequence(final Iterable<MotionEvent> events)
      throws InjectEventSecurityException {
    checkNotNull(events);
    checkState(events.iterator().hasNext(), "Expecting non-empty events to inject");
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    final Iterator<MotionEvent> mei = events.iterator();
    final long downTime = CollectionsKt.first(events).getEventTime();
    final long shift = SystemClock.uptimeMillis() - downTime;
    FutureTask<Boolean> injectTask =
        new SignalingTask<>(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                boolean success = true;
                while (mei.hasNext()) {
                  MotionEvent me = mei.next();
                  long desiredTime = me.getEventTime() + shift;
                  long timeUntilDesired = desiredTime - SystemClock.uptimeMillis();
                  if (timeUntilDesired > 10) {
                    // This must NOT run in main thread, so it's fine to sleep
                    SystemClock.sleep(timeUntilDesired);
                  }
                  if (mei.hasNext()) {
                    success &= eventInjector.injectMotionEventAsync(me);
                  } else {
                    success &= eventInjector.injectMotionEvent(me);
                  }
                }
                return success;
              }
            },
            IdleCondition.MOTION_INJECTION_HAS_COMPLETED,
            generation);
    Future<?> possiblyIgnoredError = keyEventExecutor.submit(injectTask);
    loopUntil(IdleCondition.MOTION_INJECTION_HAS_COMPLETED, dynamicIdleProvider.get());
    try {
      checkState(injectTask.isDone(), "MotionEvents injection was signaled - but it wasnt done.");
      return injectTask.get();
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof InjectEventSecurityException) {
        throw (InjectEventSecurityException) ee.getCause();
      } else {
        throwIfUnchecked(ee.getCause() != null ? ee.getCause() : ee);
        throw new RuntimeException(ee.getCause() != null ? ee.getCause() : ee);
      }
    } catch (InterruptedException neverHappens) {
      // we only call get() after done() is signaled.
      // we should never block.
      throw new RuntimeException(neverHappens);
    } finally {
      loopMainThreadUntilIdle();
    }
  }

  @Override
  public boolean injectString(String str) throws InjectEventSecurityException {
    checkNotNull(str);
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");

    // No-op if string is empty.
    if (str.isEmpty()) {
      Log.w(TAG, "Supplied string is empty resulting in no-op (nothing is typed).");
      return true;
    }

    boolean eventInjected = false;
    KeyCharacterMap keyCharacterMap = getKeyCharacterMap();

    // TODO(b/80130875): Investigate why not use (as suggested in javadoc of
    // keyCharacterMap.getEvents):
    // http://developer.android.com/reference/android/view/KeyEvent.html#KeyEvent(long,
    // java.lang.String, int, int)
    KeyEvent[] events = keyCharacterMap.getEvents(str.toCharArray());
    if (events == null) {
      throw new RuntimeException(
          String.format(
              Locale.ROOT,
              "Failed to get key events for string %s (i.e. current IME does not understand how to"
                  + " translate the string into key events). As a workaround, you can use"
                  + " replaceText action to set the text directly in the EditText field.",
              str));
    }

    Log.d(TAG, String.format(Locale.ROOT, "Injecting string: \"%s\"", str));

    for (KeyEvent event : events) {
      checkNotNull(
          event,
          String.format(
              Locale.ROOT,
              "Failed to get event for character (%c) with key code (%s)",
              event.getKeyCode(),
              event.getUnicodeChar()));

      eventInjected = false;
      for (int attempts = 0; !eventInjected && attempts < 4; attempts++) {
        // We have to change the time of an event before injecting it because
        // all KeyEvents returned by KeyCharacterMap.getEvents() have the same
        // time stamp and the system rejects too old events. Hence, it is
        // possible for an event to become stale before it is injected if it
        // takes too long to inject the preceding ones.
        event = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 0);
        eventInjected = injectKeyEvent(event);
      }

      if (!eventInjected) {
        Log.e(
            TAG,
            String.format(
                Locale.ROOT,
                "Failed to inject event for character (%c) with key code (%s)",
                event.getUnicodeChar(),
                event.getKeyCode()));
        break;
      }
    }

    return eventInjected;
  }

  @SuppressLint("InlinedApi")
  @VisibleForTesting
  @SuppressWarnings("deprecation")
  public static KeyCharacterMap getKeyCharacterMap() {
    KeyCharacterMap keyCharacterMap = null;

    // KeyCharacterMap.VIRTUAL_KEYBOARD is present from API11.
    // For earlier APIs we use KeyCharacterMap.BUILT_IN_KEYBOARD
    if (Build.VERSION.SDK_INT < 11) {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
    } else {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    }
    return keyCharacterMap;
  }

  @Override
  public IdlingResourceRegistry getIdlingResourceRegistry() {
    return idlingResourceRegistry;
  }

  @Override
  public void loopMainThreadUntilIdle() {
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    IdleNotifier<IdleNotificationCallback> dynamicIdle = dynamicIdleProvider.get();
    do {
      EnumSet<IdleCondition> condChecks = EnumSet.noneOf(IdleCondition.class);
      if (!asyncIdle.isIdleNow()) {
        asyncIdle.registerNotificationCallback(
            new SignalingTask<Void>(NO_OP, IdleCondition.ASYNC_TASKS_HAVE_IDLED, generation));

        condChecks.add(IdleCondition.ASYNC_TASKS_HAVE_IDLED);
      }

      if (!compatIdle.isIdleNow()) {
        compatIdle.registerNotificationCallback(
            new SignalingTask<Void>(NO_OP, IdleCondition.COMPAT_TASKS_HAVE_IDLED, generation));
        condChecks.add(IdleCondition.COMPAT_TASKS_HAVE_IDLED);
      }

      if (!dynamicIdle.isIdleNow()) {
        final IdlingPolicy warning = IdlingPolicies.getDynamicIdlingResourceWarningPolicy();
        final IdlingPolicy error = IdlingPolicies.getDynamicIdlingResourceErrorPolicy();
        final SignalingTask<Void> idleSignal =
            new SignalingTask<Void>(NO_OP, IdleCondition.DYNAMIC_TASKS_HAVE_IDLED, generation);
        dynamicIdle.registerNotificationCallback(
            new IdleNotificationCallback() {
              @Override
              public void resourcesStillBusyWarning(List<String> busyResourceNames) {
                warning.handleTimeout(busyResourceNames, "IdlingResources are still busy!");
              }

              @Override
              public void resourcesHaveTimedOut(List<String> busyResourceNames) {
                error.handleTimeout(busyResourceNames, "IdlingResources have timed out!");
                controllerHandler.post(idleSignal);
              }

              @Override
              public void allResourcesIdle() {
                controllerHandler.post(idleSignal);
              }
            });
        condChecks.add(IdleCondition.DYNAMIC_TASKS_HAVE_IDLED);
      }

      try {
        dynamicIdle = loopUntil(condChecks, dynamicIdle);
      } finally {
        asyncIdle.cancelCallback();
        compatIdle.cancelCallback();
        dynamicIdle.cancelCallback();
      }
    } while (!asyncIdle.isIdleNow() || !compatIdle.isIdleNow() || !dynamicIdle.isIdleNow());
  }

  @Override
  public void loopMainThreadForAtLeast(long millisDelay) {
    checkState(Looper.myLooper() == mainLooper, "Expecting to be on main thread!");
    checkState(!IdleCondition.DELAY_HAS_PAST.isSignaled(conditionSet), "recursion detected!");
    checkArgument(millisDelay > 0);

    controllerHandler.postAtTime(
        new SignalingTask<>(NO_OP, IdleCondition.DELAY_HAS_PAST, generation),
        generation,
        SystemClock.uptimeMillis() + millisDelay);
    loopUntil(IdleCondition.DELAY_HAS_PAST, dynamicIdleProvider.get());
    loopMainThreadUntilIdle();
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (!IdleCondition.handleMessage(msg, conditionSet, generation)) {
      Log.i(TAG, "Unknown message type: " + msg);
      return false;
    } else {
      return true;
    }
  }

  private void loopUntil(
      IdleCondition condition, IdleNotifier<IdleNotificationCallback> dynamicIdle) {
    loopUntil(EnumSet.of(condition), dynamicIdle);
  }

  /**
   * Loops the main thread until all IdleConditions have been signaled.
   *
   * <p>Once they've been signaled, the conditions are reset and the generation value is
   * incremented.
   *
   * <p>Signals should only be raised through SignalingTask instances, and care should be taken to
   * ensure that the signaling task is created before loopUntil is called.
   *
   * <p>Good:
   *
   * <pre>{@code
   * idlingType.runOnIdle(new SignalingTask(NO_OP, IdleCondition.MY_IDLE_CONDITION, generation));
   * loopUntil(IdleCondition.MY_IDLE_CONDITION);
   * }</pre>
   *
   * <p>Bad:
   *
   * <pre>{@code
   * idlingType.runOnIdle(new CustomCallback() {
   *   @Override public void itsDone() {
   *     // oh no - The creation of this signaling task is delayed until this method is
   *     // called, so it will not have the right value for generation.
   *     new SignalingTask(NO_OP, IdleCondition.MY_IDLE_CONDITION, generation).run();
   *     }
   *   })
   *   loopUntil(IdleCondition.MY_IDLE_CONDITION);
   * }</pre>
   */
  private IdleNotifier<IdleNotificationCallback> loopUntil(
      EnumSet<IdleCondition> conditions, IdleNotifier<IdleNotificationCallback> dynamicIdle) {
    IdlingPolicy masterIdlePolicy = IdlingPolicies.getMasterIdlingPolicy();
    IdlingPolicy dynamicIdlePolicy = IdlingPolicies.getDynamicIdlingResourceErrorPolicy();
    try {
      long start = SystemClock.uptimeMillis();
      long end =
          start + masterIdlePolicy.getIdleTimeoutUnit().toMillis(masterIdlePolicy.getIdleTimeout());
      interrogation = new MainThreadInterrogation(conditions, conditionSet, end);

      InterrogationStatus result = Interrogator.loopAndInterrogate(interrogation);
      if (InterrogationStatus.COMPLETED == result) {
        // did not time out, all conditions happy.
        return dynamicIdle;
      } else if (InterrogationStatus.INTERRUPTED == result) {
        Log.w(TAG, "Espresso interrogation of the main thread is interrupted");
        throw new RuntimeException("Espresso interrogation of the main thread is interrupted");
      }

      // timed out... what went wrong?
      List<String> idleConditions = mutableListOf();
      for (IdleCondition condition : conditions) {
        if (!condition.isSignaled(conditionSet)) {
          String conditionName = condition.name();
          switch (condition) {
            case ASYNC_TASKS_HAVE_IDLED:
              if (masterIdlePolicy.getDisableOnTimeout()
                  || (!masterIdlePolicy.getTimeoutIfDebuggerAttached()
                      && Debug.isDebuggerConnected())) {
                asyncIdle.cancelCallback();
                asyncIdle = new NoopRunnableIdleNotifier();
              }
              break;
            case COMPAT_TASKS_HAVE_IDLED:
              if (masterIdlePolicy.getDisableOnTimeout()
                  || (!masterIdlePolicy.getTimeoutIfDebuggerAttached()
                      && Debug.isDebuggerConnected())) {
                compatIdle.cancelCallback();
                compatIdle = new NoopRunnableIdleNotifier();
              }
              break;
            case DYNAMIC_TASKS_HAVE_IDLED:
              if (dynamicIdlePolicy.getDisableOnTimeout()
                  || (!masterIdlePolicy.getTimeoutIfDebuggerAttached()
                      && Debug.isDebuggerConnected())) {
                dynamicIdle.cancelCallback();
                dynamicIdleProvider = new NoopIdleNotificationCallbackIdleNotifierProvider();
                dynamicIdle = dynamicIdleProvider.get();
              }

              List<String> busyResources = idlingResourceRegistry.getBusyResources();
              conditionName =
                  String.format(
                      Locale.ROOT,
                      "%s(busy resources=%s)",
                      conditionName,
                      StringJoinerKt.joinToString(busyResources, ","));
              break;
            default:
              break;
          }
          idleConditions.add(conditionName);
        }
      }

      if (idleConditions.isEmpty()) {
        // Formatted to look consistent with other idling conditions.
        idleConditions.add(
            "MAIN_LOOPER_HAS_IDLED(last message: " + interrogation.getMessage() + ")");
      }
      masterIdlePolicy.handleTimeout(
          idleConditions,
          String.format(
              Locale.ROOT,
              "Looped for %s iterations over %s %s.",
              interrogation.execCount,
              masterIdlePolicy.getIdleTimeout(),
              masterIdlePolicy.getIdleTimeoutUnit().name()));
    } finally {
      generation++;
      for (IdleCondition condition : conditions) {
        condition.reset(conditionSet);
      }
      interrogation = null;
    }
    return dynamicIdle;
  }

  @Override
  public void interruptEspressoTasks() {
    controllerHandler.post(
        new Runnable() {
          @Override
          public void run() {
            if (interrogation != null) {
              interrogation.interruptInterrogation();
              controllerHandler.removeCallbacksAndMessages(generation);
            }
          }
        });
  }

  private static final class MainThreadInterrogation
      implements Interrogator.InterrogationHandler<InterrogationStatus> {
    private final EnumSet<IdleCondition> conditions;
    private final BitSet conditionSet;
    private final long giveUpAtMs;
    private String lastMessage;

    private InterrogationStatus status = InterrogationStatus.COMPLETED;
    private int execCount = 0;

    MainThreadInterrogation(
        EnumSet<IdleCondition> conditions, BitSet conditionSet, long giveUpAtMs) {
      this.conditions = conditions;
      this.conditionSet = conditionSet;
      this.giveUpAtMs = giveUpAtMs;
    }

    @Override
    public void setMessage(Message m) {
      try {
        lastMessage = m.toString();
      } catch (NullPointerException npe) {
        // toString can fail with an NPE on getClass()
        // This field is just for diagnosing Espresso test failures; suppress the error.
        lastMessage = "NPE calling message toString(): " + npe;
      }
    }

    @Override
    public String getMessage() {
      return lastMessage;
    }

    @Override
    public void quitting() {
      /* can not happen  */
    }

    @Override
    public boolean barrierUp() {
      return continueOrTimeout();
    }

    @Override
    public boolean queueEmpty() {
      if (conditionsMet()) {
        return false;
      }
      return true;
    }

    @Override
    public boolean taskDueSoon() {
      return continueOrTimeout();
    }

    @Override
    public boolean taskDueLong() {
      if (conditionsMet()) {
        return false;
      }
      return true;
    }

    @Override
    public boolean beforeTaskDispatch() {
      execCount++;
      return continueOrTimeout();
    }

    private boolean continueOrTimeout() {
      if (InterrogationStatus.INTERRUPTED == status) {
        return false;
      }
      if (SystemClock.uptimeMillis() >= giveUpAtMs) {
        status = InterrogationStatus.TIMED_OUT;
        return false;
      }
      return true;
    }

    void interruptInterrogation() {
      status = InterrogationStatus.INTERRUPTED;
    }

    @Override
    public InterrogationStatus get() {
      return status;
    }

    private boolean conditionsMet() {
      if (InterrogationStatus.INTERRUPTED == status) {
        return true; // we want to stop.
      }
      boolean conditionsMet = true;
      boolean shouldLogConditionState = execCount > 0 && execCount % 100 == 0;
      for (IdleCondition condition : conditions) {
        if (!condition.isSignaled(conditionSet)) {
          conditionsMet = false;
          if (shouldLogConditionState) {
            Log.w(TAG, "Waiting for: " + condition.name() + " for " + execCount + " iterations.");
          } else {
            break;
          }
        }
      }
      return conditionsMet;
    }
  }

  /**
   * Encapsulates posting a signal message to update the conditions set after a task has executed.
   */
  private class SignalingTask<T> extends FutureTask<T> {

    private final IdleCondition condition;
    private final int myGeneration;

    public SignalingTask(Callable<T> callable, IdleCondition condition, int myGeneration) {
      super(callable);
      this.condition = checkNotNull(condition);
      this.myGeneration = myGeneration;
    }

    @Override
    protected void done() {
      controllerHandler.sendMessage(condition.createSignal(controllerHandler, myGeneration));
    }
  }
}
