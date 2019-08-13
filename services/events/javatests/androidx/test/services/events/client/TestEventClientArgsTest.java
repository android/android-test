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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link androidx.test.services.events.client.TestEventClientArgs}. */
@RunWith(AndroidJUnit4.class)
public class TestEventClientArgsTest {
  private static final String ARGUMENT_ORCHESTRATOR_SERVICE = "orchestratorService";
  private static final String ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR = "listTestsForOrchestrator";

  @Test
  public void noServiceSpecified_noOrchestration() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");

    TestEventClientArgs args = argsFromBundle(bundle);

    assertThat(args.isOrchestrated, is(false));
    assertThat(args.orchestratorVersion, is(0));
    assertThat(args.isTestDiscoveryRequested, is(false));
    assertThat(args.isTestNotificationRequested, is(false));
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
    assertThat(args.isTestNotificationRequested, is(true));
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
    assertThat(args.isTestNotificationRequested, is(false));
  }

  private static TestEventClientArgs argsFromBundle(Bundle bundle) {
    RunnerArgs runnerArgs =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
            .build();
    return getEventArgs(runnerArgs);
  }

  private static TestEventClientArgs getEventArgs(RunnerArgs runnerArgs) {
    return TestEventClientArgs.builder()
        .setOrchestratorService(runnerArgs.orchestratorService)
        .setTestDiscoveryRequested(runnerArgs.listTestsForOrchestrator)
        .setTestRunEventsRequested(runnerArgs.orchestratorService != null)
        .setConnection(
            context -> {
              // Fake service connection
            })
        .build();
  }
}
