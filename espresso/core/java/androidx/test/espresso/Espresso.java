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

package androidx.test.espresso;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressMenuKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.annotation.CheckResult;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.base.IdlingResourceRegistry;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.internal.util.Checks;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.tracing.Tracer.Span;
import androidx.test.platform.tracing.Tracing;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckReturnValue;
import org.hamcrest.Matcher;

/**
 * Entry point to the Espresso framework. Test authors can initiate testing by using one of the on*
 * methods (e.g. onView) or perform top-level user actions (e.g. pressBack).
 */
public final class Espresso {

  private static final String TAG = Espresso.class.getSimpleName();
  private static final BaseLayerComponent BASE = GraphHolder.baseLayer();
  private static final IdlingResourceRegistry baseRegistry = BASE.idlingResourceRegistry();
  private static final Tracing tracer = BASE.tracer();
  private static final int TIMEOUT_SECONDS = 5;

  private Espresso() {}

  /**
   * Creates a {@link ViewInteraction} for a given view. Note: the view has to be part of the view
   * hierarchy. This may not be the case if it is rendered as part of an AdapterView (e.g.
   * ListView). If this is the case, use Espresso.onData to load the view first.
   *
   * <p>This method builds a ViewInteraction object - it does not interact with the application
   * under test at all. It is expected that the caller use the ViewInteraction object to perform an
   * action or assertion.
   *
   * @param viewMatcher used to select the view.
   * @see #onData(org.hamcrest.Matcher)
   */
  // TODO change parameter to type to Matcher<? extends View> which currently causes Dagger issues
  @CheckReturnValue
  @CheckResult
  public static ViewInteraction onView(final Matcher<View> viewMatcher) {
    return BASE.plus(new ViewInteractionModule(viewMatcher)).viewInteraction();
  }

  /**
   * Creates an {@link DataInteraction} for a data object displayed by the application. Use this
   * method to load (into the view hierarchy) items from AdapterView widgets (e.g. ListView).
   *
   * <p>This method builds a DataInteraction object - it does not interact with the application
   * under test at all. It is expected that the caller use the ViewInteraction object to perform an
   * action or assertion.
   *
   * @param dataMatcher a matcher used to find the data object.
   * @return a DataInteraction that will perform an action or assertion.
   */
  @CheckReturnValue
  @CheckResult
  public static DataInteraction onData(Matcher<? extends Object> dataMatcher) {
    return new DataInteraction(dataMatcher);
  }

  /**
   * Registers a Looper for idle checking with the framework. This is intended for use with non-UI
   * thread loopers.
   *
   * @throws IllegalArgumentException if looper is the main looper.
   * @deprecated use {@link IdlingRegistry#registerLooperAsIdlingResource(Looper)} instead.
   */
  @Deprecated
  public static void registerLooperAsIdlingResource(Looper looper) {
    registerLooperAsIdlingResource(looper, false);
  }

  /**
   * Registers a Looper for idle checking with the framework. This is intended for use with non-UI
   * thread loopers.
   *
   * <p>This method allows the caller to consider Thread.State.WAIT to be 'idle'.
   *
   * <p>This is useful in the case where a looper is sending a message to the UI thread
   * synchronously through a wait/notify mechanism.
   *
   * @throws IllegalArgumentException if looper is the main looper.
   * @deprecated use {@link IdlingRegistry#registerLooperAsIdlingResource(Looper)} instead.
   */
  @Deprecated
  public static void registerLooperAsIdlingResource(Looper looper, boolean considerWaitIdle) {
    IdlingRegistry.getInstance().registerLooperAsIdlingResource(looper);
    baseRegistry.sync(
        IdlingRegistry.getInstance().getResources(), IdlingRegistry.getInstance().getLoopers());
  }

  /**
   * Registers one or more {@link IdlingResource}s with the framework. It is expected, although not
   * strictly required, that this method will be called at test setup time prior to any interaction
   * with the application under test. When registering more than one resource, ensure that each has
   * a unique name. If any of the given resources is already registered, a warning is logged.
   *
   * @return {@code true} if all resources were successfully registered
   * @deprecated use {@link IdlingRegistry#register(IdlingResource...)} instead.
   */
  @Deprecated
  public static boolean registerIdlingResources(IdlingResource... resources) {
    if (IdlingRegistry.getInstance().register(resources)) {
      baseRegistry.sync(
          IdlingRegistry.getInstance().getResources(), IdlingRegistry.getInstance().getLoopers());
      return true;
    }
    return resources.length == 0;
  }

