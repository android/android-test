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

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import com.android.emulator.control.EmulatorControllerGrpc
import io.grpc.CallCredentials
import io.grpc.Channel
import io.grpc.ChannelCredentials
import io.grpc.InsecureChannelCredentials
import io.grpc.ManagedChannel
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import io.grpc.stub.AbstractBlockingStub
import io.grpc.stub.AbstractFutureStub
import java.io.File

/**
 * A GrpcConnectionProvider can provide a configured gRPC stub to a remote service.
 *
 * This class is able to produce fully configured channels, and call credential objects that can be
 * used for authenticating to the gRPC endpoint. Usually the provider is used to obtain a
 * conncection to the emulator in which this test is running.
 *
 * If a certificate chain is present, a tls channel will be used.
 *
 * @param address The address where this emulator can be reached. Use 10.0.2.2 for the loopback
 *   device where the emulator is running.
 * @param port The port of the gRPC endpoint.
 * @param token JWT token used for authentication. If the token is present and not empty it will be
 *   used to set the x-grpc-authorization header.
 * @param clientCertChainFilePath A PEM-encoded certificate chain.
 * @param clientPrivateKeyFilePath An unencrypted PKCS#8 key (file headers have "BEGIN CERTIFICATE"
 *   and "BEGIN PRIVATE KEY").
 * @param trustCertCollectionFilePath When present Use the provided root certificates to verify the
 *   server's identity instead of the system's default. Generally they should be PEM-encoded with
 *   all the certificates concatenated together (file header has "BEGIN CERTIFICATE", and would
 *   occur once per certificate).
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
class EmulatorGrpcConnImpl
constructor(
  private val address: String,
  private val port: Int,
  private val token: String,
  private val clientCertChainFilePath: String,
  private val clientPrivateKeyFilePath: String,
  private val trustCertCollectionFilePath: String,
) : EmulatorGrpcConn {

  val channelCredentials = channelCredentials()
  val callCredentials =
    if (token.isNullOrEmpty()) NoCallCredentials()
    else HeaderCallCredentials(AUTH_HEADER, BEARER + token)

  companion object {
    private val AUTH_HEADER = "authorization"
    private val BEARER = "Bearer "
  }

  override fun channel(): ManagedChannel {
    return OkHttpChannelBuilder.forAddress(address, port, channelCredentials).build()
  }

  override fun credentials(): CallCredentials {
    return callCredentials
  }

  override fun emulatorController(): EmulatorControllerGrpc.EmulatorControllerBlockingStub {
    return emulatorBlockingStub(EmulatorControllerGrpc::newBlockingStub)!!
  }

  override fun <T : AbstractBlockingStub<T>?> emulatorBlockingStub(builder: (Channel) -> T): T {
    return builder(channel())!!.withCallCredentials(credentials())
  }

  override fun <T : AbstractFutureStub<T>?> emulatorFutureStub(builder: (Channel) -> T): T {
    return builder(channel())!!.withCallCredentials(credentials())
  }

  private fun channelCredentials(): ChannelCredentials {
    if (
      exists(clientCertChainFilePath) &&
        exists(trustCertCollectionFilePath) &&
        exists(clientPrivateKeyFilePath)
    ) {
      // TLS
      val builder = TlsChannelCredentials.newBuilder()
      builder.keyManager(File(clientCertChainFilePath), File(clientPrivateKeyFilePath))
      builder.trustManager(File(trustCertCollectionFilePath))
      return builder.build()
    }

    return InsecureChannelCredentials.create()
  }

  /** Returns true if the path is non empty and exists */
  private fun exists(path: String?): Boolean {
    if (path.isNullOrEmpty()) return false
    return File(path).exists()
  }
}
