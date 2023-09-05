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

import androidx.annotation.RestrictTo
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import androidx.test.uiautomator.UiDevice

/**
 * Implementation of {@link DeviceController} for tests run on a physical device.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PhysicalDeviceController() : DeviceController {
  override fun setDeviceMode(deviceMode: Int) {
    throw UnsupportedDeviceOperationException(
      "Setting a device mode is not supported on physical devices."
    )
  }

  override fun setScreenOrientation(screenOrientation: Int) {
    val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    uiDevice.unfreezeRotation()
    if (screenOrientation == ScreenOrientation.PORTRAIT.getOrientation()) {
      uiDevice.setOrientationPortrait()
    } else if (screenOrientation == ScreenOrientation.LANDSCAPE.getOrientation()) {
      uiDevice.setOrientationLandscape()
    }
  }

  companion object {
    private val TAG = PhysicalDeviceController::class.java.simpleName
  }
}
