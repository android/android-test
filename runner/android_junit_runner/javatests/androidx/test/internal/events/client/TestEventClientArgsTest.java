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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link TestEventClientArgs}. */
@RunWith(AndroidJUnit4.class)
public class TestEventClientArgsTest {
  private static final String ARGUMENT_ORCHESTRATOR_SERVICE = "orchestratorService";
  private static final String ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR = "listTestsForOrchestrator";
  private static final String ARGUMENT_DISCOVERY_SERVICE = "testDiscoveryService";
  private static final String ARGUMENT_RUN_EVENTS_SERVICE = "testRunEventService";

  @Test
  public void noServiceSpecified_noOrchestration() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(false));
    assertThat(args.orchestratorVersion, is(0));
    assertThat(args.isTestDiscoveryRequested, is(false));
    assertThat(args.isTestRunEventsRequested, is(false));
  }

  @Test
  public void serviceSpecified_notificationRequested() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(true));
    assertThat(args.orchestratorVersion, is(1));
    assertThat(args.isTestDiscoveryRequested, is(false));
    assertThat(args.isTestRunEventsRequested, is(true));
  }

  @Test
  public void serviceSpecified_discoveryRequested() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(true));
    assertThat(args.orchestratorVersion, is(1));
    assertThat(args.isTestDiscoveryRequested, is(true));
    assertThat(args.isTestRunEventsRequested, is(false));
  }

  @Test
  public void discoveryServiceSpecified_v2_discoveryRequested() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_DISCOVERY_SERVICE, "foo.DiscoveryService");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(true));
    assertThat(args.orchestratorVersion, is(2));
    assertThat(args.isTestDiscoveryRequested, is(true));
    assertThat(args.isTestRunEventsRequested, is(false));
  }

  @Test
  public void runEventServiceSpecified_v2_runEventsRequested() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_RUN_EVENTS_SERVICE, "foo.RunEventService");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(true));
    assertThat(args.orchestratorVersion, is(2));
    assertThat(args.isTestDiscoveryRequested, is(false));
    assertThat(args.isTestRunEventsRequested, is(true));
  }

  @Test
  public void conflictingV1andV2flagsSpecified_pickV2() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    bundle.putString(ARGUMENT_RUN_EVENTS_SERVICE, "foo.RunEventService");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(true));
    assertThat(args.orchestratorVersion, is(2));
    assertThat(args.isTestDiscoveryRequested, is(false));
    assertThat(args.isTestRunEventsRequested, is(true));
  }

  private static TestEventClientArgs argsFromBundle(Bundle bundle) {
    String testDiscoveryService = bundle.getString(ARGUMENT_DISCOVERY_SERVICE);
    String testRunEventService = bundle.getString(ARGUMENT_RUN_EVENTS_SERVICE);
    return argsFromBundle(bundle, testDiscoveryService, testRunEventService);
  }

  private static TestEventClientArgs argsFromBundle(
      Bundle bundle, String testDiscoveryService, String testRunEventService) {
    RunnerArgs runnerArgs =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
            .build();
    // TODO(b/138811383): support for the v2 services in RunnerArgs will be added in a following CL
    TestEventClientArgs.Builder builder = builderFromRunnerArgs(runnerArgs);
    builder.setTestDiscoveryService(testDiscoveryService);
    builder.setTestRunEventService(testRunEventService);
    return builder.build();
  }

  private static TestEventClientArgs.Builder builderFromRunnerArgs(RunnerArgs runnerArgs) {
    return TestEventClientArgs.builder()
        .setOrchestratorService(runnerArgs.orchestratorService)
        .setTestDiscoveryRequested(runnerArgs.listTestsForOrchestrator)
        .setTestRunEventsRequested(runnerArgs.orchestratorService != null)
        .setConnectionFactory(listener -> context -> {}); // Create a fake v1 connection
  }
}
