/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.core.app;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotMainThread;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.State;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;
import androidx.test.annotation.Beta;
import androidx.test.internal.platform.ServiceLoaderWrapper;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ActivityScenario provides APIs to start and drive an Activity's lifecycle state for testing. It
 * works with arbitrary activities and works consistently across different versions of the Android
 * framework.
 *
 * <p>The ActivityScenario API uses {@link Lifecycle.State} extensively. If you are unfamiliar with
 * {@link android.arch.lifecycle} components, please read <a
 * href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">lifecycle</a>
 * before starting. It is crucial to understand the difference between {@link Lifecycle.State} and
 * {@link Lifecycle.Event}.
 *
 * <p>{@link ActivityScenario#moveTo(Lifecycle.State)} allows you to transition your Activity's
 * state to {@link State.CREATED}, {@link State.STARTED}, or {@link State.RESUMED}. There are two
 * paths for an Activity to reach {@link State.CREATED}: after {@link Event.ON_CREATE} happens but
 * before {@link Event.ON_START}, and after {@link Event.ON_STOP}. ActivityScenario always moves the
 * Activity's state to the second one. The same applies to {@link State.STARTED}.
 *
 * <p>This class is a replacement of ActivityController in Robolectric and ActivityTestRule in ATSL.
 *
 * <p>Following are the example of common use cases.
 *
 * <pre>
 * Before:
 *   MyActivity activity = Robolectric.setupActivity(MyActivity.class);
 *   assertThat(activity.getSomething()).isEqualTo("something");
 *
 * After:
 *   ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class);
 *   scenario.runOnActivity(activity -> {
 *     assertThat(activity.getSomething()).isEqualTo("something");
 *   });
 *
 * Before:
 *   ActivityController<MyActivity> controller = Robolectric.buildActivity(MyActivity.class);
 *   controller.create().start().resume();
 *   controller.get();          // Returns resumed activity.
 *   controller.pause().get();  // Returns paused activity.
 *   controller.stop().get();   // Returns stopped activity.
 *
 * After:
 *   ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class);
 *   scenario.runOnActivity(activity -> {});  // Your activity is resumed.
 *   scenario.moveTo(State.STARTED);
 *   scenario.runOnActivity(activity -> {});  // Your activity is paused.
 *   scenario.moveTo(State.CREATED);
 *   scenario.runOnActivity(activity -> {});  // Your activity is stopped.
 * </pre>
 *
 * BEGIN GOOGLE-INTERNAL Android API Council Review: go/activity-controller-unified-api-rev4-review
 * Design Doc: go/activityscenario-impl END GOOGLE-INTERNAL
 */
@Beta
public final class ActivityScenario<A extends Activity> {
  /**
   * The timeout for {@link #waitForActivityToBecome} method. If an Activity doesn't become
   * requested state after the timeout, we will throw {@link AssertionError} to fail tests.
   */
  private static final long TIMEOUT_MILLISECONDS = 45000;

  /** An ActivityInvoker to use. Implementation class can be configured by service provider. */
  private static final ActivityInvoker activityInvoker;

  static {
    List<ActivityInvoker> impls = ServiceLoaderWrapper.loadService(ActivityInvoker.class);
    if (impls.isEmpty()) {
      activityInvoker = new InstrumentationActivityInvoker();
    } else if (impls.size() == 1) {
      activityInvoker = impls.get(0);
    } else {
      throw new IllegalStateException(
          String.format(
              "Found more than one %s implementations.", ActivityInvoker.class.getName()));
    }
  }

  /**
   * A map to convert {@link Stage} to {@link State}. This map only contains stages that are
   * supported in {@link #moveToState}.
   */
  private static final Map<Stage, State> SUPPORTED_STAGE_TO_STATE = new EnumMap<>(Stage.class);

  static {
    SUPPORTED_STAGE_TO_STATE.put(Stage.RESUMED, State.RESUMED);
    SUPPORTED_STAGE_TO_STATE.put(Stage.PAUSED, State.STARTED);
    SUPPORTED_STAGE_TO_STATE.put(Stage.STOPPED, State.CREATED);
  }

  /** A lock that is used to block the main thread until the Activity becomes a requested state. */
  private final ReentrantLock lock = new ReentrantLock();

  /** A map to retrieve condition object by state. */
  private final Map<State, Condition> stateToCondition = new EnumMap<>(State.class);

  /** An intent to start a testing Activity. */
  private final Intent startActivityIntent;

  /**
   * A current activity stage. This variable is updated by {@link ActivityLifecycleMonitor} from the
   * main thread.
   */
  @GuardedBy("lock")
  private Stage currentActivityStage;

  /**
   * A current activity. This variable is updated by {@link ActivityLifecycleMonitor} from the main
   * thread.
   */
  @GuardedBy("lock")
  @Nullable
  private A currentActivity;

