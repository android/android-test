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
import static androidx.test.orchestrator.OrchestratorConstants.ORCHESTRATOR_FORWARDED_INSTRUMENTATION_ARGS;
import static androidx.test.orchestrator.OrchestratorConstants.TARGET_INSTRUMENTATION_ARGUMENT;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.test.services.shellexecutor.ClientNotConnected;
import androidx.test.services.shellexecutor.ShellExecutorFactory;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runnable to run a single am instrument command to execute a single test. */
public class TestRunnable implements Runnable {

  private static final String TAG = "TestRunnable";

  private final Bundle arguments;
  private final RunFinishedListener listener;
  private final OutputStream outputStream;
  private final String test;
  private final String testFilePath;
  private final boolean collectTests;
  private final Context context;
  private final String secret;

  /**
   * Constructs a TestRunnable which will run a single test.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener a callback listener to know when the run has completed
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
    return new TestRunnable(context, secret, arguments, outputStream, listener, test, null, false);
  }

  /**
   * Constructs a TestRunnable which will ask the instrumentation to list out its tests.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener a callback listener to know when the run has completed
   */
  public static TestRunnable testCollectionRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener) {
    return new TestRunnable(context, secret, arguments, outputStream, listener, null, null, true);
  }

  /**
   * Constructs a TestRunnable which will run a specific subset of tests from a file.
   *
   * @param context A context
   * @param secret A string representing the speakeasy binder key
   * @param arguments contains arguments to be passed to the target instrumentation
   * @param outputStream the stream to write the results of the test process
   * @param listener a callback listener to know when the run has completed
   * @param testFilePath the path to a file containing the tests to run
   */
  public static TestRunnable testSubsetRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener,
      String testFilePath) {
    return new TestRunnable(context, secret, arguments, outputStream, listener, null, testFilePath, false);
  }

  @VisibleForTesting
  TestRunnable(
      Context context,
      String secret,
      Bundle arguments,
      OutputStream outputStream,
      RunFinishedListener listener,
      String test,
      String testFilePath,
      boolean collectTests) {
    this.context = context;
    this.secret = secret;
    this.arguments = new Bundle(arguments);
    this.outputStream = outputStream;
    this.listener = listener;
    this.test = test;
    this.testFilePath = testFilePath;
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
      try {
        ByteStreams.copy(inputStream, outputStream);
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

    // Override the class parameter with the current test target or use testFile for test subset.
    if (test != null) {
      targetArgs.putString(AJUR_CLASS_ARGUMENT, test);
    } else if (testFilePath != null && !testFilePath.isEmpty()) {
      // For test subset, use testFile parameter to read tests from file
      targetArgs.putString("testFile", testFilePath);
    }

    return targetArgs;
  }

  /**
   * Instrumentation params are delimited by comma, each param is stripped from leading and trailing
   * whitespace. *
   *
   * <p>The order of the params are critical to the correctness here as we split up params that have
   * whitespace (eg: key value) into two different params `key` and `value` which means that those
   * two different params must be next to each other the entire time.
   */
  private List<String> getInstrumentationParamsAndRemoveBundleArgs(Bundle arguments) {
    List<String> cleanedParams = new ArrayList<>();
    String forwardedArgs = arguments.getString(ORCHESTRATOR_FORWARDED_INSTRUMENTATION_ARGS);
    if (forwardedArgs != null) {
      for (String param : forwardedArgs.split(",")) {
        // The instrumentation code within the Android Platform was not designed to deal
        // with whitespace in the arguments. The options parsing logic:
        // https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/cmds/am/src/com/android/commands/am/Am.java;l=160-204;drc=61197364367c9e404c7da6900658f1b16c42d0da;bpv=0;bpt=0?q=am.java&ss=android%2Fplatform%2Fsuperproject%2Fmain
        // uses `opt.equal("--key")` which assumes that each individual key will live
        // in its separate string. However, if we start sending strings like "--key value" then
        // options parsing will fail. The problem here is that this termination is very subtle.
        // as the instrumentation does not report to logcat, but to System.err which can sometimes
        // buffer the error and silently drop it on process exit.
        Collections.addAll(cleanedParams, param.strip().split(" "));
      }
      arguments.remove(ORCHESTRATOR_FORWARDED_INSTRUMENTATION_ARGS);
    }
    return cleanedParams;
  }

  private List<String> buildShellParams(Bundle arguments) throws IOException, ClientNotConnected {
    List<String> params = new ArrayList<>();
    params.add("instrument");
    params.add("-w");
    params.add("-r");

    params.addAll(getInstrumentationParamsAndRemoveBundleArgs(arguments));

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
    return new ShellExecutorFactory(context, secret)
        .create()
        .executeShellCommand("am", params, null, false);
  }
}
