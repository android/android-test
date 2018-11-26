/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.runner.permission;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.annotation.TargetApi;
import android.app.UiAutomation;
import android.os.ParcelFileDescriptor;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link ShellCommand} implementation which uses {@link UiAutomation} to grant a permission at
 * runtime.
 */
@TargetApi(value = 23)
class UiAutomationShellCommand extends ShellCommand {

  private static final String TAG = "UiAutomationShellCmd";

  enum PmCommand {
    GRANT_PERMISSION("grant");

    private final String pmCommand;

    PmCommand(String command) {
      pmCommand = "pm " + command;
    }

    public String get() {
      return pmCommand;
    }
  }

  private final String targetPackage;
  private final String permission;
  private final PmCommand command;
  private final UiAutomation uiAutomation;

  UiAutomationShellCommand(String targetPackage, String permission, PmCommand pmCommand) {
    this.targetPackage = shellEscape(targetPackage);
    this.permission = shellEscape(permission);
    command = pmCommand;
    uiAutomation = checkNotNull(InstrumentationRegistry.getInstrumentation().getUiAutomation());
  }

  @Override
  public void execute() throws Exception {
    executePermissionCommand(commandForPermission());
  }

  @VisibleForTesting
  protected String commandForPermission() {
    return new StringBuilder(command.get())
        .append(" ")
        .append(targetPackage)
        .append(" ")
        .append(permission)
        .toString();
  }

  /**
   * Since {@link UiAutomation#executeShellCommand(String)} does not block until the command
   * executed successfully. Write the output to byte array to make sure permission is granted before
   * starting the test.
   *
   * @param cmd to run to request permissions
   */
  private void executePermissionCommand(String cmd) throws IOException {
    Log.i(TAG, "Requesting permission: " + cmd);
    try {
      awaitTermination(uiAutomation.executeShellCommand(cmd), 2, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      Log.e(TAG, "Timeout while executing cmd: " + cmd);
    }
  }

  /**
   * Blocks until the command finished executing.
   *
   * <p>The reason why we need to have this method is because of the way that {@link
   * UiAutomation#executeShellCommand(String)} is currently implemented in the platform. Ideally
   * executeShellCommand() would just return the exit code to us or give us a callback.
   *
   * <p>This is a better way than just using Thread.sleep(), since it would be really hard to
   * determine what a timeout would be. We would wait too long on a fast device and it might still
   * not work on slow devices.
   *
   * <p>But this solution is not a great way either to determine if a command has finished execution
   * and there might still be situations where even this solution might be flaky. To have another
   * safety net we wait an additional 1000ms in RequestPermissionCallable if the permission was not
   * granted as expected. This seems to be the best we can do for now.
   *
   * @param pfDescriptor Used to read the content returned by shell command
   */
  private static void awaitTermination(
      ParcelFileDescriptor pfDescriptor, long timeout, TimeUnit unit)
      throws IOException, TimeoutException {
    long timeoutInMillis = unit.toMillis(timeout);
    long endTimeInMillis = timeoutInMillis > 0 ? System.currentTimeMillis() + timeoutInMillis : 0;
    BufferedReader reader = null;
    try {
      reader =
          new BufferedReader(
              new InputStreamReader(new ParcelFileDescriptor.AutoCloseInputStream(pfDescriptor)));
      String line;
      while ((line = reader.readLine()) != null) {
        Log.i(TAG, line);
        if (endTimeInMillis > System.currentTimeMillis()) {
          throw new TimeoutException();
        }
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }
}
