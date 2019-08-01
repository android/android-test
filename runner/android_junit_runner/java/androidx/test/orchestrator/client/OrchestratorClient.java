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

package androidx.test.orchestrator.client;

import android.app.Instrumentation;
import android.util.Log;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.orchestrator.client.OrchestratorConnection.OnConnectListener;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Connects to the remote Orchestrator service if needed. The Orchestrator may request the Android
 * JUnit Runner to send back either test case information or test run event notifications by passing
 * the name of the remote service to connect to as an instrumentation argument:
 *
 * <p>Orchestrator v1 provides a single service for both test discovery and test notifications
 * ({@code androidx.test.orchestrator.OrchestratorService}). This service is used when the {@code -e
 * orchestratorService} and/or {@code -e listTestsForOrchestrator} are non-empty. Both args are
 * typically set to "true".
 *
 * <p>Orchestrator v2 provides two separate services ({@code
 * androidx.test.tools.orchestrator.service.TestDiscoveryService} and {@code
 * androidx.test.tools.orchestrator.service.TestNotificationService}), specified by the {@code -e
 * testDiscoveryService} or {@code -e testNotificationService} args respectively.
 */
public class OrchestratorClient {
  private static final String TAG = "OrchestratorClient";
  private TestDiscovery testDiscovery;
  private OrchestratedInstrumentationListener notificationRunListener;

  /**
   * A wrapper interface in order to pass us the protected {@link
   * androidx.test.runner.MonitoringInstrumentation#isPrimaryInstrProcess(String)} method.
   */
  public interface IsPrimaryInstrProcess {
    boolean test(String processName);
  }

  public boolean connect(
      Instrumentation androidJUnitRunner,
      RunnerArgs runnerArgs,
      IsPrimaryInstrProcess isPrimaryInstrProcess) {
    OrchestratorArgs args = OrchestratorArgs.from(runnerArgs);
    if (!args.isOrchestrated) {
      return false;
    }
    if (!isPrimaryInstrProcess.test(runnerArgs.targetProcess)) {
      Log.e(
          TAG,
          "Orchestration requested, but the instrumentation process isn't "
              + runnerArgs.targetProcess);
      return false;
    }
    if (args.orchestratorVersion != 1) {
      // TODO(b/138811383): AJUR support for Orchestrator v2 services coming in a followup cl
      throw new IllegalStateException("Only Orchestrator v1 currently supported");
    }
    OrchestratorConnection connection =
        new LegacyOrchestratorConnection((OnConnectListener) androidJUnitRunner);
    if (args.isTestNotificationRequested) {
      Log.v(TAG, "Test notification callbacks requested");
      TestNotificationService notificationService = (TestNotificationService) connection;
      notificationRunListener = new OrchestratedInstrumentationListener(notificationService);
    }
    if (args.isTestDiscoveryRequested) {
      Log.d(TAG, "Test discovery callbacks requested");
      TestDiscoveryService testDiscoveryService = (TestDiscoveryService) connection;
      testDiscovery = new TestDiscovery(testDiscoveryService);
    }
    connection.connect(androidJUnitRunner.getContext());
    return true;
  }

  public boolean isDiscoveryServiceConnected() {
    return testDiscovery != null;
  }

  public boolean isNotificationServiceConnected() {
    return notificationRunListener != null;
  }

  public RunListener getNotificationRunListener() {
    if (!isNotificationServiceConnected()) {
      throw new IllegalStateException(
          "Orchestrator service not connected - can't send test run notifications");
    }
    return notificationRunListener;
  }

  public void addTests(Description description) {
    if (!isDiscoveryServiceConnected()) {
      throw new IllegalStateException("Orchestrator service not connected - can't send tests");
    }
    testDiscovery.addTests(description);
  }
}
