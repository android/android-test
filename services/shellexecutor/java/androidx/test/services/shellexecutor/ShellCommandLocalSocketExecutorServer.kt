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

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.Process as AndroidProcess
import android.util.Log
import androidx.test.services.shellexecutor.LocalSocketProtocol.asBinderKey
import androidx.test.services.shellexecutor.LocalSocketProtocol.readRequest
import androidx.test.services.shellexecutor.LocalSocketProtocol.sendResponse
import androidx.test.services.shellexecutor.LocalSocketProtocolProto.RunCommandRequest
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout

/** Server that run shell commands for a client talking over a LocalSocket. */
final class ShellCommandLocalSocketExecutorServer
@JvmOverloads
constructor(
  private val scope: CoroutineScope =
    CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())
) {
  lateinit var socket: LocalServerSocket
  lateinit var address: LocalSocketAddress
  // Since LocalServerSocket.accept() has to be interrupted, we keep that in its own Job...
  lateinit var serverJob: Job
  // ...while all the child jobs are under a single SupervisorJob that we can join later.
  val shellJobs = SupervisorJob()
  val running = AtomicBoolean(true)

  /** Returns the binder key to pass to client processes. */
  fun binderKey(): String {
    // The address can contain spaces, and since it gets passed through a command line, we need to
    // encode it. java.net.URLEncoder is conveniently available in all SDK versions.
    return address.asBinderKey()
  }

  /** Runs a simple server. */
  private suspend fun server() = coroutineScope {
    while (running.get()) {
      val connection =
        try {
          runInterruptible { socket.accept() }
        } catch (x: Exception) {
          // None of my tests have managed to trigger this one.
          Log.e(TAG, "LocalServerSocket.accept() failed", x)
          break
        }
      launch(scope.coroutineContext + shellJobs) { handleConnection(connection) }
    }
  }

  /**
   * Relays the output of process to connection with a series of RunCommandResponses.
   *
   * @param process The process to relay output from.
   * @param connection The connection to relay output to.
   * @return false if there was a problem, true otherwise.
   */
  private suspend fun relay(process: Process, connection: LocalSocket): Boolean {
    val buffer = ByteArray(4096)
    var size: Int

    // LocalSocket.isOutputShutdown() throws UnsupportedOperationException, so we can't use
    // that as our loop constraint.
    while (true) {
      try {
        size = runInterruptible { process.inputStream.read(buffer) }
        if (size < 0) return true // EOF
        if (size == 0) {
          delay(1.milliseconds)
          continue
        }
      } catch (x: InterruptedIOException) {
        // We start getting these at API 24 when the timeout handling kicks in.
        Log.i(TAG, "Interrupted while reading from ${process}: ${x.message}")
        return false
      } catch (x: IOException) {
        Log.i(TAG, "Error reading from ${process}; did it time out?", x)
        return false
      }

      if (!connection.sendResponse(buffer = buffer, size = size)) {
        return false
      }
    }
  }

  /** Handle one connection. */
  private suspend fun handleConnection(connection: LocalSocket) {
    // connection.localSocketAddress is always null, so no point in logging it.

    // Close the connection when done.
    connection.use {
      val request = connection.readRequest()
      val timeout = request.timeoutMs.milliseconds

      val pb = request.toProcessBuilder()
      pb.redirectErrorStream(true)

      val process: Process
      try {
        process = pb.start()
      } catch (x: IOException) {
        Log.e(TAG, "Failed to start process", x)
        connection.sendResponse(
          buffer = x.stackTraceToString().toByteArray(),
          exitCode = EXIT_CODE_FAILED_TO_START,
        )
        return
      }

      // We will not be writing anything to the process' stdin.
      process.outputStream.close()

      // Close the process' stdout when we're done reading.
      process.inputStream.use {
        // Launch a coroutine to relay the process' output to the client. If it times out, kill the
        // process and cancel the job. This is more coroutine-friendly than using waitFor() to
        // handle timeouts.
        val ioJob = scope.async { relay(process, connection) }

        try {
          withTimeout(timeout) {
            if (!ioJob.await()) {
              Log.w(TAG, "Relaying ${process} output failed")
            }
            runInterruptible { process.waitFor() }
          }
        } catch (x: TimeoutCancellationException) {
          Log.e(TAG, "Process ${process} timed out after $timeout")
          process.destroy()
          ioJob.cancel()
          connection.sendResponse(exitCode = EXIT_CODE_TIMED_OUT)
          return
        }

        connection.sendResponse(exitCode = process.exitValue())
      }
    }
  }

  /** Starts the server. */
  fun start() {
    socket = LocalServerSocket("androidx.test.services ${AndroidProcess.myPid()}")
    address = socket.localSocketAddress
    Log.i(TAG, "Starting server on ${address.name}")

    // Launch a coroutine to call socket.accept()
    serverJob = scope.launch { server() }
  }

  /** Stops the server. */
  fun stop(timeout: Duration) {
    running.set(false)
    // Closing the socket does not interrupt accept()...
    socket.close()
    runBlocking(scope.coroutineContext) {
      try {
        // ...so we simply cancel that job...
        serverJob.cancel()
        // ...and play nicely with all the shell jobs underneath.
        withTimeout(timeout) {
          shellJobs.complete()
          shellJobs.join()
        }
      } catch (x: TimeoutCancellationException) {
        Log.w(TAG, "Shell jobs did not stop after $timeout", x)
        shellJobs.cancel()
      }
    }
  }

  /**
   * Sets up a ProcessBuilder with information from the request; other configuration is up to the
   * caller.
   */
  private fun RunCommandRequest.toProcessBuilder(): ProcessBuilder {
    val pb = ProcessBuilder(argvList)
    val timeout = timeoutMs.milliseconds // Kotlin Durations are very readable when stringified
    Log.i(TAG, "Command to execute: [${argvList.joinToString("] [")}] within $timeout")
    if (environmentMap.isNotEmpty()) {
      pb.environment().putAll(environmentMap)
      val env = environmentMap.entries.map { (k, v) -> "$k=$v" }.joinToString(", ")
      Log.i(TAG, "Environment: $env")
    }
    return pb
  }

  private companion object {
    const val TAG = "SCLSEServer" // up to 23 characters

    const val EXIT_CODE_FAILED_TO_START = -1
    const val EXIT_CODE_TIMED_OUT = -2
  }
}
