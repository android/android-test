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

import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.System
import android.util.Log
import android.view.Surface
import androidx.annotation.RestrictTo
import androidx.test.espresso.device.common.AccelerometerRotation
import androidx.test.espresso.device.common.SettingsObserver
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getAccelerometerRotationSetting
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.getMapOfDeviceStateNamesToIdentifiers
import androidx.test.espresso.device.common.setAccelerometerRotationSetting
import androidx.test.platform.app.InstrumentationRegistry
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
class PhysicalDeviceController() : DeviceController {
  override fun setDeviceMode(deviceMode: Int) {
    val deviceIdentifiersMap = getMapOfDeviceStateNamesToIdentifiers()
    if (deviceMode == DeviceMode.FLAT.getMode()) {
      if (deviceIdentifiersMap.containsKey("OPENED")) {
        setDeviceState(deviceIdentifiersMap.get("OPENED")!!)
      } else {
        throw UnsupportedDeviceOperationException("Flat mode is not supported on this device.")
      }
    } else if (
      deviceMode == DeviceMode.TABLETOP.getMode() || deviceMode == DeviceMode.BOOK.getMode()
    ) {
      if (deviceIdentifiersMap.containsKey("HALF_OPENED")) {
        setDeviceState(deviceIdentifiersMap.get("HALF_OPENED")!!)
      } else {
        val deviceModeString =
          if (deviceMode == DeviceMode.TABLETOP.getMode()) "Tabletop" else "Book"
        throw UnsupportedDeviceOperationException(
          "${deviceModeString} mode is not supported on this device."
        )
      }
    } else if (deviceMode == DeviceMode.CLOSED.getMode()) {
      if (deviceIdentifiersMap.containsKey("CLOSED")) {
        setDeviceState(deviceIdentifiersMap.get("CLOSED")!!)
      } else {
        throw UnsupportedDeviceOperationException("Closed mode is not supported on this device.")
      }
    } else {
      throw UnsupportedDeviceOperationException("The requested device mode is not supported.")
    }
  }

  override fun setScreenOrientation(screenOrientation: Int) {
    // Executing shell commands requires API 21+
    if (getDeviceApiLevel() < 21) {
      throw UnsupportedDeviceOperationException(
        "Setting screen orientation is not suported on physical devices with APIs below 21."
      )
    }

    // TODO(b/296910911) Support setting screen orientation on folded devices
    val supportedDeviceStates = getMapOfDeviceStateNamesToIdentifiers()
    if (supportedDeviceStates.isNotEmpty()) {
      val currentDeviceStateIdentifier = executeShellCommand("cmd device_state print-state").trim()
      if (currentDeviceStateIdentifier != getMapOfDeviceStateNamesToIdentifiers().get("OPENED")) {
        throw UnsupportedDeviceOperationException(
          "Setting screen orientation is not suported on physical foldable devices that are not in flat mode."
        )
      }
    }

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

    // Setting screen orientation with the USER_ROTATION setting requires ACCELEROMETER_ROTATION to
    // be disabled
    if (getAccelerometerRotationSetting() != AccelerometerRotation.DISABLED) {
      Log.d(TAG, "Disabling auto-rotate.")
      setAccelerometerRotationSetting(AccelerometerRotation.DISABLED)
    }

    val settingsLatch: CountDownLatch = CountDownLatch(1)
    val thread: HandlerThread = HandlerThread("Observer_Thread")
    thread.start()
    val runnableHandler: Handler = Handler(thread.getLooper())
    val context = InstrumentationRegistry.getInstrumentation().getTargetContext()
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

  private fun setDeviceState(deviceIdentifier: String) {
    executeShellCommand("cmd device_state state ${deviceIdentifier}")
  }

  companion object {
    private val TAG = PhysicalDeviceController::class.java.simpleName
  }
}
