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
import android.content.pm.ActivityInfo.CONFIG_ORIENTATION
import android.content.res.Configuration
import android.util.Log
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import androidx.test.espresso.device.util.getResumedActivityOrNull
import androidx.test.espresso.device.util.isConfigurationChangeHandled
import androidx.test.espresso.device.util.isRobolectricTest
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.concurrent.CountDownLatch

/** Action to set the test device to the provided screen orientation. */
internal class ScreenOrientationAction(val screenOrientation: ScreenOrientation) : DeviceAction {
  companion object {
    private val TAG = "ScreenOrientationAction"
  }

  /**
   * Performs a screen rotation to the provided orientation.
   *
   * <p>If the device is already in the requested orientation, it's a no-op. Otherwise, the device
   * will be set to the provided orientation.
   *
   * <p>Note, this method takes care of synchronization with the device and the app/activity under
   * test after rotating the screen. Specifically, <ul>
   *
   * <li>if no activity is found in a RESUMED state, this method waits for the application's
   * orientation to change to the requested orientation. </li>
   *
   * <li>if the activity handles device orientation change,this method waits for the application's
   * orientation to change to the requested orientation, and relies on Espresso's {@code onView()}
   * method to ensure it synchronizes properly with the updated activity. </li>
   *
   * <li>if the activity doesn't handle device orientation change, it waits until the activity is
   * PAUSED and relies on Espresso's {@code onView()} method to ensure it synchronizes properly with
   * the recreated activity. </li>
   *
   * </ul>
   *
   * @param context the ActionContext containing the context for this application and test app.
   * @param deviceController the controller to use to interact with the device.
   */
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    var currentOrientation =
      context.applicationContext.getResources().getConfiguration().orientation
    val requestedOrientation =
      if (screenOrientation == ScreenOrientation.LANDSCAPE) Configuration.ORIENTATION_LANDSCAPE
      else Configuration.ORIENTATION_PORTRAIT
    if (currentOrientation == requestedOrientation) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Device screen is already in the requested orientation, no need to rotate.")
      }
      return
    }

    if (isRobolectricTest()) {
      deviceController.setScreenOrientation(screenOrientation.orientation)
      return
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
              if (activity.getLocalClassName() == currentActivityName && stage == Stage.PAUSED) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "Test activity was paused.")
                }
                ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this)
                latch.countDown()
              }
            }
          }
        )
    }
    deviceController.setScreenOrientation(screenOrientation.orientation)
    latch.await()
  }
}
