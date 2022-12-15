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
package androidx.test.espresso.device.controller.emulator

import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import com.android.emulator.control.EmulatorControllerGrpc
import com.android.emulator.control.ParameterValue
import com.android.emulator.control.PhysicalModelValue
import com.android.emulator.control.PhysicalModelValue.PhysicalType
import com.android.emulator.control.Posture
import com.android.emulator.control.Posture.PostureValue
import io.grpc.StatusRuntimeException

/** Implementation of {@link DeviceController} for tests run on an Emulator. */
class EmulatorController
constructor(
  private val emulatorControllerStub: EmulatorControllerGrpc.EmulatorControllerBlockingStub
) : DeviceController {
  companion object {
    private val TAG = EmulatorController::class.java.simpleName
    private val DEGREES_TO_ROTATE_LANDSCAPE_TO_PORTRAIT = -45F
    private val DEGREES_TO_ROTATE_PORTRAIT_TO_LANDSCAPE = 90F
  }

  override fun setDeviceMode(deviceMode: Int) {
    if (
      !(deviceMode == DeviceMode.FLAT.mode ||
        deviceMode == DeviceMode.TABLETOP.mode ||
        deviceMode == DeviceMode.BOOK.mode)
    ) {
      throw UnsupportedDeviceOperationException(
        "The provided device mode is not supported on this device."
      )
    }

    val postureValue: PostureValue =
      if (deviceMode == DeviceMode.FLAT.mode) {
        PostureValue.POSTURE_OPENED
      } else {
        PostureValue.POSTURE_HALF_OPENED
      }
    val posture: Posture = Posture.newBuilder().setValue(postureValue).build()
    try {
      emulatorControllerStub.setPosture(posture)
    } catch (e: StatusRuntimeException) {
      throw DeviceControllerOperationException(
        "Failed to set device mode. Please make sure the connected Emulator is foldable.",
        e
      )
    }
  }

  override fun setScreenOrientation(orientation: Int) {
    val degreesToRotate =
      if (orientation == ScreenOrientation.LANDSCAPE.orientation) {
        DEGREES_TO_ROTATE_PORTRAIT_TO_LANDSCAPE
      } else {
        DEGREES_TO_ROTATE_LANDSCAPE_TO_PORTRAIT
      }
    val parameters =
      ParameterValue.newBuilder().addData(0F).addData(0F).addData(degreesToRotate).build()
    try {
      emulatorControllerStub.setPhysicalModel(
        PhysicalModelValue.newBuilder()
          .setTarget(PhysicalModelValue.PhysicalType.ROTATION)
          .setValue(parameters)
          .build()
      )
    } catch (e: StatusRuntimeException) {
      throw DeviceControllerOperationException(
        "Failed to set screen orientation. Status: ${e.getStatus()}.",
        e
      )
    }
  }
}
