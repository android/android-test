/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.test.services.events.client;

import static androidx.test.services.events.client.Checks.checkNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Connects to the remote {@link androidx.test.orchestrator.OrchestratorService} if necessary. The
 * Orchestrator may request the {@link androidx.test.runner.AndroidJUnitRunner} to send back either
 * test case information or test run event notifications by passing the name of the remote service
 * to connect to as an instrumentation argument:
 *
 * <p>Orchestrator v1 provides a single service for both test discovery and test notifications
 * ({@code androidx.test.orchestrator.OrchestratorService}). This service is used when the {@code -e
 * orchestratorService} and/or {@code -e listTestsForOrchestrator} are non-empty. Both args are
 * typically set to "true".
 */
public final class TestEventClient {
  private static final String TAG = "TestEventClient";
  private TestDiscovery testDiscovery;
  private OrchestratedInstrumentationListener notificationRunListener;

  /**
   * Connects to the remote {@link androidx.test.orchestrator.OrchestratorService} if necessary and
   * returns true. If no Orchestrator connection is needed, it returns false.
   *
   * <p>The Instrumentation will be notified when the connection has finished via its {@link
   * TestEventClientConnectListener} interface.
   *
   * @return true if connection was started, false if no connection is needed
   */
  public boolean connect(
      @NonNull Context context,
      @NonNull TestEventClientConnectListener listener,
      @NonNull TestEventClientArgs args) {
    checkNotNull(context, "context");
    checkNotNull(listener, "listener");
    checkNotNull(args, "args");
    if (!args.isOrchestrated) {
      return false;
    }
    if (!args.isPrimaryInstrProcess) {
      Log.e(TAG, "Orchestration requested, but this isn't the primary instrumentation");
      return false;
    }
    OrchestratorConnection connection;
    connection = getConnection(listener, args);
    if (args.isTestDiscoveryRequested) {
      Log.v(TAG, "Test discovery events requested");
      TestDiscoveryEventService testDiscoveryEventService = (TestDiscoveryEventService) connection;
      testDiscovery = new TestDiscovery(testDiscoveryEventService);
    } else if (args.isTestRunEventsRequested) {
      Log.v(TAG, "Test run events requested");
      TestRunEventService notificationService = (TestRunEventService) connection;
      notificationRunListener = new OrchestratedInstrumentationListener(notificationService);
    }
    connection.connect(context);
    return true;
  }

  /**
   * Returns true if test discovery was requested by providing both the {@link
   * TestEventClientArgs.Builder#orchestratorService} and {@link
   * TestEventClientArgs.Builder#isTestDiscoveryRequested} arguments.
   *
   * @return true if in test discovery mode, false if in the default test run mode
   */
  public boolean isDiscoveryServiceConnected() {
    return testDiscovery != null;
  }

  /**
   * Returns true if test orchestration was requested by providing the {@link
   * TestEventClientArgs.Builder#orchestratorService} argument.
   *
   * @return true if in orchestrated test run mode
   */
  public boolean isNotificationServiceConnected() {
    return notificationRunListener != null;
  }

  /**
   * Returns the {@link OrchestratedInstrumentationListener} instance if available.
   *
   * @return an {@link OrchestratedInstrumentationListener} instance
   */
  @Nullable
  public RunListener getNotificationRunListener() {
    if (!isNotificationServiceConnected()) {
      Log.e(TAG, "Orchestrator service not connected - can't send test run notifications");
    }
    return notificationRunListener;
  }

  /**
   * Sends a single test case to the orchestrator during test discovery mode.
   *
   * @param description the JUnit {@link Description} representing a test which is to be run
   */
  public void addTests(@NonNull Description description) {
    if (!isDiscoveryServiceConnected()) {
      Log.e(TAG, "Orchestrator service not connected - can't send tests");
      return;
    }
    try {
      testDiscovery.addTests(description);
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to add test [" + description + "]");
    }
  }

  @NonNull
  private static OrchestratorConnection getConnection(
      @NonNull TestEventClientConnectListener listener, @NonNull TestEventClientArgs args) {
    if (args.orchestratorVersion == 1) {
      if (args.connectionFactory != null) {
        return args.connectionFactory.create(listener);
      } else {
        throw new IllegalArgumentException(
            "Orchestrator v1 connectionFactory must be provided "
                + "by TestEventClientArgs.Builder#setConnectionFactory()");
      }
    } else if (args.orchestratorVersion == 2) {
      if (args.isTestDiscoveryRequested) {
        return new TestDiscoveryEventServiceConnection(
            checkNotNull(args.testDiscoveryService), listener);
      } else if (args.isTestRunEventsRequested) {
        return new TestRunEventServiceConnection(checkNotNull(args.testRunEventService), listener);
      }
    }
    throw new IllegalArgumentException(
        "TestEventClientArgs misconfiguration - can't determine "
            + "which service connection to use.");
  }

  /** Reports the process crash event with a given exception. */
  public void reportProcessCrash(Throwable t, long timeoutMillis) {
    if (isNotificationServiceConnected()) {
      // Waits until the orchestrator gets a chance to handle the test failure (if any) and
      // report the process crashed event to the orchestrator before bringing down the entire
      // Instrumentation process.
      //
      // It's also possible that the process crashes in the middle of a test, so no TestFinish event
      // will be received. In this case, it will wait until timeoutMillis is reached.
      notificationRunListener.reportProcessCrash(t, timeoutMillis);
    }
  }
}
