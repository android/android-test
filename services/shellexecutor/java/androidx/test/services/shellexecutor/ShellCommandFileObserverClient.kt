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

import java.io.File
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

/**
 * Client that sends requests to the ShellCommandFileObserverExecutorServer.
 *
 * This client is designed to be callable from Java.
 */
public final class ShellCommandFileObserverClient {
  private val scope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

  public final fun run(secret: String, message: Messages.Command): Execution {
    val execution = Execution(File(secret), message)
    execution.start()
    return execution
  }

  public inner class Execution
  internal constructor(val exchangeDir: File, val message: Messages.Command) {
    private val messageWritten: Semaphore = Semaphore(1, 1)
    private val client = Client(exchangeDir, messageWritten, message)
    private lateinit var clientJob: Job

    internal fun start() {
      runBlocking { clientJob = scope.launch { client.run() } }
    }

    /** Blocks until the message has been written. */
    public fun waitForMessageWritten() {
      runBlocking { messageWritten.acquire() }
    }

    /** Standard method for obtaining the response. */
    public fun await(): Messages.CommandResult {
      runBlocking { clientJob.join() }
      return client.result
    }

    /** Alternative method for compatibility with methods that expect only an InputStream. */
    public fun asStream(): InputStream {
      val output = PipedOutputStream()
      val input = PipedInputStream(output)
      runBlocking {
        scope.launch {
          clientJob.join()
          output.use { it.write(client.result.stdout) }
        }
      }
      return input
    }
  }

  private inner class Client(
    val exchangeDir: File,
    val messageWritten: Semaphore,
    val message: Messages.Command
  ) : CoroutineFileObserver(exchangeDir) {
    private lateinit var response: File
    public lateinit var result: Messages.CommandResult

    init {
      // Uncomment this line to see the event-level chatter.
      // logLevel = Log.INFO
      logTag = "${TAG}.Client"
    }

    override fun onWatching() {
      // Wait to write the request file until we're sure we'll see the response.
      response = FileObserverProtocol.writeRequestFile(exchangeDir, message)
      // Make sure any interested parties are notified that we've finished creating the request.
      runBlocking { messageWritten.release() }
    }

    override suspend fun onCloseWrite(file: File) {
      super.onCloseWrite(file)
      if (file != response) return
      result = FileObserverProtocol.readResponseFile(response)
      stop()
    }
  }

  private companion object {
    const val TAG = "SCFOC"
  }
}
