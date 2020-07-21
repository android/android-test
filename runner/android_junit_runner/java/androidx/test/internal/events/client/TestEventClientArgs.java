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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Provides arguments to {@link TestEventClient#connect(Context, TestEventClientConnectListener,
 * TestEventClientArgs)} to determine which Orchestrator service connections are needed, if any.
 *
 * <p>The {@link Builder#build()} call will determine which Orchestrator version to use depending on
 * which arguments are provided.
 */
public final class TestEventClientArgs {
  public final boolean isOrchestrated;
  public final boolean isPrimaryInstrProcess;
  public final boolean isTestDiscoveryRequested;
  public final boolean isTestRunEventsRequested;
  public final int orchestratorVersion;
  @Nullable public final String testDiscoveryService;
  @Nullable public final String testRunEventService;
  @Nullable public final ConnectionFactory connectionFactory;

  private TestEventClientArgs(
      boolean isOrchestrated, int orchestratorVersion, @NonNull Builder builder) {
    this.isOrchestrated = isOrchestrated;
    this.isPrimaryInstrProcess = builder.isPrimaryInstProcess;
    this.isTestDiscoveryRequested = builder.testDiscoveryRequested;
    this.isTestRunEventsRequested = builder.testRunEventsRequested;
    this.testDiscoveryService = builder.testDiscoveryService;
    this.testRunEventService = builder.testRunEventService;
    this.connectionFactory = builder.connectionFactory;
    this.orchestratorVersion = orchestratorVersion;
  }

  /** Creates a new {@link TestEventClientArgs.Builder} instance. */
  @NonNull
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
    @Nullable private ConnectionFactory connectionFactory;
    @Nullable private String orchestratorService;
    @Nullable private String testDiscoveryService;
    @Nullable private String testRunEventService;

    /**
     * Indicates whether this is the primary instrumentation process, as returned by {@code
     * MonitoringInstrumentation.isPrimaryInstrProcess(RunnerArgs)}.
     */
    @NonNull
    public Builder setPrimaryInstProcess(boolean isPrimaryInstProcess) {
      this.isPrimaryInstProcess = isPrimaryInstProcess;
      return this;
    }

    /**
     * If any non-empty value is present, then use Orchestrator v1 ({@code
     * androidx.test.orchestrator.OrchestratorService}) for test discovery or test run event
     * notifications. The actual value of {@link #orchestratorService} isn't used.
     *
     * <p>Requires one of {@link #testDiscoveryRequested} or {@link #testRunEventsRequested} to be
     * set to {@code true}.
     */
    @NonNull
    public Builder setOrchestratorService(@Nullable String orchestratorService) {
      this.orchestratorService = orchestratorService;
      return this;
    }

    /** Discover all available tests and send them back to the Orchestrator. */
    @NonNull
    public Builder setTestDiscoveryRequested(boolean discoveryRequested) {
      this.testDiscoveryRequested = discoveryRequested;
      return this;
    }

    /** Send test run status updates to the Orchestrator. */
    @NonNull
    public Builder setTestRunEventsRequested(boolean runEventsRequested) {
      this.testRunEventsRequested = runEventsRequested;
      return this;
    }

    /**
     * Discover available tests and send {@link
     * androidx.test.services.events.discovery.TestDiscoveryEvent}s back to the Orchestrator v2
     * {@code androidx.test.services.events.discovery.ITestDiscoveryEvent} service.
     *
     * <p>This implicitly sets {@link #testDiscoveryRequested} to {@code true}.
     */
    @NonNull
    public Builder setTestDiscoveryService(@Nullable String testDiscoveryService) {
      this.testDiscoveryService = testDiscoveryService;
      return this;
    }

    /**
     * Send {@link androidx.test.services.events.run.TestRunEvent}s back to the Orchestrator v2
     * {@code androidx.test.services.events.run.ITestRunEvent} service.
     *
     * <p>This implicitly sets {@link #testRunEventsRequested} to {@code true}.
     */
    @NonNull
    public Builder setTestRunEventService(@Nullable String testRunEventService) {
      this.testRunEventService = testRunEventService;
      return this;
    }

    /**
     * If a custom connectionFactory instance is provided, then {@link TestEventClient} will use
     * this factory to create the {@link TestEventServiceConnection}. This is used by {@code
     * AndroidJUnitRunner} to override the connection to use for the legacy v1 {@code
     * androidx.test.orchestrator.OrchestratorService}, i.e. when argument {@link
     * #orchestratorService} is provided.
     */
    @NonNull
    public Builder setConnectionFactory(@Nullable ConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
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
      if (testDiscoveryService != null && !testDiscoveryService.isEmpty()) {
        version = 2;
        testDiscoveryRequested = true;
        testRunEventsRequested = false;
      } else if (testRunEventService != null && !testRunEventService.isEmpty()) {
        version = 2;
        testRunEventsRequested = true;
        testDiscoveryRequested = false;
      } else if (orchestratorService != null) {
        if (connectionFactory == null) {
          Log.w(
              TAG,
              "Orchestrator service ["
                  + orchestratorService
                  + "] argument given, but no connectionFactory was provided for the v1 service");
        } else if (testDiscoveryRequested || testRunEventsRequested) {
          version = 1;
        } else {
          Log.w(
              TAG,
              "Orchestrator service ["
                  + orchestratorService
                  + "] argument given, but neither test discovery nor run event services "
                  + "was requested");
        }
      } else {
        Log.v(
            TAG,
            "No service name argument was given (testDiscoveryService, "
                + "testRunEventService or orchestratorService)");
        testDiscoveryRequested = false;
        testRunEventsRequested = false;
      }
      if (testDiscoveryRequested && testRunEventsRequested) {
        Log.w(TAG, "Can't use both the test discovery and run event services simultaneously");
        testRunEventsRequested = false;
      }
      if (version > 0) {
        Log.v(TAG, "Connecting to Orchestrator v" + version);
      }
      return new TestEventClientArgs(version > 0, version, this);
    }
  }

  /**
   * A client may optionally provide a factory for the remote service connection. This is required
   * when using the Orchestrator v1 service from {@code AndroidJUnitRunner}.
   */
  public interface ConnectionFactory {

    /**
     * Creates a new connection, e.g. {@code OrchestratorV1Connection} attached to the specified
     * listener.
     */
    @NonNull
    TestEventServiceConnection create(@NonNull TestEventClientConnectListener listener);
  }
}
