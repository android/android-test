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

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Expect
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ShellCommandFileObserverExecutorServerTest {
  @get:Rule final var expect = Expect.create()
  lateinit var server: ShellCommandFileObserverExecutorServer

  @Before
  fun setUp() {
    server =
      ShellCommandFileObserverExecutorServer(
        InstrumentationRegistry.getInstrumentation()
          .getContext()
          .getDir("SCFOEST", Context.MODE_PRIVATE)
      )
    server.start()
    // The server is stopped in the test, not in tearDown(), because that's the way to guarantee
    // that the response file has been fully written and closed.
  }

  @Test
  fun success_direct() {
    val responseFile =
      FileObserverProtocol.writeRequestFile(
        server.exchangeDir,
        Messages.Command(
          command = "sh",
          parameters = listOf("-c", "echo ${'$'}{POTRZEBIE}"),
          shellEnv = mapOf("POTRZEBIE" to "furshlugginer"),
          timeoutMs = 1000L
        )
      )
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    Log.i("SCFOEST", response.toString())
    expect.that(response.resultType).isEqualTo(Messages.ResultType.EXITED)
    expect.that(response.exitCode).isEqualTo(0)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEqualTo("furshlugginer\n")
    expect.that(response.stderr.toString(Charsets.UTF_8)).isEmpty()
  }

  @Test
  fun success_executeThroughShell() {
    val responseFile =
      FileObserverProtocol.writeRequestFile(
        server.exchangeDir,
        Messages.Command(
          command = "echo ${'$'}{POTRZEBIE}",
          shellEnv = mapOf("POTRZEBIE" to "furshlugginer"),
          executeThroughShell = true,
          timeoutMs = 1000L
        )
      )
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.EXITED)
    expect.that(response.exitCode).isEqualTo(0)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEqualTo("furshlugginer\n")
    expect.that(response.stderr.toString(Charsets.UTF_8)).isEmpty()
  }

  @Test
  fun success_nonzeroExit() {
    val responseFile =
      FileObserverProtocol.writeRequestFile(
        server.exchangeDir,
        Messages.Command(command = "exit 123", executeThroughShell = true, timeoutMs = 1000L)
      )
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.EXITED)
    expect.that(response.exitCode).isEqualTo(123)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEmpty()
    expect.that(response.stderr.toString(Charsets.UTF_8)).isEmpty()
  }

  @Test
  fun timeout() {
    // Echo something then sleep for longer than the timeout. Show we get the timeout; see if we
    // get the echoed message.
    val responseFile =
      FileObserverProtocol.writeRequestFile(
        server.exchangeDir,
        Messages.Command(
          command = "echo ${'$'}{POTRZEBIE} && sleep 10",
          shellEnv = mapOf("POTRZEBIE" to "furshlugginer"),
          executeThroughShell = true,
          timeoutMs = 1000L
        )
      )
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.TIMED_OUT)
    expect.that(response.exitCode).isEqualTo(-1)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEqualTo("furshlugginer\n")
    expect.that(response.stderr.toString(Charsets.UTF_8)).isEmpty()
  }

  @Test
  fun malformed() {
    val bogus = File(server.exchangeDir, "bogus.request")
    bogus.writeText("Potrzebie!")
    val responseFile = FileObserverProtocol.calculateResponseFile(bogus)
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.SERVER_ERROR)
    expect.that(response.exitCode).isEqualTo(-1)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEmpty()
    expect
      .that(response.stderr.toString(Charsets.UTF_8))
      .startsWith("java.io.StreamCorruptedException")
  }

  @Test
  fun emptyCommand() {
    val responseFile =
      FileObserverProtocol.writeRequestFile(
        server.exchangeDir,
        Messages.Command(command = "", timeoutMs = 1000L)
      )
    while (!responseFile.exists()) Thread.sleep(10L)
    server.stop()
    val response = FileObserverProtocol.readResponseFile(responseFile)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.SERVER_ERROR)
    expect.that(response.exitCode).isEqualTo(-1)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEmpty()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      expect
        .that(response.stderr.toString(Charsets.UTF_8))
        .startsWith("java.io.IOException: Cannot run program")
    } else {
      expect
        .that(response.stderr.toString(Charsets.UTF_8))
        .startsWith("java.io.IOException: Error running exec()")
    }
  }
}
