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
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.internal.util.Checks.checkState;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * On-device {@link ActivityInvoker} implementation that drives Activity lifecycles using {@link
 * android.app.ActivityManager} indirectly via {@link Context#startActivity} and {@link
 * Activity#recreate}.
 *
 * <p>Some methods in this class are non-blocking API. It's caller's responsibility to wait for
 * activity state to be desired state.
 */
class InstrumentationActivityInvoker implements ActivityInvoker {

  /**
   * The timeout for waiting an arbitrary condition to be met. If the condition isn't satisfied
   * before the timeout, {@link AssertionError} will be thrown.
   */
  private static final long TIMEOUT_SECONDS = 45;

  /**
   * An intent action broadcasted by {@link EmptyActivity} notifying the activity becomes resumed
   * state.
   */
  private static final String EMPTY_ACTIVITY_RESUMED =
      "androidx.test.core.app.InstrumentationActivityInvoker.EMPTY_ACTIVITY_RESUMED";

  /**
   * An intent action broadcasted by {@link EmptyFloatingActivity} notifying the activity becomes
   * resumed state.
   */
  private static final String EMPTY_FLOATING_ACTIVITY_RESUMED =
      "androidx.test.core.app.InstrumentationActivityInvoker.EMPTY_FLOATING_ACTIVITY_RESUMED";

  /**
   * An intent action to notify {@link EmptyActivity} and {@link EmptyFloatingActivity} to be
   * finished.
   */
  private static final String FINISH_EMPTY_ACTIVITIES =
      "androidx.test.core.app.InstrumentationActivityInvoker.FINISH_EMPTY_ACTIVITIES";

  /**
   * An empty activity with style "android:windowIsFloating = false". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>When this activity is resumed, it broadcasts {@link
   * InstrumentationActivityInvoker#EMPTY_ACTIVITY_RESUMED} action to notify the state.
   *
   * <p>This activity finishes itself when it receives {@link
   * InstrumentationActivityInvoker#FINISH_EMPTY_ACTIVITIES} action.
   *
   * <p>This activity is used to send an arbitrary resumed Activity to stopped.
   */
  public static class EmptyActivity extends Activity {
    private final BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            finish();
          }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      registerReceiver(receiver, new IntentFilter(FINISH_EMPTY_ACTIVITIES));
    }

    @Override
    protected void onResume() {
      super.onResume();
      sendBroadcast(new Intent(EMPTY_ACTIVITY_RESUMED));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);
    }
  };

  /**
   * An empty activity with style "android:windowIsFloating = true". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>When this activity is resumed, it broadcasts {@link
   * InstrumentationActivityInvoker#EMPTY_FLOATING_ACTIVITY_RESUMED} action to notify the state.
   *
   * <p>This activity finishes itself when it receives {@link
   * InstrumentationActivityInvoker#FINISH_EMPTY_ACTIVITIES} action.
   *
   * <p>This activity is used to send an arbitrary resumed Activity to paused.
   */
  public static class EmptyFloatingActivity extends Activity {
    private final BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            finish();
          }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      registerReceiver(receiver, new IntentFilter(FINISH_EMPTY_ACTIVITIES));
    }

    @Override
    protected void onResume() {
      super.onResume();
      sendBroadcast(new Intent(EMPTY_FLOATING_ACTIVITY_RESUMED));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);
    }
  };

  /**
   * Starts an Activity using the given intent. {@link Intent#FLAG_ACTIVITY_NEW_TASK} and {@link
   * Intent#FLAG_ACTIVITY_CLEAR_TASK} flags are set to the intent to start the Activity in brand-new
   * task stack.
   */
  @Override
  public void startActivity(Intent intent) {
    // Close empty activities if it's running. This might happen if the previous test crashes while
    // empty activities are resumed.
    getTargetContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));

    // Note: Instrumentation.startActivitySync(Intent) cannot be used here because it has an
    // assertion that the resolved Activity's process name equals to the test target package name.
    // This is to ensure that the Activity is launched in the same process as instrumentation
    // however it is still possible that Activities with different process names share the same PID
    // if they set the same android:sharedUserId in AndroidManifests.
    getTargetContext()
        .startActivity(
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
  }

  /** Resumes the tested activity by finishing empty activities. */
  @Override
  public void resumeActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);
    getTargetContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));
  }

  /**
   * Pauses the tested activity by starting {@link EmptyFloatingActivity} on top of the tested
   * activity.
   */
  @Override
  public void pauseActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED);

    CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            latch.countDown();
          }
        };
    getTargetContext()
        .registerReceiver(receiver, new IntentFilter(EMPTY_FLOATING_ACTIVITY_RESUMED));

    // Starting an arbitrary Activity (android:windowIsFloating = true) forces the tested Activity
    // to the paused state.
    getTargetContext()
        .startActivity(
            getIntentForActivity(EmptyFloatingActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    try {
      latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AssertionError("Failed to pause activity", e);
    } finally {
      getTargetContext().unregisterReceiver(receiver);
    }
  }

  /** Stops the tested activity by starting {@link EmptyActivity} on top of the tested activity. */
  @Override
  public void stopActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);

    CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            latch.countDown();
          }
        };
    getTargetContext().registerReceiver(receiver, new IntentFilter(EMPTY_ACTIVITY_RESUMED));

    // Starting an arbitrary Activity (android:windowIsFloating = false) forces the tested Activity
    // to the stopped state.
    getTargetContext()
        .startActivity(
            getIntentForActivity(EmptyActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    try {
      latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AssertionError("Failed to stop activity", e);
    } finally {
      getTargetContext().unregisterReceiver(receiver);
    }
  }

  /**
   * Recreates the Activity by {@link Activity#recreate}.
   *
   * <p>Note that {@link Activity#recreate}'s behavior differs by Android framework version. For
   * example, the version P brings Activity's lifecycle state to the original state after the
   * re-creation. A stopped Activity goes to stopped state after the re-creation in concrete.
   * Whereas the version O ignores {@link Activity#recreate} method call when the activity is in
   * stopped state. The version N re-creates stopped Activity but brings back to paused state
   * instead of stopped.
   *
   * <p>In short, make sure to set Activity's state to resumed before calling this method otherwise
   * the behavior is the framework version dependent.
   */
  @Override
  public void recreateActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);
    getInstrumentation().runOnMainSync(() -> activity.recreate());
  }

  @Override
  public void finishActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);

    // Stop the activity before finish it as a workaround for the framework bug in API level 15 to
    // 22 where the framework never calls #onStop and #onDestroy if you call Activity#finish while
    // floating style Activity is in the stack.
    stopActivity(activity);

    getInstrumentation().runOnMainSync(() -> activity.finish());
    getTargetContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));
  }

  private static void checkActivityStageIsIn(Activity activity, Stage... expected) {
    checkActivityStageIsIn(activity, new HashSet<>(Arrays.asList(expected)));
  }

  private static void checkActivityStageIsIn(Activity activity, Set<Stage> expected) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  expected.contains(stage),
                  "Activity's stage must be %s but was %s",
                  expected,
                  stage);
            });
  }
}