  /** Private constructor. Use {@link #launch} to instantiate this class. */
  private ActivityScenario(Class<A> activityClass) {
    checkState(
        Settings.System.getInt(
                getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Global.ALWAYS_FINISH_ACTIVITIES,
                0)
            == 0,
        "\"Don't keep activities\" developer options must be disabled for ActivityScenario");

    stateToCondition.put(State.CREATED, lock.newCondition());
    stateToCondition.put(State.STARTED, lock.newCondition());
    stateToCondition.put(State.RESUMED, lock.newCondition());

    startActivityIntent = activityInvoker.getIntentForActivity(activityClass);
    currentActivityStage = Stage.PRE_ON_CREATE;
  }

  /**
   * Launches an Activity of a given class and constructs ActivityScenario with the activity. Waits
   * for the activity to become {@link State#RESUMED}.
   *
   * <p>This method cannot be called from the main thread except in Robolectric tests.
   *
   * @throws AssertionError if Activity never becomes {@link State#RESUMED} after timeout
   * @return ActivityScenario which you can use to make further state transitions
   */
  public static <A extends Activity> ActivityScenario<A> launch(Class<A> activityClass) {
    checkNotMainThread();
    getInstrumentation().waitForIdleSync();

    ActivityScenario<A> scenario = new ActivityScenario<>(activityClass);
    ActivityLifecycleMonitorRegistry.getInstance()
        .addLifecycleCallback(scenario.activityLifecycleObserver);

    activityInvoker.startActivity(scenario.startActivityIntent);
    scenario.waitForActivityToBecome(State.RESUMED);

    return scenario;
  }

  private void waitForActivityToBecome(State state) {
    // Wait for idle sync otherwise we might hit transient state.
    getInstrumentation().waitForIdleSync();

    lock.lock();
    try {
      if (state == SUPPORTED_STAGE_TO_STATE.get(currentActivityStage)) {
        return;
      }

      // Spurious wakeups may happen so we wrap await() with while-loop.
      // BEGIN GOOGLE-INTERNAL
      // go/errorprone/bugpattern/WaitNotInLoop
      // END GOOGLE-INTERNAL
      long now = System.currentTimeMillis();
      long deadline = now + TIMEOUT_MILLISECONDS;
      while (now < deadline && state != SUPPORTED_STAGE_TO_STATE.get(currentActivityStage)) {
        stateToCondition.get(state).await(deadline - now, TimeUnit.MILLISECONDS);
        now = System.currentTimeMillis();
      }

      if (state != SUPPORTED_STAGE_TO_STATE.get(currentActivityStage)) {
        throw new AssertionError(
            String.format(
                "Activity never becomes requested state \"%s\" "
                    + "(last lifecycle transition = \"%s\")",
                state, currentActivityStage));
      }
    } catch (InterruptedException e) {
      throw new AssertionError(
          String.format(
              "Activity never becomes requested state \"%s\" (last lifecycle transition = \"%s\")",
              state, currentActivityStage));
    } finally {
      lock.unlock();
    }
  }

  /** Observes an Activity lifecycle change events and updates ActivityScenario's internal state. */
  private final ActivityLifecycleCallback activityLifecycleObserver =
      new ActivityLifecycleCallback() {
        @Override
        public void onActivityLifecycleChanged(Activity activity, Stage stage) {
          if (!startActivityIntent.filterEquals(activity.getIntent())) {
            return;
          }
          lock.lock();
          try {
            currentActivityStage = stage;
            currentActivity = (A) (stage != Stage.DESTROYED ? activity : null);

            State currentState = SUPPORTED_STAGE_TO_STATE.get(stage);
            if (currentState != null) {
              stateToCondition.get(currentState).signal();
            }
          } finally {
            lock.unlock();
          }
        }
      };

  /**
   * ActivityState is a state class that holds a snapshot of an Activity's current state and a
   * reference to the Activity.
   */
  private static class ActivityState<A extends Activity> {
    @Nullable final A activity;
    @Nullable final State state;

    ActivityState(@Nullable A activity, @Nullable State state) {
      this.activity = activity;
      this.state = state;
    }
  }

  private ActivityState<A> getCurrentActivityState() {
    getInstrumentation().waitForIdleSync();
    lock.lock();
    try {
      return new ActivityState<>(
          currentActivity, SUPPORTED_STAGE_TO_STATE.get(currentActivityStage));
    } finally {
      lock.unlock();
    }
  }

