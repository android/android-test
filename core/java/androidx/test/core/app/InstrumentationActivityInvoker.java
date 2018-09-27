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
import android.content.Intent;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

/**
 * On-device {@link ActivityInvoker} implementation that drives Activity lifecycles using {@link
 * android.app.ActivityManager} indirectly via {@link Activity#startActivity} and {@link
 * Activity#recreate}.
 *
 * <p>All the methods in this class are non-blocking API.
 */
class InstrumentationActivityInvoker implements ActivityInvoker {
  /**
   * An empty activity with style "android:windowIsFloating = false". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>This activity is used to send an arbitrary resumed Activity to stopped.
   */
  public static class EmptyActivity extends Activity {};

  /**
   * An empty activity with style "android:windowIsFloating = true". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>This activity is used to send an arbitrary resumed Activity to paused.
   */
  public static class EmptyFloatingActivity extends Activity {};

  /**
   * Starts an Activity using the given intent. FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TOP
   * flags are set to the intent to start the Activity in brand-new task stack. Note: {@link
   * Instrumentation#startActivitySync} cannot be used here because it has an assertion that the
   * resolved Activity's process name equals to the test target package name. This is to ensure that
   * the Activity is launched in the same process as instrumentation but it is possible to run
   * Activities with different process name in a same process if they share the same
   * android:sharedUserId in the both AndroidManifests.
   */
  @Override
  public void startActivity(Intent intent) {
    getTargetContext()
        .startActivity(
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
  }

  /**
   * Resumes the Activity by issuing a start activity intent with {@link
   * Intent#FLAG_ACTIVITY_REORDER_TO_FRONT} flag, that brings back the Activity to the top of the
   * history stack (or starts new one if the Activity is not found in the stack).
   */
  @Override
  public void resumeActivity(Activity activity) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  stage == Stage.RESUMED || stage == Stage.PAUSED || stage == Stage.STOPPED,
                  "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.",
                  stage);
              activity.startActivity(
                  activity.getIntent().setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            });
  }

  /**
   * Pauses the Activity by issuing a start activity intent of {@link EmptyFloatingActivity} with
   * {@link Intent#FLAG_ACTIVITY_REORDER_TO_FRONT} flag, that brings back the Activity to the top of
   * the history stack (or starts new one if the Activity is not found in the stack).
   */
  @Override
  public void pauseActivity(Activity activity) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  stage == Stage.RESUMED || stage == Stage.PAUSED,
                  "Activity's stage must be RESUMED or PAUSED but was %s.",
                  stage);
              // Starting an arbitrary Activity (android:windowIsFloating = true) forces the tested
              // Activity
              // to the paused and still visible state.
              activity.startActivity(
                  getIntentForActivity(EmptyFloatingActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            });
  }

  /**
   * Stops the Activity by issuing a start activity intent of {@link EmptyActivity} with {@link
   * Intent#FLAG_ACTIVITY_REORDER_TO_FRONT} flag, that brings back the Activity to the top of the
   * history stack (or starts new one if the Activity is not found in the stack).
   */
  @Override
  public void stopActivity(Activity activity) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  stage == Stage.RESUMED || stage == Stage.PAUSED || stage == Stage.STOPPED,
                  "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.",
                  stage);
              // Starting an arbitrary Activity (android:windowIsFloating = false) forces the tested
              // Activity to the stopped state.
              activity.startActivity(
                  getIntentForActivity(EmptyActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            });
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
  public void recreateActivity(final Activity activity) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  stage == Stage.RESUMED || stage == Stage.PAUSED || stage == Stage.STOPPED,
                  "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.",
                  stage);
              activity.recreate();
            });
  }
}
