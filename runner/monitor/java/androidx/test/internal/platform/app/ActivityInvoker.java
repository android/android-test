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

package androidx.test.internal.platform.app;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Invokes lifecycle event changes on an {@link Activity}. All methods may work synchronously or
 * asynchronously depending on the implementation class.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">Android
 *     Component LifeCycles</a>
 */
public interface ActivityInvoker {
  /**
   * Finds an intent to start an Activity of the given class. It looks up target context first and
   * fallback to instrumentation context.
   */
  default Intent getIntentForActivity(Class<? extends Activity> activityClass) {
    Intent intent =
        Intent.makeMainActivity(
            new ComponentName(getInstrumentation().getTargetContext(), activityClass));
    if (getInstrumentation().getTargetContext().getPackageManager().resolveActivity(intent, 0)
        != null) {
      return intent;
    }
    return Intent.makeMainActivity(
        new ComponentName(getInstrumentation().getContext(), activityClass));
  }

  /**
   * Starts an activity using the given intent. After the activity finishes you can retrieve its
   * result code and data via {@link #getActivityResult()}.
   */
  void startActivity(Intent intent, @Nullable Bundle activityOptions);

  /** Convenience method to retain backwards compatibility */
  void startActivity(Intent intent);

  /**
   * Returns activity result that is started by {@link #startActivity}. Unlike other methods this
   * method blocks execution until the result becomes available.
   *
   * <p>Note: this method doesn't call {@link Activity#finish()} of the activity that is started by
   * {@link #startActivity}. If you call this method without calling that, it will end up with
   * runtime error and make the test to fail.
   */
  ActivityResult getActivityResult();

  /**
   * Transitions a current Activity to {@link androidx.test.runner.lifecycle.Stage#RESUMED}.
   *
   * <p>The current Activity state must be resumed, paused, or stopped.
   *
   * @throws IllegalStateException when you call this method with Activity in non-supported state
   */
  void resumeActivity(Activity activity);

  /**
   * Transitions a current Activity to {@link androidx.test.runner.lifecycle.Stage#PAUSED}.
   *
   * <p>The current Activity state must be resumed or paused.
   *
   * @throws IllegalStateException when you call this method with Activity in non-supported state
   */
  void pauseActivity(Activity activity);

  /**
   * Transitions a current Activity to {@link androidx.test.runner.lifecycle.Stage#STOPPED}.
   *
   * <p>The current Activity state must be resumed, paused, or stopped.
   *
   * @throws IllegalStateException when you call this method with Activity in non-supported state
   */
  void stopActivity(Activity activity);

  /**
   * Recreates the Activity and transitions it to the previous {@link
   * androidx.test.runner.lifecycle.Stage}.
   *
   * <p>The current Activity state must be resumed, paused, or stopped.
   *
   * @throws IllegalStateException when you call this method with Activity in non-supported state
   */
  void recreateActivity(Activity activity);

  /**
   * Finishes the Activity.
   *
   * <p>The current Activity state must be resumed, paused, or stopped.
   *
   * @throws IllegalStateException when you call this method with Activity in non-supported state
   */
  void finishActivity(Activity activity);
}
