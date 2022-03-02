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

import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.context.InstrumentationTestActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.EmulatorController
import androidx.test.espresso.device.controller.PhysicalDeviceController
import androidx.test.espresso.device.util.isTestDeviceAnEmulator
import androidx.test.internal.platform.ServiceLoaderWrapper
import androidx.test.platform.device.DeviceController
import com.android.emulator.control.EmulatorControllerGrpc
import dagger.Module
import dagger.Provides
import io.grpc.Channel
import io.grpc.InsecureChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Dagger module for DeviceController. */
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
        return EmulatorController(getEmulatorControllerStub())
      } else {
        return PhysicalDeviceController()
      }
    } else {
      return EspressoDeviceControllerAdpater(platformDeviceController)
    }
  }

  private fun getEmulatorControllerStub(): EmulatorControllerGrpc.EmulatorControllerBlockingStub {
    val clazz = Class.forName("android.os.SystemProperties")
    val getter: Method = clazz.getMethod("get", String::class.java)
    var gRpcPort = getter.invoke(clazz, "mdevx.grpc_guest_port") as String
    if (gRpcPort.isBlank()) {
      throw DeviceControllerOperationException(
        "Unable to connect to Emulator gRPC port. Please make sure the controller gRPC service is" +
          " enabled on the emulator."
      )
    }
    val port = gRpcPort.toInt()
    val channel: Channel =
      OkHttpChannelBuilder.forAddress("localhost", port, InsecureChannelCredentials.create())
        .idleTimeout(30, TimeUnit.SECONDS)
        .build()
    val emulatorControllerStub: EmulatorControllerGrpc.EmulatorControllerBlockingStub =
      EmulatorControllerGrpc.newBlockingStub(channel)
    return emulatorControllerStub
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
