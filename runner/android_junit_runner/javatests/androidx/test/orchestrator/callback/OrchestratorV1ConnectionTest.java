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

package androidx.test.orchestrator.callback;

import static androidx.test.orchestrator.listeners.OrchestrationListenerManager.KEY_TEST_EVENT;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.os.RemoteException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.events.client.TestEventClientConnectListener;
import androidx.test.internal.events.client.TestEventClientException;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.services.events.TestCaseInfo;
import androidx.test.services.events.discovery.TestFoundEvent;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OrchestratorV1Connection}. */
@RunWith(AndroidJUnit4.class)
public class OrchestratorV1ConnectionTest {
  @Mock private TestEventClientConnectListener mockConnectListener;
  @Mock private OrchestratorCallback mockCallback;

  private OrchestratorV1Connection connection;
  private TestCaseInfo testCase;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    connection = new OrchestratorV1Connection(mockConnectListener);
    connection.service = mockCallback;
    testCase = new TestCaseInfo("foo.FooTest", "sampleTest", emptyList(), emptyList());
  }

  @Test
  public void send_parcelableDescriptionWrappedInBundle()
      throws RemoteException, TestEventClientException {

    connection.send(new TestRunStartedEvent(testCase));

    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());
    Bundle bundle = argument.getValue();
    ParcelableDescription result = BundleJUnitUtils.getDescription(bundle);

    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_RUN_STARTED"));
    assertThat(result.getClassName(), is("foo.FooTest"));
    assertThat(result.getMethodName(), is("sampleTest"));
  }

  @Test
  public void send_throwsIfNotConnected() {
    connection.service = null;
    try {
      connection.send(new TestRunFinishedEvent(1, 2, 3, emptyList()));
      fail("Expected send to throw IllegalStateException");
    } catch (TestEventClientException e) {
      // Ignore, we expect this.
    }
  }

  @Test
  public void addTest_sendsCombinedClassAndMethodNames()
      throws RemoteException, TestEventClientException {
    connection.send(new TestFoundEvent(testCase));
    verify(mockCallback).addTest("foo.FooTest#sampleTest");
  }

  @Test
  public void addTest_throwsIfNotConnected() {
    connection.service = null;
    try {
      connection.send(new TestFoundEvent(testCase));
      fail("Expected send to throw IllegalStateException");
    } catch (TestEventClientException e) {
      // Ignore, we expect this.
    }
  }
}
