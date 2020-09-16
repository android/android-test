/*
 * Copyright (C) 2017 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.services.events.internal.StackTrimmer;
import androidx.test.services.events.run.TestAssumptionFailureEvent;
import androidx.test.services.events.run.TestFailureEvent;
import androidx.test.services.events.run.TestFinishedEvent;
import androidx.test.services.events.run.TestIgnoredEvent;
import androidx.test.services.events.run.TestRunEvent;
import androidx.test.services.events.run.TestRunEventWithTestCase;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import androidx.test.services.events.run.TestStartedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OrchestratedInstrumentationListener}. */
@RunWith(AndroidJUnit4.class)
public class OrchestratedInstrumentationListenerTest {
  @Mock TestRunEventService testRunEventService;

  private OrchestratedInstrumentationListener listener;
  private Description jUnitDescription;
  private Failure jUnitFailure;
  private Result jUnitResult;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    listener = new OrchestratedInstrumentationListener(testRunEventService);

    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    jUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    jUnitFailure = new Failure(jUnitDescription, new Throwable("error"));
    jUnitResult = new Result();
    RunListener jUnitListener = jUnitResult.createListener();
    jUnitListener.testRunStarted(jUnitDescription);
    jUnitListener.testStarted(jUnitDescription);
    jUnitListener.testFinished(jUnitDescription);
  }

  @Test
  public void testRunStarted() throws TestEventClientException {
    listener.testRunStarted(jUnitDescription);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestRunStartedEvent event = (TestRunStartedEvent) argument.getValue();
    compareDescription(event, jUnitDescription);
  }

  @Test
  public void testRunFinished() throws TestEventClientException {
    listener.testRunFinished(jUnitResult);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestRunFinishedEvent event = (TestRunFinishedEvent) argument.getValue();
    assertThat(event.count).isEqualTo(1);
  }

  @Test
  public void testStarted() throws TestEventClientException {
    listener.testStarted(jUnitDescription);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestStartedEvent event = (TestStartedEvent) argument.getValue();
    compareDescription(event, jUnitDescription);
  }

  @Test
  public void testFinished() throws TestEventClientException {
    listener.testFinished(jUnitDescription);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestFinishedEvent event = (TestFinishedEvent) argument.getValue();
    compareDescription(event, jUnitDescription);
  }

  @Test
  public void testFailure() throws TestEventClientException {
    listener.testFailure(jUnitFailure);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestFailureEvent event = (TestFailureEvent) argument.getValue();
    compareFailure(event, jUnitFailure);
  }

  @Test
  public void testFailureWithNoFailureMessage() throws TestEventClientException {
    String nullError = null;
    Failure jUnitFailureWithNoErrorMessage =
        new Failure(jUnitDescription, new Throwable(nullError));
    listener.testFailure(jUnitFailureWithNoErrorMessage);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestFailureEvent event = (TestFailureEvent) argument.getValue();
    compareFailure(event, jUnitFailureWithNoErrorMessage);
  }

  @Test
  public void testAssumptionFailure() throws TestEventClientException {
    listener.testAssumptionFailure(jUnitFailure);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestAssumptionFailureEvent event = (TestAssumptionFailureEvent) argument.getValue();
    compareFailure(event, jUnitFailure);
  }

  @Test
  public void testIgnored() throws TestEventClientException {
    listener.testIgnored(jUnitDescription);
    ArgumentCaptor<TestRunEvent> argument = ArgumentCaptor.forClass(TestRunEvent.class);
    verify(testRunEventService).send(argument.capture());

    TestIgnoredEvent event = (TestIgnoredEvent) argument.getValue();
    compareDescription(event, jUnitDescription);
  }

  private static void compareDescription(
      TestRunEventWithTestCase event, Description jUnitDescription) {
    assertThat(event.testCase.className).isEqualTo(jUnitDescription.getClassName());
    assertThat(event.testCase.methodName).isEqualTo(jUnitDescription.getMethodName());
  }

  private static void compareFailure(TestFailureEvent event, Failure jUnitFailure) {
    assertThat(event.failure.stackTrace).isEqualTo(StackTrimmer.getTrimmedStackTrace(jUnitFailure));
    compareDescription(event, jUnitFailure.getDescription());
  }
}
