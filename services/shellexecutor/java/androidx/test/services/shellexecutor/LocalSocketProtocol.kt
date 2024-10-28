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
  /**
   * Composes a RunCommandRequest and sends it over the LocalSocket.
   *
   * @param secret The secret to authenticate the request.
   * @param argv The argv of the command line to run.
   * @param env The environment variables to provide to the process.
   * @param timeout The timeout for the command; infinite or nonpositive values mean no timeout.
   */
  fun LocalSocket.sendRequest(
    secret: String,
    argv: List<String>,
    env: Map<String, String>? = null,
    timeout: Duration,
  ) {
    val builder = RunCommandRequest.newBuilder().setSecret(secret).addAllArgv(argv)
    env?.forEach { (k, v) -> builder.putEnvironment(k, v) }
    if (timeout.isInfinite() || timeout.isNegative() || timeout == Duration.ZERO) {
      builder.setTimeoutMs(0) // <= 0 means no timeout
    } else {
      builder.setTimeoutMs(timeout.inWholeMilliseconds)
    }
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
    // Since we're currently stuck on a version of protobuf where we don't have hasExitCode(), we
    // use a magic value to indicate that exitCode is not set. When we upgrade to a newer version
    // of protobuf, we can obsolete this.
    if (exitCode != null) {
      builder.exitCode = exitCode
    } else {
      builder.exitCode = HAS_NOT_EXITED
    }

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
   * Is this the end of the stream?
   *
   * Once we upgrade to a newer version of protobuf, we can switch to hasExitCode().
   */
  fun RunCommandResponse.hasExited() = exitCode != HAS_NOT_EXITED

  /**
   * Builds a "binder key", given the server address and secret. (We are not actually using a Binder
   * here, but the ShellExecutor interface calls the secret for connecting client to server a
   * "binder key", so we stick with that nomenclature.) Binder keys should be opaque outside
   * this directory.
   *
   * The address can contain spaces, and since it gets passed through a command line, we need to
   * encode it so it doesn't get split by argv. java.net.URLEncoder is conveniently available on all
   * SDK versions.
   */
  @JvmStatic
  fun LocalSocketAddress.asBinderKey(secret: String) = buildString {
    append(":")
    append(URLEncoder.encode(name, "UTF-8")) // Will convert any : to %3A
    append(":")
    append(URLEncoder.encode(secret, "UTF-8"))
    append(":")
  }

  /** Extracts the address from a binder key. */
  @JvmStatic
  fun addressFromBinderKey(binderKey: String) =
    LocalSocketAddress(URLDecoder.decode(binderKey.split(":")[1], "UTF-8"))

  /** Extracts the secret from a binder key. */
  @JvmStatic
  fun secretFromBinderKey(binderKey: String) = URLDecoder.decode(binderKey.split(":")[2], "UTF-8")

  /** Is this a valid binder key? */
  @JvmStatic
  fun isBinderKey(maybeKey: String) =
    maybeKey.startsWith(':') && maybeKey.endsWith(':') && maybeKey.split(":").size == 4

  const val TAG = "LocalSocketProtocol"
  private const val HAS_NOT_EXITED = 0xCA7F00D
}
