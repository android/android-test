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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID

/**
 * The protocol for communicating by FileObserver is:
 * 1. The server creates the server directory in /data/local/tmp.
 * 2. The client creates [UUID].request in the server directory.
 * 3. The server reads and deletes [UUID].request, then writes [UUID].response.
 * 4. The client reads and deletes [UUID].response.
 *
 * The underlying communication is handled by inotify, which only generates events for the
 * directories it is explicitly watching. (The FileObserver documentation makes it sound like it can
 * pick things up in subdirectories; this is erroneous.)
 *
 * The underlying directory and file are set world-readable and -writable so the client can write
 * the request and read the response. Because this only works when someone is already running
 * FileObserverShellMain, there is very little threat here; if someone is able to put a program onto
 * your test device that can watch /data/local/tmp for the appearance of the exchange directory, you
 * have bigger problems than whatever it's going to do with root privileges.
 */
@Suppress("SetWorldReadable", "SetWorldWritable")
object FileObserverProtocol {
  const val REQUEST = "request"
  const val RESPONSE = "response"

  /** Creates the exchange directory with appropriate permissions. */
  fun createExchangeDir(commonDir: File): File {
    val exchangeDir = File.createTempFile("androidx", ".tmp", commonDir)
    exchangeDir.delete()
    exchangeDir.mkdir()
    exchangeDir.setReadable(/* readable= */ true, /* ownerOnly= */ false)
    exchangeDir.setWritable(/* writable= */ true, /* ownerOnly= */ false)
    exchangeDir.setExecutable(/* executable= */ true, /* ownerOnly= */ false)
    return exchangeDir
  }

  /**
   * Writes a request file to the exchange directory.
   *
   * @return the location for the response file
   */
  fun writeRequestFile(exchangeDir: File, message: Messages.Command): File {
    val stem = UUID.randomUUID().toString()
    val request = File(exchangeDir, "${stem}.$REQUEST")
    request.outputStream().use {
      request.setReadable(/* readable= */ true, /* ownerOnly= */ false)
      request.setWritable(/* writable= */ true, /* ownerOnly= */ false)
      message.writeTo(it)
    }
    return File(exchangeDir, "${stem}.response")
  }

  fun isRequestFile(file: File) = file.name.endsWith(".$REQUEST")

  fun calculateResponseFile(requestFile: File) =
    File(requestFile.parentFile, "${requestFile.name.split(".").first()}.$RESPONSE")

  /** Reads and deletes the request file */
  fun readRequestFile(request: File): Messages.Command {
    val command: Messages.Command
    request.inputStream().use { command = Messages.Command.readFrom(it) }
    request.delete()
    return command
  }

  /** Writes the response file */
  fun writeResponseFile(path: File, result: Messages.CommandResult) {
    path.outputStream().use {
      path.setReadable(/* readable= */ true, /* ownerOnly= */ false)
      path.setWritable(/* writable= */ true, /* ownerOnly= */ false)
      result.writeTo(it)
    }
  }

  /** Reads and deletes the response file. */
  fun readResponseFile(response: File): Messages.CommandResult {
    try {
      val result: Messages.CommandResult
      response.inputStream().use { result = Messages.CommandResult.readFrom(it) }
      response.delete()
      return result
    } catch (x: IOException) {
      return Messages.CommandResult(
        resultType = Messages.ResultType.CLIENT_ERROR,
        stderr = x.toByteArray()
      )
    }
  }
}

/**
 * Writes an exception stack trace to a ByteArray as UTF-8, to make them easy to pass through
 * Messages.CommandResult.
 */
public fun Exception.toByteArray(): ByteArray {
  val bos = ByteArrayOutputStream()
  val pw = PrintWriter(OutputStreamWriter(bos, Charsets.UTF_8))
  printStackTrace(pw)
  pw.close()
  return bos.toByteArray()
}
