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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.services.events.discovery.TestDiscoveryErrorEvent;
import androidx.test.services.events.discovery.TestDiscoveryEvent;
import androidx.test.services.events.discovery.TestDiscoveryFinishedEvent;
import androidx.test.services.events.discovery.TestDiscoveryStartedEvent;
import androidx.test.services.events.discovery.TestFoundEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests fpr {@link TestDiscoveryListener}. */
@RunWith(AndroidJUnit4.class)
public class TestDiscoveryListenerTest {
  @Mock TestEventClientConnectListener mockConnectListener;
  @Mock TestDiscoveryEventService discoveryEventService;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFound() throws TestEventClientException {
    TestDiscoveryListener testDiscoveryListener = new TestDiscoveryListener(discoveryEventService);
    Description testDescription = Description.createTestDescription(getClass(), "sampleTest");

    testDiscoveryListener.testRunStarted(Description.EMPTY);
    testDiscoveryListener.testFinished(testDescription);
    testDiscoveryListener.testRunFinished(new Result());

    ArgumentCaptor<TestDiscoveryEvent> argument = ArgumentCaptor.forClass(TestDiscoveryEvent.class);
    verify(discoveryEventService, times(3)).send(argument.capture());

    assertThat(argument.getAllValues().get(0), instanceOf(TestDiscoveryStartedEvent.class));
    TestFoundEvent infoEvent = (TestFoundEvent) argument.getAllValues().get(1);
    assertThat(argument.getAllValues().get(2), instanceOf(TestDiscoveryFinishedEvent.class));

    assertThat(
        infoEvent.testCase.getClassAndMethodName(),
        is(getClass().getCanonicalName() + "#sampleTest"));
  }

  @Test
  public void testFailure_initError() throws TestEventClientException {
    ArgumentCaptor<TestDiscoveryEvent> argument = ArgumentCaptor.forClass(TestDiscoveryEvent.class);
    TestDiscoveryListener testDiscoveryListener = new TestDiscoveryListener(discoveryEventService);
    Description testDescription = Description.createTestDescription("a.b", "initializationError");

    testDiscoveryListener.testRunStarted(Description.EMPTY);
    testDiscoveryListener.testFailure(new Failure(testDescription, new RuntimeException()));
    testDiscoveryListener.testFinished(testDescription);
    testDiscoveryListener.testRunFinished(new Result());

    verify(discoveryEventService, times(3)).send(argument.capture());
    assertThat(argument.getAllValues().get(0), instanceOf(TestDiscoveryStartedEvent.class));
    assertThat(argument.getAllValues().get(1), instanceOf(TestDiscoveryErrorEvent.class));
    assertThat(argument.getAllValues().get(2), instanceOf(TestDiscoveryFinishedEvent.class));
    verifyNoMoreInteractions(discoveryEventService);
  }

  @Test
  public void reportProcessCrashed() throws TestEventClientException {
    ArgumentCaptor<TestDiscoveryEvent> argument = ArgumentCaptor.forClass(TestDiscoveryEvent.class);
    TestDiscoveryListener testDiscoveryListener = new TestDiscoveryListener(discoveryEventService);

    boolean result = testDiscoveryListener.reportProcessCrash(new RuntimeException());

    assertTrue(result);
    verify(discoveryEventService, times(3)).send(argument.capture());
    assertThat(argument.getAllValues().get(0), instanceOf(TestDiscoveryStartedEvent.class));
    assertThat(argument.getAllValues().get(1), instanceOf(TestDiscoveryErrorEvent.class));
    assertThat(argument.getAllValues().get(2), instanceOf(TestDiscoveryFinishedEvent.class));
    verifyNoMoreInteractions(discoveryEventService);
  }
}
