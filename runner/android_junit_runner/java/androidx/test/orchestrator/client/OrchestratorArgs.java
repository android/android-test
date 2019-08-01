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

import androidx.test.internal.runner.RunnerArgs;

/**
 * Looks at the args in {@link RunnerArgs} and determines which Orchestrator services are needed.
 */
public class OrchestratorArgs {
  final boolean isOrchestrated;
  final boolean isTestDiscoveryRequested;
  final boolean isTestNotificationRequested;
  final int orchestratorVersion;

  private OrchestratorArgs(
      boolean isOrchestrated,
      boolean isTestDiscoveryRequested,
      boolean isTestNotificationRequested,
      int orchestratorVersion) {
    this.isOrchestrated = isOrchestrated;
    this.isTestDiscoveryRequested = isTestDiscoveryRequested;
    this.isTestNotificationRequested = isTestNotificationRequested;
    this.orchestratorVersion = orchestratorVersion;
  }

  public static OrchestratorArgs from(RunnerArgs runnerArgs) {
    int version = 0;
    boolean testDiscoveryRequested = false;
    boolean testNotificationRequested = false;

    if (runnerArgs.testDiscoveryService != null) {
      version = 2;
      testDiscoveryRequested = true;
    } else if (runnerArgs.testNotificationService != null) {
      version = 2;
      testNotificationRequested = true;
    }

    if (runnerArgs.orchestratorService != null) {
      version = 1;
      if (runnerArgs.listTestsForOrchestrator) {
        testDiscoveryRequested = true;
      } else {
        testNotificationRequested = true;
      }
    }
    return new OrchestratorArgs(
        version > 0, testDiscoveryRequested, testNotificationRequested, version);
  }
}
