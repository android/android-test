/*
 * Copyright (C) 2020 The Android Open Source Project
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

package androidx.test.internal.events.client;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * {@link TestEventClient} abstracts the communication with the remote Android Test Orchestrator v1
 * service ({@link androidx.test.orchestrator.OrchestratorService}) or the newer Test Event Services
 * ({@link androidx.test.services.events.discovery.ITestDiscoveryEvent} and {@link
 * androidx.test.services.events.run.ITestRunEvent}).
 *
 * <p>It connects to the appropriate remote service as necessary. The Orchestrator may request the
 * {@link androidx.test.runner.AndroidJUnitRunner} to send back either test case information or test
 * run event notifications by passing the name of the remote service to connect to as an
 * instrumentation argument:
 *
 * <p>Orchestrator v1 provides a single service for both test discovery and test notifications
 * ({@code androidx.test.orchestrator.OrchestratorService}). This service is used when the {@code -e
 * orchestratorService} and/or {@code -e listTestsForOrchestrator} are non-empty. Both args are
 * typically set to "true".
 *
 * <p>Alternatively, if the {@code -e testDiscoveryService} arg is provided then {@link
 * androidx.test.services.events.discovery.TestDiscoveryEvent} messages are sent to the specified
 * service, or if the {@code -e testRunEventService} arg is provided then {@link
 * androidx.test.services.events.run.TestRunEvent} messages are sent to the specified service.
 */
public final class TestEventClient {
  // TODO(b/161745142): Separate TestEventServiceClient and OrchestratorServiceClient impl code
  private static final String TAG = "TestEventClient";

  /** If no test discovery or test run events required, then return a client that does nothing. */
  public static final TestEventClient NO_OP_CLIENT = new TestEventClient();

  @Nullable private final TestDiscovery testDiscovery;
  @Nullable private final OrchestratedInstrumentationListener notificationRunListener;

  /** Creates a no-op TestEventClient that doesn't send test discovery or run events. */
  private TestEventClient() {
    this.testDiscovery = null;
    this.notificationRunListener = null;
  }

  /**
   * Creates a TestEventClient in test discovery mode. Call {@link #addTests(Description)} from the
   * test runner to send the test case info to the remote service.
   */
  private TestEventClient(@NonNull TestDiscovery testDiscovery) {
    checkNotNull(testDiscovery, "testDiscovery cannot be null");
    this.testDiscovery = testDiscovery;
    this.notificationRunListener = null;
  }

  /**
   * Creates a TestEventClient in test run notifications mode. Call {@link
   * #getNotificationRunListener()} to get the JUnit {@link RunListener} to register in the test
   * runner.
   */
  private TestEventClient(@NonNull OrchestratedInstrumentationListener runListener) {
    checkNotNull(runListener, "runListener cannot be null");
    this.testDiscovery = null;
    this.notificationRunListener = runListener;
  }

  /**
   * Connects to the remote Test Event Service ({@link
   * androidx.test.orchestrator.OrchestratorService}, {@link
   * androidx.test.services.events.discovery.ITestDiscoveryEvent} or {@link
   * androidx.test.services.events.run.ITestRunEvent}) as necessary.
   *
   * <p>The Instrumentation will be notified when the connection has finished via its {@link
   * TestEventClientConnectListener} interface.
   *
   * @return a new TestEventClient instance
   */
  public static TestEventClient connect(
      @NonNull Context context,
      @NonNull TestEventClientConnectListener listener,
      @NonNull TestEventClientArgs args) {
    checkNotNull(context, "context parameter cannot be null!");
    checkNotNull(listener, "listener parameter cannot be null!");
    checkNotNull(args, "args parameter cannot be null!");
    if (!args.isOrchestrated) {
      return NO_OP_CLIENT;
    }
    if (!args.isPrimaryInstrProcess) {
      Log.w(TAG, "Orchestration requested, but this isn't the primary instrumentation");
      return NO_OP_CLIENT;
    }
    TestEventServiceConnection connection = getConnection(listener, args);
    TestEventClient result = NO_OP_CLIENT;
    if (args.isTestDiscoveryRequested) {
      Log.v(TAG, "Test discovery events requested");
      TestDiscoveryEventService testDiscoveryEventService = (TestDiscoveryEventService) connection;
      TestDiscovery testDiscovery = new TestDiscovery(testDiscoveryEventService);
      result = new TestEventClient(testDiscovery);
    } else if (args.isTestRunEventsRequested) {
      Log.v(TAG, "Test run events requested");
      TestRunEventService notificationService = (TestRunEventService) connection;
      OrchestratedInstrumentationListener runListener =
          new OrchestratedInstrumentationListener(notificationService);
      result = new TestEventClient(runListener);
    }
    connection.connect(context);
    return result;
  }

  /**
   * Returns true if test discovery was requested by providing both the {@link
   * TestEventClientArgs.Builder#orchestratorService} and {@link
   * TestEventClientArgs.Builder#isTestDiscoveryRequested} arguments.
   *
   * @return true if in test discovery mode, false if in the default test run mode
   */
  public boolean isTestDiscoveryEnabled() {
    return testDiscovery != null;
  }

  /**
   * Returns true if test orchestration was requested by providing both the {@link
   * TestEventClientArgs.Builder#orchestratorService} and {@link
   * TestEventClientArgs.Builder#isTestRunEventsRequested} arguments.
   *
   * @return true if in orchestrated test run mode
   */
  public boolean isTestRunEventsEnabled() {
    return notificationRunListener != null;
  }

  /**
   * Returns the {@link OrchestratedInstrumentationListener} instance if available.
   *
   * @return an {@link OrchestratedInstrumentationListener} instance
   */
  @Nullable
  public RunListener getNotificationRunListener() {
    if (!isTestRunEventsEnabled()) {
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
    if (!isTestDiscoveryEnabled()) {
      Log.e(TAG, "Orchestrator service not connected - can't send tests");
      return;
    }
    try {
      testDiscovery.addTests(description);
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to add test [" + description + "]", e);
    }
  }

  @NonNull
  private static TestEventServiceConnection getConnection(
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
        "TestEventClientArgs misconfiguration - can't determine which service connection to use.");
  }

  /** Reports the process crash event with a given exception. */
  public void reportProcessCrash(Throwable t, long timeoutMillis) {
    if (isTestRunEventsEnabled()) {
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
