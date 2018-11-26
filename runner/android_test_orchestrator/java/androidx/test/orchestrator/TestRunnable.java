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
package androidx.test.orchestrator;

import static androidx.test.orchestrator.OrchestratorConstants.AJUR_CLASS_ARGUMENT;
import static androidx.test.orchestrator.OrchestratorConstants.AJUR_LIST_TESTS_ARGUMENT;
import static androidx.test.orchestrator.OrchestratorConstants.ISOLATED_ARGUMENT;
import static androidx.test.orchestrator.OrchestratorConstants.TARGET_INSTRUMENTATION_ARGUMENT;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.services.shellexecutor.ClientNotConnected;
import androidx.test.services.shellexecutor.ShellExecutorImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** Runnable to run a single am instrument command to execute a single test. */
public class TestRunnable implements Runnable {

  private static final String TAG = "TestRunnable";

  private final Bundle arguments;
  private final RunFinishedListener listener;
  private final OutputStream outputStream;
  private final String test;
  private final boolean collectTests;
  private final Context context;
  private final String secret;

  /**
   * Constructs a TestRunnable executes all tests in arguments.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener, a callback listener to know when the run has completed
   */
  public static TestRunnable legacyTestRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener) {
    return new TestRunnable(context, secret, arguments, outputStream, listener, null, false);
  }

  /**
   * Constructs a TestRunnable which will run a single test.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener, a callback listener to know when the run has completed
   * @param test contains a specific test#method to run. Will override whatever is specified in the
   *     bundle.
   */
  public static TestRunnable singleTestRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener,
      String test) {
    return new TestRunnable(context, secret, arguments, outputStream, listener, test, false);
  }

  /**
   * Constructs a TestRunnable which will ask the instrumentation to list out its tests.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener, a callback listener to know when the run has completed
   */
  public static TestRunnable testCollectionRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener) {
    return new TestRunnable(context, secret, arguments, outputStream, listener, null, true);
  }

  @VisibleForTesting
  TestRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener,
      String test,
      boolean collectTests) {
    this.context = context;
    this.secret = secret;
    this.arguments = new Bundle(arguments);
    this.outputStream = outputStream;
    this.listener = listener;
    this.test = test;
    this.collectTests = collectTests;
  }

  /** Called at the end of a test run. */
  public interface RunFinishedListener {
    void runFinished();
  }

  @Override
  public void run() {
    try {
      InputStream inputStream =
          runShellCommand(buildShellParams(getTargetInstrumentationArguments()));
      byte[] read = new byte[1024];
      try {
        while (inputStream.read(read) != -1) {
          outputStream.write(read);
        }
      } finally {
        if (inputStream != null) {
          inputStream.close();
          outputStream.close();
        } else {
          Log.e(TAG, "InputStream returned from shell command is null");
        }
      }
    } catch (IOException e) {
      Log.e(TAG, "IOException thrown when running remote test", e);
    } catch (ClientNotConnected e) {
      Log.e(TAG, "ShellCommandClient not connected, unable to run remote test", e);
    } catch (InterruptedException e) {
      Log.e(TAG, "ShellCommandClient connection interrupted, unable to run remote test", e);
    } catch (RemoteException e) {
      Log.e(TAG, "ShellCommandClient remote execution, unable to run remote test", e);
    }

    listener.runFinished();
  }

  private String getTargetInstrumentation() {
    return arguments.getString(TARGET_INSTRUMENTATION_ARGUMENT);
  }

  private Bundle getTargetInstrumentationArguments() {
    Bundle targetArgs = new Bundle(arguments);
    // Filter out the only argument intended specifically for Listener
    targetArgs.remove(TARGET_INSTRUMENTATION_ARGUMENT);
    targetArgs.remove(ISOLATED_ARGUMENT);

    if (collectTests) {
      targetArgs.putString(AJUR_LIST_TESTS_ARGUMENT, "true");
    } else {
      // If we aren't engaging in test collection, then we should have a specific test target, and
      // the orchestrator will pass a specific class parameter. Passing class and package parameters
      // at the same time breaks AndroidJUnitRunner and is redundant.  Thus, we can remove these
      // parameters.
      targetArgs.remove("package");
      targetArgs.remove("testFile");
    }

    // Override the class parameter with the current test target.
    if (test != null) {
      targetArgs.putString(AJUR_CLASS_ARGUMENT, test);
    }

    return targetArgs;
  }

  private List<String> buildShellParams(Bundle arguments) throws IOException, ClientNotConnected {
    List<String> params = new ArrayList<>();
    params.add("instrument");
    params.add("-w");
    params.add("-r");

    for (String key : arguments.keySet()) {
      params.add("-e");
      params.add(key);
      params.add(arguments.getString(key));
    }
    params.add(getTargetInstrumentation());

    return params;
  }

  InputStream runShellCommand(List<String> params)
      throws IOException, ClientNotConnected, InterruptedException, RemoteException {
    return new ShellExecutorImpl(context, secret).executeShellCommand("am", params, null, false);
  }
}
