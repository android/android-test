/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.espresso.device.action

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.device.common.calculateCurrentDisplayWidthAndHeightPx
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.getResumedActivityOrNull
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/** Action to set the test device to the provided display size. */
internal class DisplaySizeAction(
  val widthDisplaySize: WidthSizeClass,
  val heightDisplaySize: HeightSizeClass
) : DeviceAction {
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    if (getDeviceApiLevel() < 24) {
      throw UnsupportedDeviceOperationException(
        "Setting display size is not supported on devices with APIs below 24."
      )
    }

    val currentActivity = getResumedActivityOrNull()
    if (currentActivity != null) {
      val displaySize = calculateCurrentDisplayWidthAndHeightDp(currentActivity)
      val startingWidth = displaySize.first
      val startingHeight = displaySize.second
      if (
        widthDisplaySize == WidthSizeClass.compute(startingWidth) &&
          heightDisplaySize == HeightSizeClass.compute(startingHeight)
      ) {
        Log.d(TAG, "Device display is already the requested size, no changes needed.")
        return
      }

      val latch: CountDownLatch = CountDownLatch(1)

      val currentActivityView: View =
        object : View(currentActivity) {
          override fun onConfigurationChanged(newConfig: Configuration?) {
            super.onConfigurationChanged(newConfig)
            val currentDisplaySize = calculateCurrentDisplayWidthAndHeightDp(currentActivity)
            if (
              WidthSizeClass.compute(currentDisplaySize.first) == widthDisplaySize &&
                HeightSizeClass.compute(currentDisplaySize.second) == heightDisplaySize
            ) {
              latch.countDown()
            }
          }
        }
      val container: ViewGroup =
        currentActivity.getWindow().findViewById(android.R.id.content) as ViewGroup
      currentActivity.runOnUiThread { container.addView(currentActivityView) }

      val widthDp = WidthSizeClass.getWidthDpInSizeClass(widthDisplaySize)
      val heightDp = HeightSizeClass.getHeightDpInSizeClass(heightDisplaySize)

      executeShellCommand("wm size ${widthDp}dpx${heightDp}dp")

      latch.await(5, TimeUnit.SECONDS)
      currentActivity.runOnUiThread { container.removeView(currentActivityView) }

      val finalSize = calculateCurrentDisplayWidthAndHeightDp(currentActivity)
      if (
        WidthSizeClass.compute(finalSize.first) != widthDisplaySize ||
          HeightSizeClass.compute(finalSize.second) != heightDisplaySize
      ) {
        // Display could not be set to the requested size, reset to starting size
        executeShellCommand("wm size ${startingWidth}dpx${startingHeight}dp")
        throw UnsupportedDeviceOperationException(
          "Device could not be set to the requested display size."
        )
      }
    } else {
      throw DeviceControllerOperationException(
        "Device could not be set to the requested display size because there are no activities in" +
          " the resumed stage."
      )
    }
  }

  private fun calculateCurrentDisplayWidthAndHeightDp(activity: Activity): Pair<Int, Int> {
    val displayPx = calculateCurrentDisplayWidthAndHeightPx()
    val widthDp = (displayPx.first / activity.getResources().displayMetrics.density).roundToInt()
    val heightDp = (displayPx.second / activity.getResources().displayMetrics.density).roundToInt()
    return Pair(widthDp, heightDp)
  }

  companion object {
    private val TAG = DisplaySizeAction::class.java.simpleName
  }
}
