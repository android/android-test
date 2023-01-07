/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.UiController;
import java.util.List;
import kotlin.collections.CollectionsKt;

/** Helper methods to synchronize configuration changes with onView actions. */
final class ConfigurationSynchronizationUtils {
  private static final String TAG = ConfigurationSynchronizationUtils.class.getSimpleName();
  private static final List<Integer> ORIENTATION_WAIT_TIMES =
      CollectionsKt.listOf(10, 50, 100, 250, 500, 2000 /* 2sec */);

  private ConfigurationSynchronizationUtils() {}

  /**
   * Wait for configuration changes to finish on the activity if there are any in progress.
   *
   * @param currentActivity the Activity to check for configuration changes in progress.
   * @param uiController used to loop the main thread while configuration changes occur.
   * @param appContext the application's context
   */
  public static void waitForConfigurationChangesOnActivity(
      Activity currentActivity, UiController uiController, Context appContext) {
    // The activity's orientation can differ from application's orientation when the activity is in
    // multi-window mode.
    if (Build.VERSION.SDK_INT >= 24 && currentActivity.isInMultiWindowMode()) {
      return;
    }

    int applicationOrientation = appContext.getResources().getConfiguration().orientation;
    if (applicationOrientation != currentActivity.getResources().getConfiguration().orientation) {
      for (long waitTime : ORIENTATION_WAIT_TIMES) {
        Log.w(
            TAG,
            "Activity's orientation does not match the application's - waiting: "
                + waitTime
                + "ms for orientation to update.");
        uiController.loopMainThreadForAtLeast(waitTime);
        if (applicationOrientation
            == currentActivity.getResources().getConfiguration().orientation) {
          return;
        }
      }

      throw new NoActivityResumedException(
          "Timed out waiting for Activity's orientation to update.");
    }
  }
}
