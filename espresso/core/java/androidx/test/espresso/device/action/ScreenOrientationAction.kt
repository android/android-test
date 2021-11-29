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

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.util.Log
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import java.util.concurrent.CountDownLatch

/** Action to set the test device to the provided screen orientation. */
internal class ScreenOrientationAction(val screenOrientation: ScreenOrientation) : DeviceAction {
  companion object {
    private val TAG = "ScreenOrientationAction"
  }

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

    deviceController.setScreenOrientation(screenOrientation.orientation)
    val latch: CountDownLatch = CountDownLatch(1)
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
    latch.await()
  }
}