  /**
   * Unregisters one or more {@link IdlingResource}s. If any of the given resources are not already
   * registered, a warning is logged.
   *
   * @return {@code true} if all resources were successfully unregistered
   * @deprecated use {@link IdlingRegistry#unregister(IdlingResource...)} instead.
   */
  @Deprecated
  public static boolean unregisterIdlingResources(IdlingResource... resources) {
    if (IdlingRegistry.getInstance().unregister(resources)) {
      baseRegistry.sync(
          IdlingRegistry.getInstance().getResources(), IdlingRegistry.getInstance().getLoopers());
      return true;
    }
    return resources.length == 0;
  }

  /**
   * Returns a list of all currently registered {@link IdlingResource}s.
   *
   * @deprecated use {@link IdlingRegistry#getResources()} instead.
   */
  @Deprecated
  public static List<IdlingResource> getIdlingResources() {
    return baseRegistry.getResources();
  }

  /** Changes the default {@link FailureHandler} to the given one. */
  public static void setFailureHandler(FailureHandler failureHandler) {
    BASE.failureHolder().update(checkNotNull(failureHandler));
  }

  /**
   * ******************************** Top Level Actions *****************************************
   */

  // Ideally, this should be only allOf(isDisplayed(), withContentDescription("More options"))
  // But the AppCompatActivity compat lib is missing a content description for this element, so
  // we add the class name matcher as another option to find the view.
  private static final Matcher<View> OVERFLOW_BUTTON_MATCHER =
      anyOf(
          allOf(isDisplayed(), withContentDescription("More options")),
          allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton"))));

  /** Closes soft keyboard if open. */
  public static void closeSoftKeyboard() {
    try (Span ignored = tracer.beginSpan("Espresso-closeSoftKeyboard")) {
      onView(isRoot()).perform(ViewActions.closeSoftKeyboard());
    }
  }

  /**
   * Opens the overflow menu displayed in the contextual options of an ActionMode.
   *
   * <p>This works with both native and SherlockActionBar action modes.
   *
   * <p>Note the significant difference in UX between ActionMode and ActionBar overflows -
   * ActionMode will always present an overflow icon and that icon only responds to clicks. The menu
   * button (if present) has no impact on it.
   */
  public static void openContextualActionModeOverflowMenu() {
    try (Span ignored = tracer.beginSpan("Espresso-openContextualActionModeOverflowMenu")) {
      onView(isRoot()).perform(new TransitionBridgingViewAction());

      // provide an pressBack rollback action to the click, to handle occasional flakiness where the
      // click is interpreted as a long press
      onView(OVERFLOW_BUTTON_MATCHER).perform(click(ViewActions.pressBack()));
    }
  }

  /**
   * Press on the back button.
   *
   * @throws PerformException if currently displayed activity is root activity, since pressing back
   *     button would result in application closing.
   */
  public static void pressBack() {
    try (Span ignored = tracer.beginSpan("Espresso-pressBack")) {
      onView(isRoot()).perform(ViewActions.pressBack());
    }
  }

  /**
   * Similar to {@link #pressBack()} but will <b>not</b> throw an exception when Espresso navigates
   * outside the application or process under test.
   */
  public static void pressBackUnconditionally() {
    try (Span ignored = tracer.beginSpan("Espresso-pressBackUnconditionally")) {
      onView(isRoot()).perform(ViewActions.pressBackUnconditionally());
    }
  }

  /**
   * Opens the overflow menu displayed within an ActionBar.
   *
   * <p>This works with both native and SherlockActionBar ActionBars.
   *
   * <p>Note the significant differences of UX between ActionMode and ActionBars with respect to
   * overflows. If a hardware menu key is present, the overflow icon is never displayed in
   * ActionBars and can only be interacted with via menu key presses.
   */
  public static void openActionBarOverflowOrOptionsMenu(Context context) {
    try (Span ignored = tracer.beginSpan("Espresso-openActionBarOverflowOrOptionsMenu")) {
      // We need to wait for Activity#onPrepareOptionsMenu to be called before trying to open
      // overflow or it's missing. onPrepareOptionsMenu is called by Choreographer after onResume
      // and view is attached to window. To ensure the options menu is created before we try to
      // open, wait for all processing tasks in this frame to be finished.
      waitUntilNextFrame(2);

      if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.HONEYCOMB) {
        // regardless of the os level of the device, this app will be rendering a menukey
        // in the virtual navigation bar (if present) or responding to hardware option keys on
        // any activity.
        onView(isRoot()).perform(pressMenuKey());
      } else if (hasVirtualOverflowButton(context)) {
        // If we're using virtual keys - theres a chance we're in mid animation of switching
        // between a contextual action bar and the non-contextual action bar. In this case there
        // are 2 'More Options' buttons present. Lets wait till that is no longer the case.
        onView(isRoot()).perform(new TransitionBridgingViewAction());

        onView(OVERFLOW_BUTTON_MATCHER).perform(click());
      } else {
        // either a hardware button exists, or we're on a pre-HC os.
        onView(isRoot()).perform(pressMenuKey());
      }

      // Again, we need to wait for the next rendering frame so that overflow menu is rendered on
      // screen. This wait is especially important for API 29+ devices. If we skip this wait and you
      // try clicking on overflow menu, the click may delivered to unexpected view which is
      // positioned the same location but under the overflow menu. This happens because the context
      // menu is there in view hierarchy but not rendered yet so Espresso is able to calculate
      // coordinate but injected motion event goes to the wrong view. Waits for two frames because
      // runnable to display the menu is registered in the current frame and it is executed in the
      // next frame.
      waitUntilNextFrame(2);
    }
  }

