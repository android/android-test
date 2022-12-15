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
import android.app.Instrumentation
import android.content.res.Configuration
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.util.getDeviceApiLevel
import androidx.test.espresso.device.util.getResumedActivityOrNull
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/** Action to set the test device to the provided display size. */
internal class DisplaySizeAction(
  private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
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
      val displaySize = calculateCurrentDisplay(currentActivity)
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

      currentActivity.runOnUiThread {
        val container: ViewGroup =
          currentActivity.getWindow().findViewById(android.R.id.content) as ViewGroup

        container.addView(
          object : View(currentActivity) {
            override fun onConfigurationChanged(newConfig: Configuration?) {
              super.onConfigurationChanged(newConfig)
              val currentDisplaySize = calculateCurrentDisplay(currentActivity)
              if (
                WidthSizeClass.compute(currentDisplaySize.first) == widthDisplaySize &&
                  HeightSizeClass.compute(currentDisplaySize.second) == heightDisplaySize
              ) {
                latch.countDown()
              }
            }
          }
        )
      }

      val widthDp = WidthSizeClass.getWidthDpInSizeClass(widthDisplaySize)
      val heightDp = HeightSizeClass.getHeightDpInSizeClass(heightDisplaySize)

      instrumentation.getUiAutomation().executeShellCommand("wm size ${widthDp}dpx${heightDp}dp")

      latch.await(5, TimeUnit.SECONDS)

      val finalSize = calculateCurrentDisplay(currentActivity)
      if (
        WidthSizeClass.compute(finalSize.first) != widthDisplaySize ||
          HeightSizeClass.compute(finalSize.second) != heightDisplaySize
      ) {
        // Display could not be set to the requested size, reset to starting size
        instrumentation
          .getUiAutomation()
          .executeShellCommand("wm size ${startingWidth}dpx${startingHeight}dp")
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

  private fun calculateCurrentDisplay(activity: Activity): Pair<Int, Int> {
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

    val widthDp = (widthPx / activity.getResources().displayMetrics.density).roundToInt()
    val heightDp = (heightPx / activity.getResources().displayMetrics.density).roundToInt()
    return Pair(widthDp, heightDp)
  }

  companion object {
    private val TAG = DisplaySizeAction::class.java.simpleName
  }
}
