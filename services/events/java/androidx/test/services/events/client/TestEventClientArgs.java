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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Provides arguments to {@link TestEventClient#connect(Context, TestEventClientArgs)} to determine
 * which Orchestrator service connections are needed, if any.
 */
public final class TestEventClientArgs {
  public final boolean isOrchestrated;
  public final boolean isPrimaryInstrProcess;
  public final boolean isTestDiscoveryRequested;
  public final boolean isTestNotificationRequested;
  public final int orchestratorVersion;
  @Nullable public final OrchestratorConnection connection;

  private TestEventClientArgs(
      boolean isOrchestrated, int orchestratorVersion, @NonNull Builder builder) {
    this.isOrchestrated = isOrchestrated;
    this.isPrimaryInstrProcess = builder.isPrimaryInstProcess;
    this.isTestDiscoveryRequested = builder.testDiscoveryRequested;
    this.isTestNotificationRequested = builder.testRunEventsRequested;
    this.connection = builder.connection;
    this.orchestratorVersion = orchestratorVersion;
  }

  /** Creates a new {@link TestEventClientArgs.Builder} instance. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link TestEventClientArgs} instance using the provided arguments. Use the
   * applicable setters to configure the arguments then call {@link #build()} to create the new
   * {@link TestEventClientArgs}.
   */
  public static class Builder {
    private static final String TAG = "TestEventClient";
    boolean isPrimaryInstProcess = true;
    boolean testDiscoveryRequested = false;
    boolean testRunEventsRequested = false;
    @Nullable private OrchestratorConnection connection;
    @Nullable private String orchestratorService;

    /**
     * Indicates whether this is the primary instrumentation process, as returned by {@code
     * MonitoringInstrumentation.isPrimaryInstrProcess(RunnerArgs)}.
     */
    public Builder setPrimaryInstProcess(boolean isPrimaryInstProcess) {
      this.isPrimaryInstProcess = isPrimaryInstProcess;
      return this;
    }

    /**
     * If any non-empty value is present, then use Orchestrator v1 ({@code
     * androidx.test.orchestrator.OrchestratorService}). The actual value of {@link
     * #orchestratorService} isn't used.
     */
    public Builder setOrchestratorService(@Nullable String orchestratorService) {
      this.orchestratorService = orchestratorService;
      return this;
    }

    /** Discover all available tests and send them back to the Orchestrator. */
    public Builder setTestDiscoveryRequested(boolean discoveryRequested) {
      this.testDiscoveryRequested = discoveryRequested;
      return this;
    }

    /** Send test run status updates to the Orchestrator. */
    public Builder setTestRunEventsRequested(boolean runEventsRequested) {
      this.testRunEventsRequested = runEventsRequested;
      return this;
    }

    /**
     * If a custom connection instance is provided, then {@link TestEventClient} won't attempt to
     * create a new Orchestrator v2 connection. This is used by {@code AndroidJUnitRunner} to
     * override the connection to use the legacy v1 {@code
     * androidx.test.orchestrator.OrchestratorService}.
     */
    public Builder setConnection(@Nullable OrchestratorConnection connection) {
      this.connection = connection;
      return this;
    }

    /**
     * Determines which operation to perform (test discovery, or test run event notifications) by
     * evaluating the provided arguments.
     *
     * @return a new {@link TestEventClientArgs} instance
     */
    @NonNull
    public TestEventClientArgs build() {
      int version = 0;
      // TODO(b/138811383): AJUR support for Orchestrator v2 services coming in a followup cl
      if (orchestratorService != null) {
        if (testDiscoveryRequested && testRunEventsRequested) {
          Log.w(TAG, "Can't use both the test discovery and run event services simultaneously");
          testRunEventsRequested = false;
        }
        if (testDiscoveryRequested || testRunEventsRequested) {
          version = 1;
        }
        if (version > 0) {
          Log.v(TAG, "Using Orchestrator v" + version);
        } else {
          Log.w(
              TAG,
              "Orchestrator service ["
                  + orchestratorService
                  + "] argument given, but neither test discovery nor run event services "
                  + "was requested");
        }
      } else {
        Log.v(TAG, "No orchestratorService argument was given");
        testDiscoveryRequested = false;
        testRunEventsRequested = false;
      }
      return new TestEventClientArgs(version > 0, version, this);
    }
  }
}
