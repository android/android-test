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
import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private static TestEventServiceConnection defaultConn;

  /*
   * At most one of these should not be null - the non-null value determines how this client
   * operates.
   */
  @Nullable private final TestDiscoveryListener testDiscoveryListener;
  @Nullable private final OrchestratedInstrumentationListener notificationRunListener;
  @Nullable private final TestPlatformListener testPlatformListener;
  private final AtomicBoolean isConnectedToOrchestrator = new AtomicBoolean(false);

  /** Creates a no-op TestEventClient that doesn't send test discovery or run events. */
  private TestEventClient() {
    this.testDiscoveryListener = null;
    this.notificationRunListener = null;
    this.testPlatformListener = null;
  }

  /**
   * Creates a TestEventClient in test discovery mode. Call {@link #addTests(Description)} from the
   * test runner to send the test case info to the remote service.
   */
  private TestEventClient(@NonNull TestDiscoveryListener testDiscoveryListener) {
    checkNotNull(testDiscoveryListener, "testDiscovery cannot be null");
    this.testDiscoveryListener = testDiscoveryListener;
    this.notificationRunListener = null;
    this.testPlatformListener = null;
  }

  /**
   * Creates a TestEventClient in test run notifications mode. Call {@link #getRunListener()} to get
   * the JUnit {@link RunListener} to register in the test runner.
   */
  private TestEventClient(@NonNull OrchestratedInstrumentationListener runListener) {
    checkNotNull(runListener, "runListener cannot be null");
    this.testDiscoveryListener = null;
    this.notificationRunListener = runListener;
    this.testPlatformListener = null;
  }

  /**
   * Creates a TestEventClient in test run notifications mode. Call {@link #getRunListener()} to get
   * the JUnit {@link RunListener} to register in the test runner.
   */
  private TestEventClient(@NonNull TestPlatformListener runListener) {
    checkNotNull(runListener, "runListener cannot be null");
    this.testDiscoveryListener = null;
    this.notificationRunListener = null;
    this.testPlatformListener = runListener;
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
    TestEventServiceConnection connection =
        defaultConn != null ? defaultConn : getConnection(listener, args);
    TestEventClient result = NO_OP_CLIENT;
    if (args.isTestDiscoveryRequested) {
      Log.v(TAG, "Test discovery events requested");
      TestDiscoveryEventService testDiscoveryEventService = (TestDiscoveryEventService) connection;
      TestDiscoveryListener testDiscoveryListener =
          new TestDiscoveryListener(testDiscoveryEventService);
      result = new TestEventClient(testDiscoveryListener);
    } else if (args.isTestRunEventsRequested) {
      Log.v(TAG, "Test run events requested");
      if (args.testPlatformMigration) {
        TestPlatformListener platformListener =
            new TestPlatformListener((TestPlatformEventService) connection);
        result = new TestEventClient(platformListener);
      } else {
        TestRunEventService notificationService = (TestRunEventService) connection;
        OrchestratedInstrumentationListener runListener =
            new OrchestratedInstrumentationListener(notificationService);
        result = new TestEventClient(runListener);
      }
    }
    connection.connect(context);
    return result;
  }

  /**
   * Returns true if an orchestration service (either discovery or test run events service) was
   * requested.
   */
  public boolean isOrchestrationServiceEnabled() {
    return isTestDiscoveryEnabled() || isTestRunEventsEnabled();
  }

  /**
   * Returns true if test discovery was requested by providing both the {@link
   * TestEventClientArgs.Builder#orchestratorService} and {@link
   * TestEventClientArgs.Builder#isTestDiscoveryRequested} arguments.
   *
   * @return true if in test discovery mode, false if in the default test run mode
   */
  private boolean isTestDiscoveryEnabled() {
    return testDiscoveryListener != null;
  }

  /**
   * Returns true if test orchestration was requested by providing both the {@link
   * TestEventClientArgs.Builder#orchestratorService} and {@link
   * TestEventClientArgs.Builder#isTestRunEventsRequested} arguments.
   *
   * @return true if in orchestrated test run mode
   */
  private boolean isTestRunEventsEnabled() {
    return notificationRunListener != null || testPlatformListener != null;
  }

  /**
   * Returns the main {@link RunListener} for reporting test results
   *
   * @return an {@link RunListener} instance or null if testEventClient is not enabled
   */
  @Nullable
  public RunListener getRunListener() {
    if (isTestDiscoveryEnabled()) {
      return testDiscoveryListener;
    } else if (isTestRunEventsEnabled()) {
      if (notificationRunListener != null) {
        return notificationRunListener;
      } else {
        return testPlatformListener;
      }
    } else {
      return null;
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
        if (args.testPlatformMigration) {
          return new TestPlatformEventServiceConnection(
              checkNotNull(args.testRunEventService), listener);
        }
        return new TestRunEventServiceConnection(checkNotNull(args.testRunEventService), listener);
      }
    }
    throw new IllegalArgumentException(
        "TestEventClientArgs misconfiguration - can't determine which service connection to use.");
  }

  /**
   * Reports the process crash event with a given exception.
   *
   * @return true if process crash was reported to a test event service, false otherwise
   */
  public boolean reportProcessCrash(Throwable t) {
    return reportProcessCrash(t, /* timeout */ SECONDS.toMillis(20));
  }

  /**
   * Reports the process crash event with a given exception.
   *
   * @return true if process crash was reported to a test event service, false otherwise
   */
  public boolean reportProcessCrash(Throwable t, long timeoutMillis) {
    if (!isConnectedToOrchestrator.get()) {
      Log.w(TAG, "Process crashed before connection to orchestrator");
      return false;
    }
    if (isTestRunEventsEnabled()) {
      // Waits until the orchestrator gets a chance to handle the test failure (if any) and
      // report the process crashed event to the orchestrator before bringing down the entire
      // Instrumentation process.
      //
      // It's also possible that the process crashes in the middle of a test, so no TestFinish event
      // will be received. In this case, it will wait until timeoutMillis is reached.
      if (notificationRunListener != null) {
        Log.d(TAG, "Reporting process crashed to orchestration test run event service.");
        return notificationRunListener.reportProcessCrash(t, timeoutMillis);
      }
      // Ignores timeoutMillis, this listener reports the error and allows the process to exit
      // quickly. We don't wait for the test to handle the exception.
      if (testPlatformListener != null) {
        Log.d(TAG, "Reporting process crash to platform test event service.");
        return testPlatformListener.reportProcessCrash(t);
      }
    } else if (isTestDiscoveryEnabled()) {
      Log.d(TAG, "Reporting process crash to platform test discovery service.");
      return testDiscoveryListener.reportProcessCrash(t);
    }
    return false;
  }

  /**
   * Sets the default connection to use to connect to the orchestrator.
   *
   * <p>Only useful in internal platform testing. Shouldn't be called when running Instrumentation
   * tests.
   *
   * @param conn the default connection instance to use.
   */
  public static void setOrchestratorConnection(TestEventServiceConnection conn) {
    defaultConn = checkNotNull(conn);
  }

  public void setConnectedToOrchestrator(boolean b) {
    isConnectedToOrchestrator.set(b);
  }
}
