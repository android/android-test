/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.shellexecutor;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Describes a command to be run by ShellCommandExecutor */
class ShellCommand {

  private final String command;
  private final List<String> parameters;
  private final Map<String, String> shellEnv;
  private final boolean executeThroughShell;
  private final long timeoutMs;

  /**
   * @param command The command to be executed
   * @param parameters A list of params to be passed to the command
   * @param shellEnv A map of environment variables to be set before the command is executed
   * @param executeThroughShell If set to {@code true}, the command string will be executed through
   *     the shell with parameters given as additional shell arguments.
   * @param timeoutMs If set to a value > 0, this creates a watcher that kills the subprocess when
   *     it surpasses the timeout.
   */
  ShellCommand(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell,
      long timeoutMs) {

    if (TextUtils.isEmpty(command)) {
      throw new IllegalArgumentException("Null or empty command");
    }

    this.command = command;
    this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
    this.shellEnv = Collections.unmodifiableMap(new HashMap<>(shellEnv));
    this.executeThroughShell = executeThroughShell;
    this.timeoutMs = timeoutMs;
  }

  public boolean executeThroughShell() {
    return executeThroughShell;
  }

  public String getCommand() {
    return command;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public Map<String, String> getShellEnv() {
    return shellEnv;
  }

  public long getTimeoutMs() {
    return timeoutMs;
  }
}
