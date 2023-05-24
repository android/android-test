/*
 * Copyright (C) 2023 The Android Open Source Project
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

import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout

/**
 * Server that handles requests from the ShellCommandFileObserverClient.
 *
 * This server should be easily callable from Java.
 *
 * On API 28, System.getProperty("java.io.tmpdir") returns "/tmp", which does not exist on a
 * standard emulator! /data/local/tmp seems to be a reliable location.
 */
final class ShellCommandFileObserverExecutorServer
@JvmOverloads
constructor(
  private val commonDir: File = File("/data/local/tmp"),
  private val scope: CoroutineScope =
    CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())
) {
  @JvmField val exchangeDir: File

  private lateinit var server: Server
  private lateinit var serverJob: Job

  init {
    exchangeDir = FileObserverProtocol.createExchangeDir(commonDir)
  }

  fun start() {
    server = Server(exchangeDir)
    runBlocking {
      serverJob = scope.launch { server.run() }
      server.waitForReady()
    }
  }

  fun stop() {
    server.stop()
    runBlocking { serverJob.join() }
    try {
      exchangeDir.delete()
    } catch (x: IOException) {
      Log.e(TAG, "Couldn't delete $exchangeDir", x)
    }
  }

  private final inner class Server(directory: File) : CoroutineFileObserver(directory) {

    private val ready: Semaphore = Semaphore(1, 1)

    init {
      // Uncomment this line to see the event-level chatter.
      // logLevel = Log.INFO
      logTag = "${TAG}.Server"
    }

    override fun onWatching() {
      ready.release()
    }

    suspend fun waitForReady() {
      ready.acquire()
    }

    override suspend fun onCloseWrite(file: File) {
      super.onCloseWrite(file)
      if (!FileObserverProtocol.isRequestFile(file)) return
      if (file.isDirectory()) {
        Log.w(logTag, "$file is a directory")
        return
      }
      if (!file.canRead()) {
        Log.w(logTag, "$file cannot be read")
        return
      }
      coroutineScope { launch { handleCommand(file) } }
    }

    suspend fun handleCommand(request: File) {
      val response = FileObserverProtocol.calculateResponseFile(request)
      val command: Messages.Command
      try {
        command = FileObserverProtocol.readRequestFile(request)
      } catch (x: IOException) {
        Log.e(logTag, "Couldn't parse command in $request", x)
        FileObserverProtocol.writeResponseFile(
          response,
          Messages.CommandResult(
            resultType = Messages.ResultType.SERVER_ERROR,
            stderr = x.toByteArray()
          )
        )
        return
      }

      val process: Process
      try {
        val argv = mutableListOf<String>()
        if (command.executeThroughShell) argv.addAll(listOf("sh", "-c"))
        argv.add(command.command)
        argv.addAll(command.parameters)

        val pb = ProcessBuilder(argv)
        pb.environment().putAll(command.shellEnv)
        pb.redirectErrorStream(command.redirectErrorStream)

        process = pb.start()
        process.outputStream.close()

        val exitCode =
          process.onTimeout(command.timeoutMs) {
            // The input streams are not yet closed, so don't try reading to EOF. Instead, read all
            // available bytes. (Calling process.destroy() first causes InputStream.available()
            // to throw "java.io.IOException: Stream closed", so doing the read after the destroy
            // won't work.)
            FileObserverProtocol.writeResponseFile(
              response,
              Messages.CommandResult(
                resultType = Messages.ResultType.TIMED_OUT,
                stdout = process.inputStream.availableToByteArray(),
                stderr =
                  if (!command.redirectErrorStream) {
                    process.errorStream.availableToByteArray()
                  } else {
                    ByteArray(0)
                  }
              )
            )
          }

        if (exitCode < 0) return // timed out

        FileObserverProtocol.writeResponseFile(
          response,
          Messages.CommandResult(
            resultType = Messages.ResultType.EXITED,
            exitCode,
            stdout = process.inputStream.readBytes(),
            stderr =
              if (!command.redirectErrorStream) process.errorStream.readBytes() else ByteArray(0)
          )
        )
      } catch (x: Exception) {
        FileObserverProtocol.writeResponseFile(
          response,
          Messages.CommandResult(
            resultType = Messages.ResultType.SERVER_ERROR,
            stderr = x.toByteArray()
          )
        )
      }
    }
  }

  /** Hide API differences in handling timeouts. */
  private fun Process.onTimeout(timeout: Long, onTimeout: () -> Unit): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!waitFor(timeout, TimeUnit.MILLISECONDS)) {
        onTimeout.invoke()
        destroy()
        return -1
      }
      return exitValue()
    } else {
      var exitCode: Int = -1
      runBlocking {
        try {
          val job = scope.async { waitFor() }
          withTimeout(timeout) { exitCode = job.await() }
        } catch (e: TimeoutCancellationException) {
          onTimeout.invoke()
          destroy()
        }
      }
      return exitCode
    }
  }

  /** Use this instead of ByteString.readFrom when the stream has not yet been closed. */
  private fun InputStream.availableToByteArray(): ByteArray {
    val expected = available()
    if (expected == 0) return ByteArray(0)
    val bytes = ByteArray(expected)
    val amount = read(bytes)
    return bytes.sliceArray(0..amount - 1)
  }

  private companion object {
    const val TAG = "SCFOES"
  }
}
