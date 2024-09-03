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

import android.Manifest.permission
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Process
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.common.AccelerometerRotation
import androidx.test.espresso.device.common.getAccelerometerRotationSetting
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.setAccelerometerRotationSetting
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import com.android.emulator.control.EmulatorControllerGrpc
import com.android.emulator.control.ParameterValue
import com.android.emulator.control.PhysicalModelValue
import com.android.emulator.control.Posture
import com.android.emulator.control.Posture.PostureValue
import io.grpc.StatusRuntimeException

/**
 * Implementation of {@link DeviceController} for tests run on an Emulator.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
class EmulatorController
constructor(
  private val emulatorControllerStub: EmulatorControllerGrpc.EmulatorControllerBlockingStub
) : DeviceController {
  companion object {
    private val TAG = EmulatorController::class.java.simpleName
    private const val DEGREES_TO_ROTATE_LANDSCAPE_TO_PORTRAIT = -90F
    private const val DEGREES_TO_ROTATE_PORTRAIT_TO_LANDSCAPE = 90F
    private const val additionalSetUpInstructions =
      " See https://developer.android.com/studio/test/espresso-api#set_up_your_project_for_the_espresso_device_api for set up instructions."
  }

  override fun setDeviceMode(deviceMode: Int) {
    if (
      !(deviceMode == DeviceMode.FLAT.getMode() ||
        deviceMode == DeviceMode.TABLETOP.getMode() ||
        deviceMode == DeviceMode.BOOK.getMode() ||
        deviceMode == DeviceMode.CLOSED.getMode())
    ) {
      throw UnsupportedDeviceOperationException(
        "The provided device mode is not supported on this device."
      )
    }

    val postureValue: PostureValue =
      if (deviceMode == DeviceMode.FLAT.getMode()) {
        PostureValue.POSTURE_OPENED
      } else if (deviceMode == DeviceMode.CLOSED.getMode()) {
        PostureValue.POSTURE_CLOSED
      } else {
        PostureValue.POSTURE_HALF_OPENED
      }
    val posture: Posture = Posture.newBuilder().setValue(postureValue).build()
    checkInternetPermission()
    try {
      emulatorControllerStub.setPosture(posture)
    } catch (e: StatusRuntimeException) {
      throw DeviceControllerOperationException(
        "Failed to set device mode. Please make sure the connected Emulator is foldable, the Android Emulator version" +
          " is updated to 33.1.11+, and the controller gRPC service is enabled on the emulator" +
          additionalSetUpInstructions,
        e,
      )
    }
  }

  override fun setScreenOrientation(orientation: Int) {
    // Enable auto-rotate if it is disabled
    if (getAccelerometerRotationSetting() != AccelerometerRotation.ENABLED) {
      // Executing shell commands requires API 21+.
      if (getDeviceApiLevel() >= 21) {
        Log.d(TAG, "Enabling auto-rotate.")
        setAccelerometerRotationSetting(AccelerometerRotation.ENABLED)
      } else {
        throw UnsupportedDeviceOperationException(
          "Screen orientation cannot be set on this device because auto-rotate is disabled. Please manually enable auto-rotate and try again."
        )
      }
    }

    checkInternetPermission()

    try {
      val physicalModelValue: PhysicalModelValue =
        emulatorControllerStub.getPhysicalModel(
          PhysicalModelValue.newBuilder()
            .setTarget(PhysicalModelValue.PhysicalType.ROTATION)
            .build()
        )
      val rotation: ParameterValue = physicalModelValue.getValue()
      val startingRotationDegrees = rotation.getDataList()[2].toFloat()
      var degreesToRotate =
        if (orientation == ScreenOrientation.PORTRAIT.getOrientation()) {
          DEGREES_TO_ROTATE_LANDSCAPE_TO_PORTRAIT + startingRotationDegrees
        } else {
          DEGREES_TO_ROTATE_PORTRAIT_TO_LANDSCAPE - startingRotationDegrees
        }
      val parameters =
        ParameterValue.newBuilder().addData(0F).addData(0F).addData(degreesToRotate).build()
      emulatorControllerStub.setPhysicalModel(
        PhysicalModelValue.newBuilder()
          .setTarget(PhysicalModelValue.PhysicalType.ROTATION)
          .setValue(parameters)
          .build()
      )
    } catch (e: StatusRuntimeException) {
      throw DeviceControllerOperationException(
        "Failed to set screen orientation. Please make sure the Android Emulator version" +
          " is updated to 33.1.11+ and the controller gRPC service is enabled on the emulator." +
          additionalSetUpInstructions,
        e,
      )
    }
  }

  /**
   * Making a connection to the Emulator GRPC requires the INTERNET permission. This method checks
   * if the current process has the permission, and if not, throws a meaninful error message.
   */
  private fun checkInternetPermission() {
    if (
      getInstrumentation()
        .getTargetContext()
        .checkPermission(permission.INTERNET, Process.myPid(), Process.myUid()) !=
        PERMISSION_GRANTED
    ) {
      throw DeviceControllerOperationException(
        "The current process does not have the INTERNET permission. Ensure the app-under-test has '<uses-permission " +
          "android:name=\"android.permission.INTERNET\"/>' in its AndroidManifest.xml." +
          additionalSetUpInstructions
      )
    }
  }
}
