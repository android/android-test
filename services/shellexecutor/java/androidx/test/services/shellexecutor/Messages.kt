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

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

/**
 * Messages used for communication between the ShellCommandFileObserverClient and the
 * ShellCommandFileObserverExecutorServer.
 *
 * These are not protobufs because on APIs 15-19, desugaring causes the FileObserverShellMain to
 * crash with ClassNotFound: j$.util.concurrent.ConcurrentHashMap, and GeneratedMessageLite needs
 * that class. The fix for this (starting multidex earlier) is not possible in a class with no
 * Context.
 *
 * They aren't PersistableBundles because those get introduced in API 21. So we resort to the
 * ObjectInputStream/ObjectOutputStream, working around the BanSerializableRead.
 */
object Messages {
  data class Command(
    val command: String,
    val parameters: List<String> = emptyList(),
    val shellEnv: Map<String, String> = emptyMap(),
    val executeThroughShell: Boolean = false,
    val redirectErrorStream: Boolean = false,
    val timeoutMs: Long,
  ) {
    fun writeTo(outputStream: OutputStream) {
      ObjectOutputStream(outputStream).use {
        it.apply {
          writeUTF(command)
          write(parameters)
          write(shellEnv)
          writeBoolean(executeThroughShell)
          writeBoolean(redirectErrorStream)
          writeLong(timeoutMs)
        }
      }
    }

    override fun toString(): String {
      val env = shellEnv.asSequence().map { "${it.key}=${it.value}" }.joinToString(", ")
      val ets = if (executeThroughShell) " executeThroughShell" else ""
      val res = if (redirectErrorStream) " redirectErrorStream" else ""
      return "[$command] [${parameters.joinToString("] [")}] ($env)$ets$res ${timeoutMs}ms"
    }

    companion object {
      fun readFrom(inputStream: InputStream): Command {
        ObjectInputStream(inputStream).use {
          it.apply {
            return Command(
              command = readUTF(),
              parameters = readStringList(),
              shellEnv = readStringMap(),
              executeThroughShell = readBoolean(),
              redirectErrorStream = readBoolean(),
              timeoutMs = readLong()
            )
          }
        }
      }
    }
  }

  enum class ResultType {
    EXITED,
    TIMED_OUT,
    SERVER_ERROR,
    CLIENT_ERROR
  }

  data class CommandResult(
    val resultType: ResultType,
    val exitCode: Int = -1,
    val stdout: ByteArray = ByteArray(0),
    val stderr: ByteArray = ByteArray(0),
  ) {
    fun writeTo(outputStream: OutputStream) {
      ObjectOutputStream(outputStream).use {
        it.apply {
          write(resultType)
          writeInt(exitCode)
          writeInt(stdout.size)
          write(stdout)
          writeInt(stderr.size)
          write(stderr)
        }
      }
    }

    override fun toString(): String {
      val out = stdout.toString(Charsets.UTF_8)
      val err = stderr.toString(Charsets.UTF_8)
      return "${resultType.name} $exitCode stdout=[$out] stderr=[$err]"
    }

    companion object {
      fun readFrom(inputStream: InputStream): CommandResult {
        ObjectInputStream(inputStream).use {
          it.apply {
            return CommandResult(
              resultType = readResultType(),
              exitCode = readInt(),
              stdout = ByteArray(readInt()).also { readFully(it) },
              stderr = ByteArray(readInt()).also { readFully(it) }
            )
          }
        }
      }
    }
  }

  private fun ObjectOutputStream.write(resultType: ResultType) {
    writeUTF(resultType.name)
  }

  private fun ObjectInputStream.readResultType() = ResultType.valueOf(readUTF())

  private fun ObjectOutputStream.write(list: List<String>) {
    writeInt(list.size)
    for (s in list) writeUTF(s)
  }

  private fun ObjectInputStream.readStringList(): List<String> {
    val count = readInt()
    val result = mutableListOf<String>()
    for (i in 1..count) result.add(readUTF())
    return result
  }

  private fun ObjectOutputStream.write(map: Map<String, String>) {
    writeInt(map.size)
    for (entry in map) {
      writeUTF(entry.key)
      writeUTF(entry.value)
    }
  }

  private fun ObjectInputStream.readStringMap(): Map<String, String> {
    val count = readInt()
    val result = mutableMapOf<String, String>()
    for (i in 1..count) result.put(readUTF(), readUTF())
    return result
  }
}