  private static void waitUntilNextFrame(int times) {
    // Choreographer API is added in API 16.
    if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
      return;
    }

    for (int i = 0; i < times; ++i) {
      CountDownLatch latch = new CountDownLatch(1);
      InstrumentationRegistry.getInstrumentation()
          .runOnMainSync(
              () ->
                  Choreographer.getInstance()
                      .postFrameCallback(frameTimeNanos -> latch.countDown()));
      BASE.controlledLooper().drainMainThreadUntilIdle();
      try {
        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Log.w(TAG, "Waited for the next frame to start but never happened.");
        return;
      }
    }
  }

  /**
   * Loops the main thread until the app goes idle.
   *
   * <p>Same as {@link Espresso#onIdle()}, but takes an additional {@link Callable} as parameter,
   * which is executed after the app goes idle.
   *
   * @param action callable executed when the app goes idle.
   * @return the computed result of the action callable.
   * @throws AppNotIdleException when app does not go Idle within the master policies timeout.
   * @throws RuntimeException when being invoked on the main thread.
   */
  public static <T> T onIdle(Callable<T> action) {
    try (Span ignored = tracer.beginSpan("Espresso-onIdle")) {
      Checks.checkNotMainThread();

      Executor mainThreadExecutor = BASE.mainThreadExecutor();
      ListenableFutureTask<Void> idleFuture =
          ListenableFutureTask.create(
              new Runnable() {
                @Override
                public void run() {
                  BASE.uiController().loopMainThreadUntilIdle();
                }
              },
              null);
      FutureTask<T> actionTask = new FutureTask<>(action);
      idleFuture.addListener(actionTask, mainThreadExecutor);
      mainThreadExecutor.execute(idleFuture);
      BASE.controlledLooper().drainMainThreadUntilIdle();

      try {
        idleFuture.get();
        return actionTask.get();
      } catch (InterruptedException ie) {
        throw new RuntimeException(ie);
      } catch (ExecutionException ee) {
        if (ee.getCause() instanceof AppNotIdleException) {
          throw (AppNotIdleException) ee.getCause();
        } else {
          throw new RuntimeException(ee);
        }
      }
    }
  }

  /**
   * Loops the main thread until the app goes idle.
   *
   * <p>Only call this method for tests that do not interact with any UI elements, but require
   * Espresso's main thread synchronisation! This method is mainly useful for test utilities and
   * frameworks that are build on top of Espresso.
   *
   * <p>For UI tests use {@link Espresso#onView(Matcher)} or {@link Espresso#onData(Matcher)}. These
   * APIs already use Espresso's internal synchronisation mechanisms and do not require a call to
   * {@link Espresso#onIdle()}.
   *
   * @throws AppNotIdleException when app does not go Idle within the master policies timeout.
   * @throws RuntimeException when being invoked on the main thread.
   */
  public static void onIdle() {
    onIdle(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            // no-op callable
            return null;
          }
        });
  }

  private static boolean hasVirtualOverflowButton(Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    } else {
      return !ViewConfiguration.get(context).hasPermanentMenuKey();
    }
  }

  /**
   * Handles the cases where the app is transitioning between a contextual action bar and a non
   * contextual action bar.
   */
  private static class TransitionBridgingViewAction implements ViewAction {
    @Override
    public void perform(UiController controller, View view) {
      int loops = 0;
      while (isTransitioningBetweenActionBars(view) && loops < 100) {
        loops++;
        controller.loopMainThreadForAtLeast(50);
      }
      // if we're not transitioning properly the next viewaction
      // will give a decent enough exception.
    }

    @Override
    public String getDescription() {
      return "Handle transition between action bar and action bar context.";
    }

    @Override
    public Matcher<View> getConstraints() {
      return isRoot();
    }

    private boolean isTransitioningBetweenActionBars(View view) {
      int actionButtonCount = 0;
      for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
        if (OVERFLOW_BUTTON_MATCHER.matches(child)) {
          actionButtonCount++;
        }
      }
      return actionButtonCount > 1;
    }
  }
}
