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

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible

/** Variant of ShellMain that uses a LocalSocket to communicate with the client. */
class LocalSocketShellMain {

  suspend fun run(args: Array<String>): Int {
    val scope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())
    val server = ShellCommandLocalSocketExecutorServer(scope = scope)
    server.start()

    val processArgs = args.toMutableList()
    processArgs.addAll(
      processArgs.size - 1,
      listOf("-e", ShellExecSharedConstants.BINDER_KEY, server.binderKey()),
    )
    val pb = ProcessBuilder(processArgs.toList())

    val exitCode: Int

    try {
      val process = pb.start()

      val stdinCopier = scope.launch { copyStream("stdin", System.`in`, process.outputStream) }
      val stdoutCopier = scope.launch { copyStream("stdout", process.inputStream, System.out) }
      val stderrCopier = scope.launch { copyStream("stderr", process.errorStream, System.err) }

      runInterruptible { process.waitFor() }
      exitCode = process.exitValue()

      stdinCopier.cancel() // System.`in`.close() does not force input.read() to return
      stdoutCopier.join()
      stderrCopier.join()
    } finally {
      server.stop(100.milliseconds)
    }
    return exitCode
  }

  suspend fun copyStream(name: String, input: InputStream, output: OutputStream) {
    val buf = ByteArray(1024)
    try {
      while (true) {
        val size = input.read(buf)
        if (size == -1) break
        output.write(buf, 0, size)
      }
      output.flush()
    } catch (x: IOException) {
      Log.e(TAG, "IOException on $name. Terminating.", x)
    }
  }

  companion object {
    private const val TAG = "LocalSocketShellMain"

    @JvmStatic
    public fun main(args: Array<String>) {
      System.exit(runBlocking { LocalSocketShellMain().run(args) })
    }
  }
}
