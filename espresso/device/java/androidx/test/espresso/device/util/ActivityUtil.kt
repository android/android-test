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

@file:JvmName("ActivityUtil")

package androidx.test.espresso.device.util

import android.app.Activity
import android.app.Instrumentation
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/** Collection of utility methods for interacting with activities. */
private val TAG = "ActivityUtil"

/**
 * Detects if configuration changes are handled by the activity.
 *
 * @param configBit, bit in ActivityInfo#configChanges that indicates whether the activity can
 * handle this type of configuration change.
 * @return whether the activity handles the given configuration change.
 */
fun Activity.isConfigurationChangeHandled(configBit: Int): Boolean {
  val activityInfo = this.getPackageManager().getActivityInfo(this.getComponentName(), 0)
  return (activityInfo.configChanges and configBit) != 0
}

/**
* Calculates the current width and height of the test device's display.
*
* @param instrumentation, an instance of Instrumentation used to execute shell commands on the test device 
* @return a Pair of integers corresponding to the display's width and height
*/
fun Activity.calculateCurrentDisplayWidthAndHeight(
  instrumentation: Instrumentation
): Pair<Int, Int> {
  // "wm size" will output a string with the format
  // "Physical size: WxH
  //  Override size: WxH"
  val parcelFileDescriptor = instrumentation.getUiAutomation().executeShellCommand("wm size")
  val output: String
  AutoCloseInputStream(parcelFileDescriptor).use { inputStream ->
    output = inputStream.readBytes().toString(Charset.defaultCharset())
  }

  val subStringToFind = "Override size: "
  val displaySizes =
    output.substring(output.indexOf(subStringToFind) + subStringToFind.length).trim().split("x")
  val widthPx = displaySizes.get(0).toInt()
  val heightPx = displaySizes.get(1).toInt()

  val widthDp = (widthPx / this.getResources().displayMetrics.density).roundToInt()
  val heightDp = (heightPx / this.getResources().displayMetrics.density).roundToInt()
  return Pair(widthDp, heightDp)
}

/**
 * Returns the first activity found in the RESUMED stage, or null if none are found after waiting up
 * to two seconds for one.
 */
fun getResumedActivityOrNull(): Activity? {
  var activity: Activity? = null
  InstrumentationRegistry.getInstrumentation().runOnMainSync {
    run {
      val activities: Collection<Activity> =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
      if (activities.size > 1) {
        val activityNames = activities.map { it.getLocalClassName() }
        Log.d(
          TAG,
          "More than one activity was found in the RESUMED stage. Activities found: $activityNames"
        )
      } else if (activities.isEmpty()) {
        Log.d(TAG, "No activity found in the RESUMED stage. Waiting up to 2 seconds for one.")
        val latch = CountDownLatch(1)
        ActivityLifecycleMonitorRegistry.getInstance()
          .addLifecycleCallback(
            object : ActivityLifecycleCallback {
              override fun onActivityLifecycleChanged(newActivity: Activity, stage: Stage) {
                if (stage == Stage.RESUMED) {
                  Log.d(TAG, "Found ${newActivity.getLocalClassName()} in the RESUMED stage.")
                  ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this)
                  latch.countDown()
                  activity = newActivity
                }
              }
            }
          )
        latch.await(2, TimeUnit.SECONDS)
      } else {
        activity = activities.elementAtOrNull(0)
      }
    }
  }
  InstrumentationRegistry.getInstrumentation().waitForIdleSync()
  return activity
}
