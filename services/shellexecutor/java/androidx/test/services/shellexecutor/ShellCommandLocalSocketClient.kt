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
import android.os.Build
import android.util.Log
import androidx.test.services.shellexecutor.LocalSocketProtocol.readResponse
import androidx.test.services.shellexecutor.LocalSocketProtocol.sendRequest
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout

/**
 * Client that sends requests to the ShellCommandLocalSocketExecutorServer.
 *
 * This client is designed to be callable from Java.
 */
class ShellCommandLocalSocketClient(private val address: LocalSocketAddress) {
  private lateinit var socket: LocalSocket

  /** Composes a request and sends it to the server, and streams the resulting output. */
  fun request(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeout: Duration,
  ): InputStream {
    if (command == null || command.isEmpty()) {
      throw IllegalArgumentException("Null or empty command")
    }

    lateinit var result: InputStream

    // The call to runBlocking causes Android to emit "art: Note: end time exceeds epoch:". This is
    // in InitTimeSpec in runtime/utils.cc. I don't see a way to invoke it in such a way that it
    // doesn't clutter the logcat.
    runBlocking(scope.coroutineContext) {
      withTimeout(timeout) {
        runInterruptible {
          socket = LocalSocket(LocalSocket.SOCKET_STREAM)
          // While there *is* a timeout option on connect(), in the Android source, it throws
          // UnsupportedOperationException! So we leave the timeout up to withTimeout +
          // runInterruptible. Capture the time taken to connect so we can subtract it from the
          // overall timeout. (Calling socket.setSoTimeout() before connect() throws IOException
          // "socket not created".)
          val connectTime = measureTime { socket.connect(address) }

          val argv = mutableListOf<String>()
          if (executeThroughShell) {
            argv.addAll(listOf("sh", "-c"))
            argv.add((listOf(command) + (parameters ?: emptyList())).joinToString(" "))
          } else {
            argv.add(command)
            parameters?.let { argv.addAll(it) }
          }

          socket.sendRequest(argv, shellEnv, timeout - connectTime)
          socket.shutdownOutput()

          // We read responses off the socket, write buffers to the pipe, and close the pipe when we
          // get an exit code. The existing ShellExecutor API doesn't provide for *returning* that
          // exit code, but it's useful as a way to know when to close the stream. By using the pipe
          // as an intermediary, we can respond to exceptions sensibly.
          val upstream = PipedOutputStream()
          val downstream = PipedInputStream(upstream)

          scope.launch {
            try {
              socket.inputStream.use {
                while (true) {
                  val response = socket.readResponse()
                  if (response == null) {
                    if (socket.fileDescriptor.valid()) {
                      delay(1.milliseconds)
                      continue
                    } else {
                      Log.w(TAG, "Unexpected EOF on LocalSocket for ${argv[0]}!")
                      break
                    }
                  }
                  if (response.hasBuffer()) response.buffer.writeTo(upstream)
                  if (response.hasExitCode()) {
                    Log.i(TAG, "Process ${argv[0]} exited with code ${response.exitCode}")
                    break
                  }
                }
              }
            } catch (x: IOException) {
              if (x.isPipeClosed()) {
                Log.i(TAG, "LocalSocket relay for ${argv[0]} closed early")
              } else {
                Log.w(TAG, "LocalSocket relay for ${argv[0]} failed", x)
              }
            } finally {
              upstream.flush()
              upstream.close()
            }
          }

          result = downstream
        }
      }
    }
    return result
  }

  /** Java-friendly wrapper for the above. */
  fun request(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeout: java.time.Duration,
  ): InputStream =
    request(command, parameters, shellEnv, executeThroughShell, timeout.toKotlinDuration())

  private companion object {
    private const val TAG = "SCLSClient" // up to 23 characters

    // Keep this around for all clients; if you create a new one with every object, you can wind up
    // running out of threads.
    private val scope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())
  }
}

// Sadly, the only way to distinguish the downstream pipe being closed is the text
// of the exception thrown when you try to write to it. Which varies by API level.
private fun IOException.isPipeClosed() =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    message.equals("Pipe closed")
  } else {
    message.equals("Pipe is closed")
  }
