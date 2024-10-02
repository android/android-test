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

import android.net.LocalSocketAddress
import androidx.test.services.shellexecutor.LocalSocketProtocol.fromBinderKey
import java.io.InputStream
import kotlin.time.Duration.Companion.milliseconds

/** ShellExecutor that talks to LocalSocketShellMain. */
class ShellExecutorLocalSocketImpl(private val binderKey: String) : ShellExecutor {
  private val address: LocalSocketAddress

  init {
    address = fromBinderKey(binderKey)
  }

  /** {@inheritDoc} */
  override fun getBinderKey() = binderKey

  /** {@inheritDoc} */
  override fun executeShellCommandSync(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeoutMs: Long,
  ): String =
    executeShellCommand(command, parameters, shellEnv, executeThroughShell, timeoutMs).use {
      it.readBytes().toString(Charsets.UTF_8)
    }

  /** {@inheritDoc} */
  override fun executeShellCommandSync(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
  ) = executeShellCommandSync(command, parameters, shellEnv, executeThroughShell, TIMEOUT_FOREVER)

  /** {@inheritDoc} */
  override fun executeShellCommand(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeoutMs: Long,
  ): InputStream {
    if (command == null || command.isEmpty()) {
      throw IllegalArgumentException("Null or empty command")
    }
    val client = ShellCommandLocalSocketClient(address)
    val timeout =
      if (timeoutMs > 0) {
        timeoutMs.milliseconds
      } else {
        TIMEOUT_FOREVER.milliseconds
      }
    return client.request(command, parameters, shellEnv, executeThroughShell, timeout)
  }

  /** {@inheritDoc} */
  override fun executeShellCommand(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
  ) = executeShellCommand(command, parameters, shellEnv, executeThroughShell, TIMEOUT_FOREVER)

  private companion object {
    const val TAG = "ShellExecutorLocalSocketImpl"
    const val TIMEOUT_FOREVER = 24 * 60 * 60 * 1000L
  }
}
