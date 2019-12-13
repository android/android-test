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

/** Unit tests for {@link androidx.test.services.events.client.TestEventClient}. */
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
  public void connect_noOrchestrationServiceProvided_returnsFalse() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, null);
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    boolean result = client.connect(context, listener, args);

    assertThat(result, is(false));
    assertThat(client.isDiscoveryServiceConnected(), is(false));
    assertThat(client.isNotificationServiceConnected(), is(false));
  }

  @Test
  public void connect_orchestrationServiceProvided_returnsTrue() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    boolean result = client.connect(context, listener, args);

    assertThat(result, is(true));
  }

  @Test
  public void isNotificationServiceConnected_listTestsArgFalse_returnsTrue() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    boolean result = client.connect(context, listener, args);

    assertThat(result, is(true));
    assertThat(client.isNotificationServiceConnected(), is(true));
  }

  @Test
  public void isNotificationServiceConnected_listTestsArgTrue_returnsFalse() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);

    assertThat(client.isNotificationServiceConnected(), is(false));
  }

  @Test
  public void isDiscoveryServiceConnected_listTestsArgFalse_returnsFalse() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);

    assertThat(client.isDiscoveryServiceConnected(), is(false));
  }

  @Test
  public void isDiscoveryServiceConnected_listTestsArgTrue_returnsTrue() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);

    assertThat(client.isDiscoveryServiceConnected(), is(true));
  }

  @Test
  public void getNotificationRunListener_listTestsArgTrue_returnsNull() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);

    assertThat(client.getNotificationRunListener(), nullValue());
  }

  @Test
  public void getNotificationRunListener_listTestsArgFalse_returnsInstance() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);

    assertThat(client.getNotificationRunListener(), instanceOf(RunListener.class));
  }

  @Test
  public void addTests_listTestsArgFalse_doesNotThrow() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);
    client.addTests(Description.createTestDescription(getClass(), "sampleTest"));
  }

  @Test
  public void addTests_connected_doesNotThrow() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    TestEventClientArgs args = argsFromBundle(bundle);

    TestEventClient client = new TestEventClient();

    client.connect(context, listener, args);
    client.addTests(Description.createTestDescription(getClass(), "sampleTest"));
  }

  @Test
  public void addTests_notConnected_doesNotThrow() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "foo.OrchestratorService");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");

    TestEventClient client = new TestEventClient();

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
      implements OrchestratorConnection, TestRunEventService, TestDiscoveryEventService {

    @Override
    public void connect(@NonNull Context context) {}

    @Override
    public void send(@NonNull TestDiscoveryEvent testDiscoveryEvent) {}

    @Override
    public void send(@NonNull TestRunEvent event) {}
  }
}
