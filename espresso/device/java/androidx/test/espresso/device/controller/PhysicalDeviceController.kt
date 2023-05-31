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
import android.view.Surface
import androidx.annotation.RestrictTo
import androidx.test.espresso.device.common.SettingsObserver
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Implementation of {@link DeviceController} for tests run on a physical device.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PhysicalDeviceController(private val context: Context) : DeviceController {
  override fun setDeviceMode(deviceMode: Int) {
    throw UnsupportedDeviceOperationException(
      "Setting a device mode is not supported on physical devices."
    )
  }

  override fun setScreenOrientation(screenOrientation: Int) {
    // System user_rotation values must be one of the Surface rotation constants and these values
    // can indicate different orientations on different devices, since we check if the device is
    // already in correct orientation in ScreenOrientationAction, set user_rotation to its opposite
    // here to rotate the device to the opposite orientation
    val startingUserRotation =
      executeShellCommand("settings get system user_rotation").trim().toInt()
    val userRotationToSet =
      if (
        startingUserRotation == Surface.ROTATION_0 || startingUserRotation == Surface.ROTATION_270
      ) {
        Surface.ROTATION_90
      } else {
        Surface.ROTATION_0
      }

    val settingsLatch: CountDownLatch = CountDownLatch(1)
    val thread: HandlerThread = HandlerThread("Observer_Thread")
    thread.start()
    val runnableHandler: Handler = Handler(thread.getLooper())
    val settingsObserver: SettingsObserver =
      SettingsObserver(runnableHandler, context, settingsLatch, System.USER_ROTATION)
    settingsObserver.observe()
    executeShellCommand("settings put system user_rotation ${userRotationToSet}")
    settingsLatch.await(5, TimeUnit.SECONDS)
    settingsObserver.stopObserver()
    thread.quitSafely()

    if (
      executeShellCommand("settings get system user_rotation").trim().toInt() != userRotationToSet
    ) {
      throw DeviceControllerOperationException(
        "Device could not be set to the requested screen orientation."
      )
    }
  }
}
