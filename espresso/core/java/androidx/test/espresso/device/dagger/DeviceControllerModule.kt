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
import androidx.test.espresso.device.controller.DeviceController
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.EmulatorController
import androidx.test.espresso.device.controller.PhysicalDeviceController
import androidx.test.espresso.device.util.isTestDeviceAnEmulator
import com.android.emulator.control.EmulatorControllerGrpc
import dagger.Module
import dagger.Provides
import io.grpc.Channel
import io.grpc.InsecureChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import java.lang.reflect.Method
import javax.inject.Singleton

/** Dagger module for DeviceController. */
@Module
internal class DeviceControllerModule {

  @Provides
  @Singleton
  fun provideActionContext(): ActionContext {
    // TODO(b/203570026) Initialize ActionContext depending on whether the test is an
    // instrumentation
    // test or Robolectric
    return InstrumentationTestActionContext()
  }

  @Provides
  @Singleton
  fun provideDeviceController(): DeviceController {
    if (isTestDeviceAnEmulator()) {
      return EmulatorController(getEmulatorControllerStub())
    } else {
      return PhysicalDeviceController()
    }
  }

  private fun getEmulatorControllerStub(): EmulatorControllerGrpc.EmulatorControllerBlockingStub {
    val clazz = Class.forName("android.os.SystemProperties")
    val getter: Method = clazz.getMethod("get", String::class.java)
    var gRpcPort = getter.invoke(clazz, "mdevx.grpc_port") as String
    if (gRpcPort.isBlank()) {
      throw DeviceControllerOperationException(
        "Unable to connect to Emulator gRPC port. Please make sure the controller gRPC service is" +
          "enabled on the emulator."
      )
    }
    val port = gRpcPort.toInt()
    val channel: Channel =
      OkHttpChannelBuilder.forAddress("10.0.2.2", port, InsecureChannelCredentials.create()).build()
    val emulatorControllerStub: EmulatorControllerGrpc.EmulatorControllerBlockingStub =
      EmulatorControllerGrpc.newBlockingStub(channel)
    return emulatorControllerStub
  }
}