  /**
   * Moves Activity state to a new state.
   *
   * <p>If a new state and current state are the same, it does nothing. It accepts {@link
   * State.CREATED}, {@link State.STARTED}, and {@link State.RESUMED}.
   *
   * <p>This method cannot be called from the main thread except in Robolectric tests.
   *
   * @throws IllegalArgumentException if unsupported {@code newState} is given
   * @throws IllegalStateException if Activity is destroyed, finished or finishing
   * @throws AssertionError if Activity never becomes requested state
   */
  public ActivityScenario<A> moveToState(State newState) {
    checkNotMainThread();
    checkArgument(
        stateToCondition.containsKey(newState),
        String.format("A requested state \"%s\" is not supported", newState));
    getInstrumentation().waitForIdleSync();

    ActivityState<A> currentState = getCurrentActivityState();
    checkNotNull(currentState.state);
    if (currentState.state == newState) {
      return this;
    }
    checkState(
        currentState.state != State.DESTROYED && currentState.activity != null,
        String.format(
            "Cannot move to state \"%s\" since the Activity has been destroyed already", newState));

    switch (newState) {
      case CREATED:
        activityInvoker.stopActivity(currentState.activity);
        break;
      case STARTED:
        moveToState(State.RESUMED);
        activityInvoker.pauseActivity(currentState.activity);
        break;
      case RESUMED:
        activityInvoker.resumeActivity(currentState.activity);
        break;
      default:
        throw new IllegalArgumentException(
            String.format("A requested state \"%s\" is not supported", newState));
    }

    waitForActivityToBecome(newState);
    return this;
  }

  /**
   * Recreates the Activity.
   *
   * <p>A current Activity will be destroyed after its data is saved into {@link android.os.Bundle}
   * with {@link Activity#savedInstanceState}, then it creates a new Activity with the saved Bundle.
   * After this method call, it is ensured that the Activity state goes back to the same state as
   * its previous state.
   *
   * <p>This method cannot be called from the main thread except in Robolectric tests.
   *
   * @throws IllegalStateException if Activity is destroyed, finished or finishing
   * @throws AssertionError if Activity never be re-created
   */
  public ActivityScenario<A> recreate() {
    checkNotMainThread();
    getInstrumentation().waitForIdleSync();

    final ActivityState<A> prevActivityState = getCurrentActivityState();
    checkNotNull(prevActivityState.activity);
    checkNotNull(prevActivityState.state);

    // Move the state to RESUMED before starting re-creation and manually move the state to its
    // original state after the re-creation. This is because Activity#recreate's behavior differs
    // by Android framework version. See InstrumentationActivityInvoker#recreateActivity for
    // details.
    moveToState(State.RESUMED);
    activityInvoker.recreateActivity(prevActivityState.activity);

    ActivityState<A> activityState;
    long now = System.currentTimeMillis();
    long deadline = now + TIMEOUT_MILLISECONDS;
    do {
      waitForActivityToBecome(State.RESUMED);
      now = System.currentTimeMillis();
      activityState = getCurrentActivityState();
    } while (now < deadline && activityState.activity == prevActivityState.activity);
    if (activityState.activity == prevActivityState.activity) {
      throw new IllegalStateException("Requested a re-creation of Activity but didn't happen");
    }

    moveToState(prevActivityState.state);

    return this;
  }

  /**
   * The RunOnActivity interface should be implemented by any class whose instances are intended to
   * be executed by the main thread. An Activity that is instrumented by the ActivityScenario is
   * passed to {@link RunOnActivity#run} method.
   *
   * <pre>
   * Example:
   *   ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class);
   *   scenario.runOnActivity(activity -> {
   *     assertThat(activity.getSomething()).isEqualTo("something");
   *   });
   * </pre>
   *
   * <p>You should never keep the Activity reference. It should only be accessed in {@link
   * RunOnActivity#run} scope for two reasons: 1) Android framework may re-create the Activity
   * during lifecycle changes, your holding reference might be stale. 2) It increases the reference
   * counter and it may affect to the framework behavior, especially after you finish the Activity.
   *
   * <pre>
   * Bad Example:
   *   ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class);
   *   final MyActivity[] myActivityHolder = new MyActivity[1];
   *   scenario.runOnActivity(activity -> {
   *     myActivityHolder[0] = activity;
   *   });
   *   assertThat(myActivityHolder[0].getSomething()).isEqualTo("something");
   * </pre>
   */
  public interface RunOnActivity<A extends Activity> {
    /**
     * This method is invoked on the main thread with the reference to the Activity.
     *
     * @param activity an Activity instrumented by the {@link ActivityScenario}. It never be null.
     */
    void run(A activity);
  }

  /**
   * Runs a given {@code runOnActivity} on the current Activity's main thread.
   *
   * <p>Note that you should never keep Activity reference passed into your {@code runOnActivity}
   * because it can be recreated at anytime during state transitions.
   *
   * <p>Throwing an exception from {@code runOnActivity} makes the Activity to crash. You can
   * inspect the exception in logcat outputs.
   *
   * <p>This method cannot be called from the main thread except in Robolectric tests.
   *
   * @throws IllegalStateException if Activity is destroyed, finished or finishing
   */
  public ActivityScenario<A> runOnActivity(final RunOnActivity<A> runOnActivity) {
    checkNotMainThread();
    getInstrumentation().waitForIdleSync();
    getInstrumentation()
        .runOnMainSync(
            () -> {
              lock.lock();
              try {
                checkNotNull(
                    currentActivity,
                    "Cannot run runOnActivity since Activity has been destroyed already");
                runOnActivity.run(currentActivity);
              } finally {
                lock.unlock();
              }
            });
    return this;
  }
}
