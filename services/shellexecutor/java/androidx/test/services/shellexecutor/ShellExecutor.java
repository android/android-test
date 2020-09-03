/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.services.shellexecutor;

import android.os.RemoteException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Executes a shell command in a similar manner and environment as {@link UiAutomation
 * #executeShellCommand} , (i.e. as either root or shell user).
 *
 * <p>Unlike {@link UiAutomation} this is available on all API levels and will not conflict with
 * other instances of {@link UiAutomation}.
 */
public interface ShellExecutor {
  /**
   * Execute a command with elevated permissions and block.
   *
   * @param command The shell command to be executed.
   * @param parameters A {@link Map} parameters to be given to the shell command
   * @param shellEnv A {@link Map} of shell environment variables to be set
   * @param executeThroughShell If set to true, the command string will be executed through the
   *     shell with parameters given as additional shell arguments.
   * @param timeoutMs Optional, destroys the executing subprocess if it runs longer than this
   *     timeout.
   * @return {@link String} representing the contents of the shell output of the command.
   * @throws IOException if cannot execute command on executor service.
   */
  String executeShellCommandSync(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell,
      long timeoutMs)
      throws ClientNotConnected, IOException, RemoteException;

  /**
   * Execute a command with elevated permissions and block.
   *
   * @param command The shell command to be executed.
   * @param parameters A {@link Map} parameters to be given to the shell command
   * @param shellEnv A {@link Map} of shell environment variables to be set
   * @param executeThroughShell If set to true, the command string will be executed through the
   *     shell with parameters given as additional shell arguments.
   * @return {@link String} representing the contents of the shell output of the command.
   * @throws IOException if cannot execute command on executor service.
   */
  String executeShellCommandSync(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell)
      throws ClientNotConnected, IOException, RemoteException;

  /**
   * Execute a command with elevated permissions and return immediately.
   *
   * @param command The shell command to be executed.
   * @param parameters A {@link Map} parameters to be given to the shell command
   * @param shellEnv A {@link Map} of shell environment variables to be set
   * @param executeThroughShell If set to true, the command string will be executed through the
   *     shell with parameters given as additional shell arguments.
   * @param timeoutMs Optional, destroys the executing subprocess if it runs longer than this
   *     timeout.
   * @return {@link java.io.InputStream} representing the shell output of the command.
   * @throws IOException if cannot execute command on executor service.
   */
  InputStream executeShellCommand(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell,
      long timeoutMs)
      throws ClientNotConnected, IOException, RemoteException;

  /**
   * Execute a command with elevated permissions and return immediately.
   *
   * @param command The shell command to be executed.
   * @param parameters A {@link Map} parameters to be given to the shell command
   * @param shellEnv A {@link Map} of shell environment variables to be set
   * @param executeThroughShell If set to true, the command string will be executed through the
   *     shell with parameters given as additional shell arguments.
   * @return {@link java.io.InputStream} representing the shell output of the command.
   * @throws IOException if cannot execute command on executor service.
   */
  InputStream executeShellCommand(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell)
      throws ClientNotConnected, IOException, RemoteException;
}
