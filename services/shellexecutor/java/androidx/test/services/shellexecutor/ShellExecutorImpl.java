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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** {@inheritDoc} */
public final class ShellExecutorImpl implements ShellExecutor {
  private static final String TAG = "ShellExecutorImpl";

  private final Context context;
  private final String binderKey;

  public ShellExecutorImpl(Context context, String binderKey) {
    if (null == context) {
      throw new NullPointerException("context, cannot be null!");
    }
    this.context = context;

    if (null == binderKey) {
      Log.e(TAG, "Unable to find the binder key from the instrumentation registry.");
      throw new NullPointerException("binderKey, cannot be null!");
    }
    this.binderKey = binderKey;
  }

  /** {@inheritDoc} */
  @Override
  public String getBinderKey() {
    return binderKey;
  }

  /** {@inheritDoc} */
  @Override
  public String executeShellCommandSync(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell,
      long timeoutMs) {
    try {
      return ShellCommandClient.execOnServerSync(
          context, binderKey, command, parameters, shellEnv, executeThroughShell, timeoutMs);
    } catch (ClientNotConnected e) {
      Log.e(TAG, "ShellCommandClient not connected. Is ShellCommandExecutor service started?", e);
      throw new RuntimeException(e);
    } catch (IOException | RemoteException e) {
      Log.e(
          TAG, "ShellCommandClient connection failed. Is ShellCommandExecutor service started?", e);
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String executeShellCommandSync(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell) {
    return executeShellCommandSync(command, parameters, shellEnv, executeThroughShell, 0L);
  }

  /** {@inheritDoc} */
  @Override
  public InputStream executeShellCommand(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell,
      long timeoutMs) {
    try {
      return ShellCommandClient.execOnServer(
          context, binderKey, command, parameters, shellEnv, executeThroughShell, timeoutMs);
    } catch (ClientNotConnected e) {
      Log.e(TAG, "ShellCommandClient not connected. Is ShellCommandExecutor service started?", e);
      throw new RuntimeException(e);
    } catch (IOException | RemoteException e) {
      Log.e(
          TAG, "ShellCommandClient connection failed. Is ShellCommandExecutor service started?", e);
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public InputStream executeShellCommand(
      String command,
      List<String> parameters,
      Map<String, String> shellEnv,
      boolean executeThroughShell) {
    return executeShellCommand(command, parameters, shellEnv, executeThroughShell, 0L);
  }
}
