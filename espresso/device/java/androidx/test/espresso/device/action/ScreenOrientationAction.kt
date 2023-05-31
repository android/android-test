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
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.System
import android.util.Log
import androidx.test.espresso.device.common.SettingsObserver
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.getResumedActivityOrNull
import androidx.test.espresso.device.common.isConfigurationChangeHandled
import androidx.test.espresso.device.common.isRobolectricTest
import androidx.test.espresso.device.common.isTestDeviceAnEmulator
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
   * @param context the ActionContext containing the context for this application and test app.
   * @param deviceController the controller to use to interact with the device.
   */
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    if (screenOrientation == getCurrentScreenOrientation(context)) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Device screen is already in the requested orientation, no need to rotate.")
      }
      return
    }

    if (isRobolectricTest()) {
      deviceController.setScreenOrientation(screenOrientation.orientation)
      return
    }

    var oldAccelRotationSetting = getAccelerometerRotationSetting(context.applicationContext)
    // For emulators, auto-rotate must be enabled. For physical devices, it must be disabled.
    if (isTestDeviceAnEmulator() && oldAccelRotationSetting != AccelerometerRotation.ENABLED) {
      // Executing shell commands requires API 21+.
      if (getDeviceApiLevel() >= 21) {
        Log.d(TAG, "Enabling auto-rotate.")
        setAccelerometerRotation(AccelerometerRotation.ENABLED, context.applicationContext)
      } else {
        throw UnsupportedDeviceOperationException(
          "Screen orientation cannot be set on this device because auto-rotate is disabled. Please manually enable auto-rotate and try again."
        )
      }
    } else if (
      !isTestDeviceAnEmulator() && oldAccelRotationSetting != AccelerometerRotation.DISABLED
    ) {
      if (getDeviceApiLevel() >= 21) {
        Log.d(TAG, "Disabling auto-rotate.")
        setAccelerometerRotation(AccelerometerRotation.DISABLED, context.applicationContext)
      } else {
        throw UnsupportedDeviceOperationException(
          "Screen orientation cannot be set on this device because auto-rotate is enabled. Please manually disable auto-rotate and try again."
        )
      }
    }

    val currentActivity = getResumedActivityOrNull()
    val currentActivityName: String? = currentActivity?.getLocalClassName()
    val configChangesHandled =
      if (currentActivity != null) {
        currentActivity.isConfigurationChangeHandled(CONFIG_ORIENTATION)
      } else {
        false
      }

    val latch: CountDownLatch = CountDownLatch(1)
    val requestedOrientation =
      if (screenOrientation == ScreenOrientation.LANDSCAPE) Configuration.ORIENTATION_LANDSCAPE
      else Configuration.ORIENTATION_PORTRAIT

    if (currentActivity == null || configChangesHandled) {
      if (currentActivity == null) {
        Log.d(TAG, "No activity was found in the RESUMED stage.")
      } else if (configChangesHandled) {
        Log.d(TAG, "The current activity handles configuration changes.")
      }
      context.applicationContext.registerComponentCallbacks(
        object : ComponentCallbacks {
          override fun onConfigurationChanged(newConfig: Configuration) {
            if (newConfig.orientation == requestedOrientation) {
              if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Application's orientation was set to the requested orientation.")
              }
              context.applicationContext.unregisterComponentCallbacks(this)
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
                activity.getLocalClassName() == currentActivityName &&
                  stage == Stage.RESUMED &&
                  activity.getResources().getConfiguration().orientation == requestedOrientation
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
    deviceController.setScreenOrientation(screenOrientation.orientation)
    latch.await(5, TimeUnit.SECONDS)
    if (
      getDeviceApiLevel() >= 21 &&
        oldAccelRotationSetting != getAccelerometerRotationSetting(context.applicationContext)
    ) {
      setAccelerometerRotation(oldAccelRotationSetting, context.applicationContext)
    }
    if (getCurrentScreenOrientation(context) != screenOrientation) {
      throw DeviceControllerOperationException(
        "Device could not be set to the requested screen orientation."
      )
    }
  }

  private fun getAccelerometerRotationSetting(context: Context): AccelerometerRotation =
    if (System.getInt(context.getContentResolver(), System.ACCELEROMETER_ROTATION, 0) == 1) {
      AccelerometerRotation.ENABLED
    } else {
      AccelerometerRotation.DISABLED
    }

  private fun setAccelerometerRotation(
    accelerometerRotation: AccelerometerRotation,
    context: Context
  ) {
    val settingsLatch: CountDownLatch = CountDownLatch(1)
    val thread: HandlerThread = HandlerThread("Observer_Thread")
    thread.start()
    val runnableHandler: Handler = Handler(thread.getLooper())
    val settingsObserver: SettingsObserver =
      SettingsObserver(runnableHandler, context, settingsLatch, System.ACCELEROMETER_ROTATION)
    settingsObserver.observe()
    executeShellCommand("settings put system accelerometer_rotation ${accelerometerRotation.value}")
    settingsLatch.await(5, TimeUnit.SECONDS)
    settingsObserver.stopObserver()
    thread.quitSafely()

    if (
      executeShellCommand("settings get system accelerometer_rotation").trim().toInt() !=
        accelerometerRotation.value
    ) {
      throw DeviceControllerOperationException(
        "Device could not be set to the requested screen orientation."
      )
    }
  }

  private fun getCurrentScreenOrientation(context: ActionContext): ScreenOrientation {
    var currentOrientation =
      context.applicationContext.getResources().getConfiguration().orientation
    return if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
      ScreenOrientation.LANDSCAPE
    else ScreenOrientation.PORTRAIT
  }

  companion object {
    private val TAG = ScreenOrientationAction::class.java.simpleName

    private enum class AccelerometerRotation(val value: Int) {
      DISABLED(0),
      ENABLED(1)
    }
  }
}
