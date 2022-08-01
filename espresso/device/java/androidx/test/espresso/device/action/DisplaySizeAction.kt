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

import android.app.Instrumentation
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.UnsupportedDeviceOperationException
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.util.calculateCurrentDisplayWidthAndHeight
import androidx.test.espresso.device.util.getDeviceApiLevel
import androidx.test.espresso.device.util.getResumedActivityOrNull
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
      val displaySize = currentActivity.calculateCurrentDisplayWidthAndHeight(instrumentation)
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
              val currentDisplaySize =
                currentActivity.calculateCurrentDisplayWidthAndHeight(instrumentation)
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

      val finalSize = currentActivity.calculateCurrentDisplayWidthAndHeight(instrumentation)
      if (
        WidthSizeClass.compute(finalSize.first) != widthDisplaySize ||
          HeightSizeClass.compute(finalSize.second) != heightDisplaySize
      ) {
        // Display could not be set to the requested size, reset to starting size
        instrumentation
          .getUiAutomation()
          .executeShellCommand("wm size ${startingWidth}dpx${startingHeight}dp")
        throw DeviceControllerOperationException(
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

  companion object {
    private val TAG = DisplaySizeAction::class.java.simpleName
  }
}
