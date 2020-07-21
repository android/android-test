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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.services.events.discovery.TestDiscoveryEvent;
import androidx.test.services.events.run.TestRunEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TestEventClient}. */
@RunWith(AndroidJUnit4.class)
public class TestEventClientTest {
  private static final String ARGUMENT_ORCHESTRATOR_SERVICE = "orchestratorService";
  private static final String ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR = "listTestsForOrchestrator";

  private final Context context = ApplicationProvider.getApplicationContext();

  @Mock private TestEventClientConnectListener listener;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void connect_noOrchestrationServiceProvided_returnsNoOpClient() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, null);
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.isTestDiscoveryEnabled(), is(false));
    assertThat(client.isTestRunEventsEnabled(), is(false));
  }

  @Test
  public void connect_onlyOrchestrationServiceProvided_returnsRunEventsClient() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.isTestDiscoveryEnabled(), is(false));
    assertThat(client.isTestRunEventsEnabled(), is(true));
  }

  @Test
  public void connect_orchestrationServiceProvided_listTestsArgFalse_returnsRunEventsClient() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.isTestDiscoveryEnabled(), is(false));
    assertThat(client.isTestRunEventsEnabled(), is(true));
  }

  @Test
  public void connect_orchestrationServiceProvided_listTestsArgTrue_returnsDiscoveryClient() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.isTestDiscoveryEnabled(), is(true));
    assertThat(client.isTestRunEventsEnabled(), is(false));
  }

  @Test
  public void getNotificationRunListener_listTestsArgTrue_returnsNull() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.getNotificationRunListener(), nullValue());
  }

  @Test
  public void getNotificationRunListener_listTestsArgFalse_returnsRunListenerInstance() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    assertThat(client.getNotificationRunListener(), instanceOf(RunListener.class));
  }

  @Test
  public void addTests_listTestsArgFalse_doesNotThrow() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    client.addTests(Description.createTestDescription(getClass(), "sampleTest"));
  }

  @Test
  public void addTests_listTestsArgTrue_doesNotThrow() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = TestEventClient.connect(context, listener, args);

    client.addTests(Description.createTestDescription(getClass(), "sampleTest"));
  }

  private static TestEventClientArgs argsFromBundle(Bundle bundle) {
    RunnerArgs runnerArgs =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
            .build();
    return TestEventClientArgs.builder()
        .setOrchestratorService(runnerArgs.orchestratorService)
        .setTestDiscoveryRequested(runnerArgs.listTestsForOrchestrator)
        .setTestRunEventsRequested(!runnerArgs.listTestsForOrchestrator)
        .setConnectionFactory(listener -> new FakeConnection())
        .build();
  }

  private static class FakeConnection
      implements TestEventServiceConnection, TestRunEventService, TestDiscoveryEventService {

    @Override
    public void connect(@NonNull Context context) {}

    @Override
    public void send(@NonNull TestDiscoveryEvent testDiscoveryEvent) {}

    @Override
    public void send(@NonNull TestRunEvent event) {}
  }
}
