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

import androidx.test.espresso.device.controller.DeviceControllerOperationException
import com.android.emulator.control.EmulatorControllerGrpc
import com.google.protobuf.Empty
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.grpc.CallCredentials.MetadataApplier
import io.grpc.CallCredentials.RequestInfo
import io.grpc.Metadata
import java.lang.reflect.Method
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GrpcConnectionProviderTest {
  // Note: This tests expects there is a reverse proxy running on localhost on port
  // mdevx.grpc_guest_port.
  private val EMULATOR_ADDRESS = "localhost"
  private val connection: GrpcConnectionProvider = createConnection()

  @Test
  fun canAccessAndReadGrpcHeader() {
    // A failure in this tests indicates that the gRPC endpoint is not reachable.
    val socket = Socket(InetAddress.getByName(EMULATOR_ADDRESS), getGrpcPort())
    val inputStream = socket.getInputStream()
    assertNotEquals(inputStream.read(), -1)
  }

  @Test
  fun emulatorConnection_hasCredentials() {
    assertNotNull(connection.credentials())
  }

  @Test
  fun emulatorConnection_hasChannel() {
    assertNotNull(connection.credentials())
  }

  @Test
  fun emulatorConnection_hasBlockingController() {
    assertNotNull(connection.emulatorController())
  }

  @Test
  fun emulatorConnection_canConstructFutureStub() {
    assertNotNull(connection.emulatorFutureStub(EmulatorControllerGrpc::newFutureStub))
  }

  @Test
  fun emulatorConnection_statusIsAccessible() {
    val controller = connection.emulatorController().withDeadlineAfter(2, TimeUnit.SECONDS)
    val fst = controller.getStatus(Empty.getDefaultInstance())
    assertThat("Uptime is positive", fst.uptime, greaterThan(0))

    val snd = controller.getStatus(Empty.getDefaultInstance())
    assertThat("Uptime is monotonically increasing", fst.uptime, lessThan(snd.uptime))
  }

  @Test
  fun emulatorConnection_setHeaderWithToken() {
    val testToken = "A_FAKE_TEST_TOKEN"
    val header = "authorization"
    val testConnection =
      GrpcConnectionProvider(EMULATOR_ADDRESS, getGrpcPort(), testToken, "", "", "")

    val executor = DirectExecutor()
    val applier = mock<MetadataApplier>()
    val requestinfo = mock<RequestInfo>()
    val captor = argumentCaptor<Metadata>()

    testConnection.credentials().applyRequestMetadata(requestinfo, executor, applier)
    verify(applier).apply(captor.capture())
    assertThat("The authorization header is set", captor.lastValue.keys(), hasItem(header))
    assertThat(
      "Header should contain token",
      captor.lastValue.get(Metadata.Key.of(header, Metadata.ASCII_STRING_MARSHALLER)),
      isEqualTo("Bearer " + testToken)
    )
  }

  private fun getGrpcPort(): Int {
    val clazz = Class.forName("android.os.SystemProperties")
    val getter: Method = clazz.getMethod("get", String::class.java)
    var gRpcPort = getter.invoke(clazz, "mdevx.grpc_guest_port") as String
    if (gRpcPort.isBlank()) {
      throw DeviceControllerOperationException(
        "gRPC port not found in SystemProperties, is mdevx.grpc_guest_port set?"
      )
    }
    return gRpcPort.toInt()
  }

  private fun createConnection(): GrpcConnectionProvider {
    return GrpcConnectionProvider(
      EMULATOR_ADDRESS,
      getGrpcPort(),
      "", // No token needed in G3
      "", // TLS is not used in G3
      "",
      ""
    )
  }

  class DirectExecutor : Executor {
    override fun execute(r: Runnable) {
      r.run()
    }
  }
}
