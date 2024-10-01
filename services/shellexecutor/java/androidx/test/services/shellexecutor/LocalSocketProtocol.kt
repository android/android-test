/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.shellexecutor

import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.util.Log
import androidx.test.services.shellexecutor.LocalSocketProtocolProto.RunCommandRequest
import androidx.test.services.shellexecutor.LocalSocketProtocolProto.RunCommandResponse
import com.google.protobuf.ByteString
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.time.Duration

/**
 * Protocol for ShellCommandLocalSocketClient to talk to ShellCommandLocalSocketExecutorServer.
 *
 * Since androidx.test.services already includes the protobuf runtime, we aren't paying much extra
 * for adding some more protos to ship back and forth, which is vastly easier to deal with than
 * PersistableBundles (which don't even support ByteArray types).
 *
 * A conversation consists of a single RunCommandRequest from the client followed by a stream of
 * RunCommandResponses from the server; the final response has an exit code.
 */
object LocalSocketProtocol {
  /** Composes a RunCommandRequest and sends it over the LocalSocket. */
  fun LocalSocket.sendRequest(
    argv: List<String>,
    env: Map<String, String>? = null,
    timeout: Duration,
  ) {
    val builder = RunCommandRequest.newBuilder()
    builder.addAllArgv(argv)
    env?.forEach { (k, v) -> builder.putEnvironment(k, v) }
    builder.setTimeoutMs(timeout.inWholeMilliseconds)
    builder.build().writeDelimitedTo(outputStream)
  }

  /** Reads a RunCommandRequest from the LocalSocket. */
  fun LocalSocket.readRequest(): RunCommandRequest {
    return RunCommandRequest.parseDelimitedFrom(inputStream)!!
  }

  /** Composes a RunCommandResponse and sends it over the LocalSocket. */
  fun LocalSocket.sendResponse(
    buffer: ByteArray? = null,
    size: Int = 0,
    exitCode: Int? = null,
  ): Boolean {
    val builder = RunCommandResponse.newBuilder()
    buffer?.let {
      val bufferSize = if (size > 0) size else it.size
      builder.buffer = ByteString.copyFrom(it, 0, bufferSize)
    }
    exitCode?.let { builder.exitCode = it }

    try {
      builder.build().writeDelimitedTo(outputStream)
    } catch (x: IOException) {
      // Sadly, the only way to discover that the client cut the connection is an exception that
      // can only be distinguished by its text.
      if (x.message.equals("Broken pipe")) {
        Log.i(TAG, "LocalSocket stream closed early")
      } else {
        Log.w(TAG, "LocalSocket write failed", x)
      }
      return false
    }
    return true
  }

  /** Reads a RunCommandResponse from the LocalSocket. */
  fun LocalSocket.readResponse(): RunCommandResponse? {
    return RunCommandResponse.parseDelimitedFrom(inputStream)
  }

  /**
   * The address can contain spaces, and since it gets passed through a command line, we need to
   * encode it so it doesn't get split by argv. java.net.URLEncoder is conveniently available on all
   * SDK versions.
   */
  @JvmStatic fun LocalSocketAddress.asBinderKey() = ":${URLEncoder.encode(name, "UTF-8")}"

  @JvmStatic
  fun fromBinderKey(binderKey: String) =
    LocalSocketAddress(URLDecoder.decode(binderKey.trimStart(':'), "UTF-8"))

  @JvmStatic fun isBinderKey(maybeKey: String) = maybeKey.startsWith(':')

  const val TAG = "LocalSocketProtocol"
}
