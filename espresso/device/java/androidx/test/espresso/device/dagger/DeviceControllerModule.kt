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
package androidx.test.espresso.device.dagger

import android.provider.Settings
import androidx.annotation.RestrictTo
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.espresso.device.common.isTestDeviceAnEmulator
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.context.InstrumentationTestActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.PhysicalDeviceController
import androidx.test.espresso.device.controller.emulator.EmulatorController
import androidx.test.espresso.device.controller.emulator.EmulatorGrpcConn
import androidx.test.espresso.device.controller.emulator.EmulatorGrpcConnImpl
import androidx.test.internal.platform.ServiceLoaderWrapper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import dagger.Module
import dagger.Provides
import java.lang.reflect.Method
import javax.inject.Singleton

/**
 * Dagger module for DeviceController.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@Module
internal class DeviceControllerModule {

  @Provides
  @Singleton
  fun provideActionContext(): ActionContext {
    return InstrumentationTestActionContext()
  }

  @Provides
  @Singleton
  fun provideDeviceController(): DeviceController {
    val platformDeviceController: androidx.test.platform.device.DeviceController? =
      ServiceLoaderWrapper.loadSingleServiceOrNull(
        androidx.test.platform.device.DeviceController::class.java
      )
    if (platformDeviceController == null) {
      if (isTestDeviceAnEmulator()) {
        val connection = getEmulatorConnection()
        return EmulatorController(connection.emulatorController())
      } else {
        return PhysicalDeviceController(InstrumentationTestActionContext().applicationContext)
      }
    } else {
      return EspressoDeviceControllerAdpater(platformDeviceController)
    }
  }

  private fun getEmulatorConnection(): EmulatorGrpcConn {
    val args = InstrumentationRegistry.getArguments()
    var defaultEmulatorAddress = EmulatorGrpcConn.EMULATOR_ADDRESS
    var grpcPortString = args.getString(EmulatorGrpcConn.ARGS_GRPC_PORT)
    var grpcPort: Int
    if (grpcPortString != null && grpcPortString.toInt() > 0) {
      grpcPort = grpcPortString.toInt()
    } else {
      grpcPort = getEmulatorGRPCPortFromSystem()
      defaultEmulatorAddress = "localhost"
    }
    var emulatorAddressArg = args.getString(EmulatorGrpcConn.ARGS_EMULATOR_ADDRESS)
    var emulatorAddress =
      if (emulatorAddressArg != null) emulatorAddressArg else defaultEmulatorAddress

    return EmulatorGrpcConnImpl(
      emulatorAddress,
      grpcPort,
      args.getString(EmulatorGrpcConn.ARGS_GRPC_TOKEN, ""),
      args.getString(EmulatorGrpcConn.ARGS_GRPC_CER, ""),
      args.getString(EmulatorGrpcConn.ARGS_GRPC_KEY, ""),
      args.getString(EmulatorGrpcConn.ARGS_GRPC_CA, "")
    )
  }

  /**
   * Gets the default emulator gRPC port set directly in system properties, for emulators whose port
   * is configured dynamically at launch. If set, this port always implies a default emulator
   * address of 'localhost'
   */
  private fun getEmulatorGRPCPortFromSystem(): Int {
    val gRpcPort =
      if (getDeviceApiLevel() >= 17) {
        Settings.Global.getString(
          provideActionContext().applicationContext.getContentResolver(),
          "mdevx.grpc_guest_port"
        )
      } else {
        val clazz = Class.forName("android.os.SystemProperties")
        val getter: Method = clazz.getMethod("get", String::class.java)
        getter.invoke(clazz, "mdevx.grpc_guest_port") as String
      }
    if (gRpcPort.isBlank()) {
      throw DeviceControllerOperationException(
        "Unable to connect to Emulator gRPC port. Please make sure the controller gRPC service is" +
          " enabled on the emulator."
      )
    }
    return gRpcPort.toInt()
  }

  private class EspressoDeviceControllerAdpater(
    val deviceController: androidx.test.platform.device.DeviceController
  ) : DeviceController {
    override fun setDeviceMode(deviceMode: Int) {
      deviceController.setDeviceMode(deviceMode)
    }

    override fun setScreenOrientation(screenOrientation: Int) {
      deviceController.setScreenOrientation(screenOrientation)
    }
  }
}
