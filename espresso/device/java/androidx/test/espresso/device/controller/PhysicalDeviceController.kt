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

package androidx.test.espresso.device.controller

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.System
import androidx.annotation.RestrictTo
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.util.SettingsObserver
import androidx.test.espresso.device.util.executeShellCommand
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import java.util.concurrent.CountDownLatch

/**
 * Implementation of {@link DeviceController} for tests run on a physical device.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PhysicalDeviceController(private val context: Context) : DeviceController {
  override fun setDeviceMode(deviceMode: Int) {
    if (android.os.Build.MODEL != "Pixel") {
      throw UnsupportedDeviceOperationException(
        "Setting a device mode is not supported on physical devices."
      )
    }

    val requestedDeviceState: Int =
      when (deviceMode) {
        DeviceMode.CLOSED.mode -> 0 // CLOSED
        DeviceMode.TABLETOP.mode -> 1 // HALF_OPENED
        DeviceMode.BOOK.mode -> 1 // HALF_OPENED
        DeviceMode.FLAT.mode -> 2 // OPENED
        else -> -1
      }
    if (requestedDeviceState == -1) {
      throw UnsupportedDeviceOperationException(
        "The provided device mode is not supported on this device."
      )
    }
    executeShellCommand("device_state state ${requestedDeviceState}")
  }

  override fun setScreenOrientation(screenOrientation: Int) {
    // TODO(b/203092519) Investigate supporting screen orientation rotation on real devices.
    if (android.os.Build.MODEL != "Pixel") {
      throw UnsupportedDeviceOperationException(
        "Setting screen orientation is not supported on physical devices."
      )
    }

    if (screenOrientation == ScreenOrientation.PORTRAIT.orientation) {
      setUserRotation(UserRotation.PORTRAIT, context)
    } else {
      setUserRotation(UserRotation.LANDSCAPE, context)
    }
  }

  private fun setUserRotation(userRotation: UserRotation, context: Context) {
    val settingsLatch: CountDownLatch = CountDownLatch(1)
    val thread: HandlerThread = HandlerThread("Observer_Thread")
    thread.start()
    val runnableHandler: Handler = Handler(thread.getLooper())
    val settingsObserver: SettingsObserver =
      SettingsObserver(runnableHandler, context, settingsLatch, System.USER_ROTATION)
    settingsObserver.observe()
    executeShellCommand("settings put system user_rotation ${userRotation.value}")
    settingsLatch.await()
    settingsObserver.stopObserver()
    thread.quitSafely()
  }

  companion object {
    private val TAG = PhysicalDeviceController::class.java.simpleName

    private enum class UserRotation(val value: Int) {
      // TODO: look into if this is constant for all devices
      PORTRAIT(0),
      LANDSCAPE(1)
    }
  }
}
