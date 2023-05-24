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

/** ShellExecutor that talks to FileObserverShellMain. */
class ShellExecutorFileObserverImpl(private val binderKey: String) : ShellExecutor {
  private val client = ShellCommandFileObserverClient()

  override fun getBinderKey() = binderKey

  /** {@inheritDoc} */
  override fun executeShellCommandSync(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeoutMs: Long
  ): String {
    if (command == null || command.isEmpty()) {
      throw IllegalArgumentException("Null or empty command")
    }

    val message =
      Messages.Command(
        command,
        parameters ?: emptyList(),
        shellEnv ?: emptyMap(),
        executeThroughShell,
        redirectErrorStream = true,
        if (timeoutMs > 0L) timeoutMs else TIMEOUT_FOREVER
      )

    val execution = client.run(binderKey, message)
    return execution.await().stdout.toString(Charsets.UTF_8)
  }

  /** {@inheritDoc} */
  override fun executeShellCommandSync(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean
  ) = executeShellCommandSync(command, parameters, shellEnv, executeThroughShell, TIMEOUT_FOREVER)

  /** {@inheritDoc} */
  override fun executeShellCommand(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean,
    timeoutMs: Long
  ): InputStream {
    if (command == null || command.isEmpty()) {
      throw IllegalArgumentException("Null or empty command")
    }

    val message =
      Messages.Command(
        command,
        parameters ?: emptyList(),
        shellEnv ?: emptyMap(),
        executeThroughShell,
        redirectErrorStream = true,
        if (timeoutMs > 0L) timeoutMs else TIMEOUT_FOREVER
      )

    return client.run(binderKey, message).asStream()
  }

  /** {@inheritDoc} */
  override fun executeShellCommand(
    command: String?,
    parameters: List<String>?,
    shellEnv: Map<String, String>?,
    executeThroughShell: Boolean
  ) = executeShellCommand(command, parameters, shellEnv, executeThroughShell, TIMEOUT_FOREVER)

  companion object {
    const val TIMEOUT_FOREVER = 24 * 60 * 60 * 1000L
  }
}
