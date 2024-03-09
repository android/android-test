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

package androidx.test.espresso.device.action

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.ActivityInfo.CONFIG_ORIENTATION
import android.content.res.Configuration
import android.util.Log
import androidx.test.espresso.device.common.getAccelerometerRotationSetting
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.getResumedActivityOrNull
import androidx.test.espresso.device.common.isConfigurationChangeHandled
import androidx.test.espresso.device.common.isRobolectricTest
import androidx.test.espresso.device.common.setAccelerometerRotationSetting
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getMapOfDeviceStateNamesToIdentifiers
import androidx.test.platform.device.UnsupportedDeviceOperationException

/** Action to set the test device to the provided screen orientation. */
internal class ScreenOrientationAction(val screenOrientation: ScreenOrientation) : DeviceAction {

  /**
   * Performs a screen rotation to the provided orientation.
   *
   * <p>If the device is already in the requested orientation, it's a no-op. Otherwise, the device
   * will be set to the provided orientation.
   *
   * <p>Note, this method takes care of synchronization with the device and the app/activity under
   * test after rotating the screen. Specifically, <ul>
   * <li>if no activity is found in a RESUMED state, this method waits for the application's
   *   orientation to change to the requested orientation. </li>
   * <li>if the activity handles device orientation change,this method waits for the application's
   *   orientation to change to the requested orientation, and relies on Espresso's {@code onView()}
   *   method to ensure it synchronizes properly with the updated activity. </li>
   * <li>if the activity doesn't handle device orientation change, it waits until the activity is
   *   PAUSED and relies on Espresso's {@code onView()} method to ensure it synchronizes properly
   *   with the recreated activity. </li>
   *
   * </ul>
   *
   * @param deviceController the controller to use to interact with the device.
   */
  override fun perform(deviceController: DeviceController) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (screenOrientation == getCurrentScreenOrientation(context)) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Device screen is already in the requested orientation, no need to rotate.")
      }
      return
    }

    if (isRobolectricTest()) {
      deviceController.setScreenOrientation(screenOrientation.getOrientation())
      return
    }
    
    // TODO(b/296910911) Support setting screen orientation on folded devices
    val supportedDeviceStates = getMapOfDeviceStateNamesToIdentifiers()
    if (supportedDeviceStates.isNotEmpty()) {
      val currentDeviceStateIdentifier = executeShellCommand("cmd device_state print-state").trim()
      if (currentDeviceStateIdentifier != getMapOfDeviceStateNamesToIdentifiers().get("OPENED")) {
        throw UnsupportedDeviceOperationException(
          "Setting screen orientation is not suported on foldable devices that are not in flat mode."
        )
      }
    }

    val currentActivity = getResumedActivityOrNull()
    if (currentActivity == null) {
      Log.d(TAG, "No activity was found in the RESUMED stage.")
      throw DeviceControllerOperationException(
        "Device could not be set to the requested screen orientation because no activity was found."
      )
    }

    var startingAccelRotationSetting = getAccelerometerRotationSetting()
    val currentActivityName: String? = currentActivity.localClassName
    val configChangesHandled = currentActivity.isConfigurationChangeHandled(CONFIG_ORIENTATION)
    val latch: CountDownLatch = CountDownLatch(1)
    val requestedOrientation =
      if (screenOrientation == ScreenOrientation.LANDSCAPE) Configuration.ORIENTATION_LANDSCAPE
      else Configuration.ORIENTATION_PORTRAIT

    if (configChangesHandled) {
      Log.d(TAG, "The current activity handles configuration changes.")
      context.registerComponentCallbacks(
        object : ComponentCallbacks {
          override fun onConfigurationChanged(newConfig: Configuration) {
            if (newConfig.orientation == requestedOrientation) {
              if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Application's orientation was set to the requested orientation.")
              }
              context.unregisterComponentCallbacks(this)
              latch.countDown()
            }
          }

          override fun onLowMemory() {}
        }
      )
    } else {
      Log.d(
        TAG,
        "The current activity does not handle configuration changes and will be recreated when " +
          "its orientation changes."
      )
      ActivityLifecycleMonitorRegistry.getInstance()
        .addLifecycleCallback(
          object : ActivityLifecycleCallback {
            override fun onActivityLifecycleChanged(activity: Activity, stage: Stage) {
              if (
                activity.localClassName == currentActivityName &&
                  stage == Stage.RESUMED &&
                  activity.resources.configuration.orientation == requestedOrientation
              ) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "Test activity was resumed in the requested orientation.")
                }
                ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this)
                latch.countDown()
              }
            }
          }
        )
    }
    deviceController.setScreenOrientation(screenOrientation.getOrientation())
    latch.await(5, TimeUnit.SECONDS)

    // Restore accelerometer rotation setting if it was changed
    if (
      getDeviceApiLevel() >= 21 && startingAccelRotationSetting != getAccelerometerRotationSetting()
    ) {
      setAccelerometerRotationSetting(startingAccelRotationSetting)
    }

    if (getCurrentScreenOrientation(context) != screenOrientation) {
      throw DeviceControllerOperationException(
        "Device could not be set to the requested screen orientation."
      )
    }
  }

  private fun getCurrentScreenOrientation(context: Context): ScreenOrientation {
    var currentOrientation = context.resources.configuration.orientation
    return if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
      ScreenOrientation.LANDSCAPE
    else ScreenOrientation.PORTRAIT
  }

  companion object {
    private val TAG = ScreenOrientationAction::class.java.simpleName
  }
}
