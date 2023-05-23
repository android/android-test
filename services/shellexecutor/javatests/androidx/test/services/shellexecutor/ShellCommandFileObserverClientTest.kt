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
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Expect
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.io.FileNotFoundException
import java.io.FilenameFilter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ShellCommandFileObserverClientTest {
  @get:Rule final var expect = Expect.create()
  val exchangeDir =
    InstrumentationRegistry.getInstrumentation().getContext().getDir("SCFOCT", Context.MODE_PRIVATE)
  lateinit var client: ShellCommandFileObserverClient

  @Before
  fun setUp() {
    client = ShellCommandFileObserverClient()
  }

  private fun getRequestFile(): File {
    val files =
      exchangeDir.listFiles(
        object : FilenameFilter {
          override fun accept(dir: File, name: String) = name.endsWith(".request")
        }
      )
    if (files == null) throw FileNotFoundException()
    return files[0]
  }

  @Test
  fun success_await() {
    val execution =
      client.run(
        exchangeDir.toString(),
        Messages.Command(
          "command",
          listOf("parameters"),
          mapOf("name" to "value"),
          true,
          true,
          1234L
        )
      )
    execution.waitForMessageWritten()
    val requestFile = getRequestFile()
    val request = FileObserverProtocol.readRequestFile(requestFile)
    val responseFile = FileObserverProtocol.calculateResponseFile(requestFile)
    FileObserverProtocol.writeResponseFile(
      responseFile,
      Messages.CommandResult(
        Messages.ResultType.EXITED,
        123,
        "stdout".toByteArray(Charsets.UTF_8),
        "stderr".toByteArray(Charsets.UTF_8)
      )
    )

    val response = execution.await()

    expect.that(request.command).isEqualTo("command")
    expect.that(request.parameters).containsExactly("parameters")
    expect.that(request.shellEnv).containsExactlyEntriesIn(mapOf("name" to "value"))
    expect.that(request.executeThroughShell).isTrue()
    expect.that(request.redirectErrorStream).isTrue()
    expect.that(request.timeoutMs).isEqualTo(1234L)
    expect.that(response.resultType).isEqualTo(Messages.ResultType.EXITED)
    expect.that(response.stdout.toString(Charsets.UTF_8)).isEqualTo("stdout")
    expect.that(response.stderr.toString(Charsets.UTF_8)).isEqualTo("stderr")
    expect.that(response.exitCode).isEqualTo(123)
  }

  @Test
  fun success_asStream() {
    val execution = client.run(exchangeDir.toString(), Messages.Command("command", timeoutMs = 0L))
    execution.waitForMessageWritten()
    val requestFile = getRequestFile()
    FileObserverProtocol.readRequestFile(requestFile)
    val responseFile = FileObserverProtocol.calculateResponseFile(requestFile)

    FileObserverProtocol.writeResponseFile(
      responseFile,
      Messages.CommandResult(
        Messages.ResultType.EXITED,
        0,
        "foo\nbar\nbaz\n".toByteArray(Charsets.UTF_8)
      )
    )

    val output = execution.asStream().readBytes()

    assertThat(output.toString(Charsets.UTF_8)).isEqualTo("foo\nbar\nbaz\n")
  }

  @Test
  fun failure_malformedResponse() {
    val execution = client.run(exchangeDir.toString(), Messages.Command("command", timeoutMs = 0L))
    execution.waitForMessageWritten()
    val requestFile = getRequestFile()
    FileObserverProtocol.readRequestFile(requestFile)
    val responseFile = FileObserverProtocol.calculateResponseFile(requestFile)
    responseFile.writeText("Potrzebie!")
    val response = execution.await()
    expect.that(response.resultType).isEqualTo(Messages.ResultType.CLIENT_ERROR)
    expect
      .that(response.stderr.toString(Charsets.UTF_8))
      .startsWith("java.io.StreamCorruptedException")
  }
}
