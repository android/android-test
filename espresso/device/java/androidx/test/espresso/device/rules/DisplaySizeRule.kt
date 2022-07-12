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

package androidx.test.espresso.device.rules

import android.app.Activity
import android.app.Instrumentation
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.setDisplaySize
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.UnsupportedDeviceOperationException
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.util.calculateCurrentDisplayWidthAndHeight
import androidx.test.espresso.device.util.getDeviceApiLevel
import androidx.test.espresso.device.util.getResumedActivityOrNull
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Restores the display size when a test case finishes.
 *
 * @param defaultWidth: the display will be set to the specified width size class, or the one that
 * the test starts with if none is given.
 * @param defaultHeight: the display will be set to the specified height size class, or the one that
 * the test starts with if none is given.
 * @param instrumentation: an instance of the Instrumentation for the test
 */
class DisplaySizeRule(
  private val defaultWidth: WidthSizeClass? = null,
  private val defaultHeight: HeightSizeClass? = null,
  private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) : TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        if (getDeviceApiLevel() < 24) {
          throw UnsupportedDeviceOperationException(
            "DisplaySizeRule is not supported on devices with APIs below 24."
          )
        }

        val currentActivity = getResumedActivityOrNull()
        if (currentActivity != null) {
          val currentDisplaySize =
            currentActivity.calculateCurrentDisplayWidthAndHeight(instrumentation)
          val startingWidth = currentDisplaySize.first
          val startingHeight = currentDisplaySize.second

          statement.evaluate()

          if (defaultWidth != null && defaultHeight != null) {
            onDevice().perform(setDisplaySize(defaultWidth, defaultHeight))
          } else if (defaultWidth != null) {
            val widthDpToRestore = WidthSizeClass.getWidthDpInSizeClass(defaultWidth)
            setDisplaySize(currentActivity, widthDpToRestore, startingHeight)
          } else if (defaultHeight != null) {
            val heightDpToRestore = HeightSizeClass.getHeightDpInSizeClass(defaultHeight)
            setDisplaySize(currentActivity, startingWidth, heightDpToRestore)
          } else {
            setDisplaySize(currentActivity, startingWidth, startingHeight)
          }
        } else {
          throw DeviceControllerOperationException(
            "Device could not be set to the requested display size because there are no activities in" +
              " the resumed stage."
          )
        }
      }
    }
  }

  private fun setDisplaySize(activity: Activity, widthDp: Int, heightDp: Int) {
    var startingDisplaySize = activity.calculateCurrentDisplayWidthAndHeight(instrumentation)
    if (widthDp == startingDisplaySize.first && heightDp == startingDisplaySize.second) {
      return
    }

    val latch: CountDownLatch = CountDownLatch(1)
    activity.runOnUiThread {
      val container: ViewGroup =
        activity.getWindow().findViewById(android.R.id.content) as ViewGroup

      container.addView(
        object : View(activity) {
          override fun onConfigurationChanged(newConfig: Configuration?) {
            super.onConfigurationChanged(newConfig)
            val displaySize = activity.calculateCurrentDisplayWidthAndHeight(instrumentation)
            if (widthDp == displaySize.first && heightDp == displaySize.second) {
              latch.countDown()
            }
          }
        }
      )
    }

    instrumentation.getUiAutomation().executeShellCommand("wm size ${widthDp}dpx${heightDp}dp")

    latch.await(2, TimeUnit.SECONDS)

    val currentDisplaySize = activity.calculateCurrentDisplayWidthAndHeight(instrumentation)
    if (widthDp != currentDisplaySize.first || heightDp != currentDisplaySize.second) {
      // Display could not be set to the requested size, reset to starting size
      instrumentation
        .getUiAutomation()
        .executeShellCommand(
          "wm size ${startingDisplaySize.first}dpx${startingDisplaySize.second}dp"
        )
      throw DeviceControllerOperationException(
        "Device could not be set to the requested display size."
      )
    }
  }
}
