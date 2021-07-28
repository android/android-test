/*
 * Copyright (C) 2021 The Android Open Source Project
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.services.events.TestStatus.Status;
import androidx.test.services.events.platform.TestCaseErrorEvent;
import androidx.test.services.events.platform.TestCaseFinishedEvent;
import androidx.test.services.events.platform.TestCaseStartedEvent;
import androidx.test.services.events.platform.TestPlatformEvent;
import androidx.test.services.events.platform.TestRunErrorEvent;
import androidx.test.services.events.platform.TestRunFinishedEvent;
import androidx.test.services.events.platform.TestRunStartedEvent;
import java.util.List;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link TestPlatformListener} */
@RunWith(AndroidJUnit4.class)
public class TestPlatformListenerTest {
  private static class MyTestClass {}

  private static final String ALPHA = "alpha";
  private static final String BETA = "beta";
  private static final String INIT_ERR = "initializationError";
  private static final String MY_TEST_CLASS = MyTestClass.class.getName();

  private Description testSuiteDesc;
  private Description testClassDesc;
  private Description alphaDesc;
  private Description betaDesc;

  private Description initErrSuiteDesc;
  private Description initErrClassDesc;
  private Description initErrDesc;

  private TestPlatformListener listener;
  @Mock private TestPlatformEventService stubService;
  @Captor private ArgumentCaptor<TestPlatformEvent> serviceCaptor;
  @Mock private Result runResult;

  @Before
  public void makeListener() {
    MockitoAnnotations.initMocks(this);
    listener = new TestPlatformListener(stubService);
  }

  @Test
  public void passingTests() throws Exception {
    buildRegularDescriptionTree();
    when(runResult.wasSuccessful()).thenReturn(true);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFinished(alphaDesc); // 2
    listener.testStarted(betaDesc); // 3
    listener.testFinished(betaDesc); // 4
    listener.testRunFinished(runResult); // 5
    verify(stubService, times(6)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseStartedEvent betaStart = (TestCaseStartedEvent) events.get(3);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(4);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(5);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(betaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.PASSED);
  }

  @Test
  public void testFailure() throws Exception {
    buildRegularDescriptionTree();
    RuntimeException error = new RuntimeException("Beta Failed");
    when(runResult.wasSuccessful()).thenReturn(false);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFinished(alphaDesc); // 2
    listener.testStarted(betaDesc); // 3
    listener.testFailure(new Failure(betaDesc, error)); // 4
    listener.testFinished(betaDesc); // 5
    listener.testRunFinished(runResult); // 6
    verify(stubService, times(7)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseStartedEvent betaStart = (TestCaseStartedEvent) events.get(3);
    TestCaseErrorEvent betaError = (TestCaseErrorEvent) events.get(4);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(5);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(6);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(betaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaError.error.errorMessage).isEqualTo(error.getMessage());
    assertThat(betaError.error.errorType).isEqualTo(error.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(betaError.error.stackTrace).isNotEmpty();
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.FAILED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void runnerFailure_cancelsRemainingTests() throws Exception {
    buildRegularDescriptionTree();
    RuntimeException error = new RuntimeException("Mysterious Run Failure");
    when(runResult.wasSuccessful()).thenReturn(false);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFinished(alphaDesc); // 2
    listener.testFailure(new Failure(testClassDesc, error)); // 3
    listener.testRunFinished(runResult); // 4
    verify(stubService, times(6)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestRunErrorEvent runError = (TestRunErrorEvent) events.get(3);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(4);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(5);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(runError.error.errorMessage).isEqualTo(error.getMessage());
    assertThat(runError.error.errorType).isEqualTo(error.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(runError.error.stackTrace).isNotEmpty();
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.CANCELLED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void runnerFailure_cancelsAllTests() throws Exception {
    buildRegularDescriptionTree();
    RuntimeException error = new RuntimeException("BeforeClass Failure");
    when(runResult.wasSuccessful()).thenReturn(false);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testFailure(new Failure(testClassDesc, error)); // 1
    listener.testRunFinished(runResult); // 2
    verify(stubService, times(5)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestRunErrorEvent runError = (TestRunErrorEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(3);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(4);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.CANCELLED);
    assertThat(runError.error.errorMessage).isEqualTo(error.getMessage());
    assertThat(runError.error.errorType).isEqualTo(error.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(runError.error.stackTrace).isNotEmpty();
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.CANCELLED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void runFinished_abortsActiveTest_cancelsRemainingTests() throws Exception {
    buildRegularDescriptionTree();
    when(runResult.wasSuccessful()).thenReturn(true);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    // should send testFinished x2 (once fore each test) + test run finished x 1
    listener.testRunFinished(runResult); // 3
    verify(stubService, times(5)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(3);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(4);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.ABORTED);
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.CANCELLED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.ABORTED);
  }

  @Test
  public void appCrash_duringTest_reportsTestError() throws Exception {
    buildRegularDescriptionTree();
    RuntimeException error = new RuntimeException("Beta Instrumentation Crash");
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFinished(alphaDesc); // 2
    listener.testStarted(betaDesc); // 3
    listener.reportProcessCrash(error); // 4
    verify(stubService, times(7)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseStartedEvent betaStart = (TestCaseStartedEvent) events.get(3);
    TestCaseErrorEvent betaError = (TestCaseErrorEvent) events.get(4);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(5);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(6);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(betaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaError.error.errorMessage).isEqualTo(error.getMessage());
    assertThat(betaError.error.errorType).isEqualTo(error.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(betaError.error.stackTrace).isNotEmpty();
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.FAILED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void appCrash_outsideOfTest_reportsRunnerError() throws Exception {
    buildRegularDescriptionTree();
    // JUnit isn't keeping track of this internally so it will think everything is fine
    RuntimeException error = new RuntimeException("Some Instrumentation Crash");
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFinished(alphaDesc); // 2
    listener.testStarted(betaDesc); // 3
    listener.testFinished(betaDesc); // 4
    listener.reportProcessCrash(error); // 5
    verify(stubService, times(7)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(2);
    TestCaseStartedEvent betaStart = (TestCaseStartedEvent) events.get(3);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(4);
    TestRunErrorEvent runError = (TestRunErrorEvent) events.get(5);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(6);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(betaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.PASSED);
    assertThat(runError.error.errorMessage).isEqualTo(error.getMessage());
    assertThat(runError.error.errorType).isEqualTo(error.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(runError.error.stackTrace).isNotEmpty();
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void testSkipped_ok() throws Exception {
    buildRegularDescriptionTree();
    when(runResult.wasSuccessful()).thenReturn(true);
    AssumptionViolatedException alphaSkip = new AssumptionViolatedException("skip alpha");
    AssumptionViolatedException betaSkip = new AssumptionViolatedException("skip beta");
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testAssumptionFailure(new Failure(alphaDesc, alphaSkip)); // 2
    listener.testFinished(alphaDesc); // 3
    listener.testStarted(betaDesc); // 4
    listener.testAssumptionFailure(new Failure(betaDesc, betaSkip)); // 5
    listener.testFinished(betaDesc); // 6
    listener.testRunFinished(runResult); // 7
    verify(stubService, times(8)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseStartedEvent alphaStart = (TestCaseStartedEvent) events.get(1);
    TestCaseErrorEvent alphaError = (TestCaseErrorEvent) events.get(2);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(3);
    TestCaseStartedEvent betaStart = (TestCaseStartedEvent) events.get(4);
    TestCaseErrorEvent betaError = (TestCaseErrorEvent) events.get(5);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(6);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(7);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaError.error.errorMessage).isEqualTo(alphaSkip.getMessage());
    assertThat(alphaError.error.errorType).isEqualTo(alphaSkip.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(alphaError.error.stackTrace).isNotEmpty();
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.SKIPPED);
    assertThat(betaStart.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaError.error.errorMessage).isEqualTo(betaSkip.getMessage());
    assertThat(betaError.error.errorType).isEqualTo(betaSkip.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(betaError.error.stackTrace).isNotEmpty();
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.SKIPPED);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.PASSED);
  }

  @Test
  public void initializationError_ok() throws Exception {
    buildInitializationErrorTree();
    when(runResult.wasSuccessful()).thenReturn(false);
    InitializationError initErr = new InitializationError("Malformed class");
    // Initialization error gets pushed through the Run Listener lifecycle as if it was an actual
    // test method instead of an error indicating the lack of test methods. Only send updates to
    // indicate that the test run has started, encountered an error, and stopped.
    listener.testRunStarted(initErrSuiteDesc); // 0
    listener.testStarted(initErrDesc); // 1
    listener.testFailure(new Failure(initErrClassDesc, initErr)); // 2
    listener.testFinished(initErrDesc); // 3
    listener.testRunFinished(runResult); // 4
    verify(stubService, times(3)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestRunErrorEvent runError = (TestRunErrorEvent) events.get(1);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(2);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(1);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + INIT_ERR);
    assertThat(runError.error.errorMessage).isEqualTo(initErr.getMessage());
    assertThat(runError.error.errorType).isEqualTo(initErr.getClass().getName());
    // no need to test JUnit stack trace concatenation internals here
    assertThat(runError.error.stackTrace).isNotEmpty();
    assertThat(runFinished.testRun.testCases).hasSize(1);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + INIT_ERR);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.FAILED);
  }

  @Test
  public void testIgnored_ok() throws Exception {
    buildRegularDescriptionTree();
    when(runResult.wasSuccessful()).thenReturn(true);
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testIgnored(alphaDesc); // 1
    listener.testIgnored(betaDesc); // 2
    listener.testRunFinished(runResult); // 3
    verify(stubService, times(4)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunStartedEvent runStart = (TestRunStartedEvent) events.get(0);
    TestCaseFinishedEvent alphaFinished = (TestCaseFinishedEvent) events.get(1);
    TestCaseFinishedEvent betaFinished = (TestCaseFinishedEvent) events.get(2);
    TestRunFinishedEvent runFinished = (TestRunFinishedEvent) events.get(3);
    assertThat(runStart.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runStart.testRun.testCases).hasSize(2);
    assertThat(runStart.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runStart.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(alphaFinished.testCase.getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(alphaFinished.testStatus.status).isEqualTo(Status.IGNORED);
    assertThat(betaFinished.testCase.getClassAndMethodName()).isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(betaFinished.testStatus.status).isEqualTo(Status.IGNORED);
    assertThat(runFinished.testRun.testRunName).isEqualTo(MY_TEST_CLASS);
    assertThat(runFinished.testRun.testCases).hasSize(2);
    assertThat(runFinished.testRun.testCases.get(0).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + ALPHA);
    assertThat(runFinished.testRun.testCases.get(1).getClassAndMethodName())
        .isEqualTo(MY_TEST_CLASS + "#" + BETA);
    assertThat(runFinished.runStatus.status).isEqualTo(Status.PASSED);
  }

  @Test
  public void testFailure_largeMessage() throws Exception {
    buildRegularDescriptionTree();
    listener.testRunStarted(testSuiteDesc); // 0
    listener.testStarted(alphaDesc); // 1
    listener.testFailure(new Failure(alphaDesc, new Exception(getVeryLargeString(1000000)))); // 2
    listener.testFinished(alphaDesc); // 3
    listener.testRunFinished(runResult); // 4

    verify(stubService, times(6)).send(serviceCaptor.capture());
    TestCaseErrorEvent testError = (TestCaseErrorEvent) serviceCaptor.getAllValues().get(2);
    // verify message is truncated
    assertThat(testError.error.errorMessage.length()).isLessThan(100000);
  }

  @Test
  public void crashBeforeTestRun() throws TestEventClientException {
    listener.reportProcessCrash(new RuntimeException("Crash before test run"));

    // Verifies a test failure event & test run finished event were sent
    verify(stubService, times(2)).send(serviceCaptor.capture());
    List<TestPlatformEvent> events = serviceCaptor.getAllValues();
    TestRunErrorEvent failureEvent = (TestRunErrorEvent) events.get(0);
    assertThat(failureEvent.testRun.testRunName).isEqualTo("No Tests");
    assertThat(failureEvent.error.errorType).isEqualTo("java.lang.RuntimeException");
    TestRunFinishedEvent runFinishedEvent = (TestRunFinishedEvent) events.get(1);
    assertThat(runFinishedEvent.testRun.testRunName).isEqualTo("No Tests");
    assertThat(runFinishedEvent.runStatus.status).isEqualTo(Status.FAILED);
  }

  private void buildRegularDescriptionTree() {
    // AJUR top level description name is "null". This might be a bug, but it's also probably going
    // to be one of the weirdest Description trees we may encounter. Better to just defensively test
    // this component to ensure it works with other Runners as well.
    testSuiteDesc = Description.createSuiteDescription("null");
    testClassDesc = Description.createSuiteDescription(MyTestClass.class);
    alphaDesc = Description.createTestDescription(MY_TEST_CLASS, ALPHA, ALPHA);
    betaDesc = Description.createTestDescription(MY_TEST_CLASS, BETA, BETA);

    // "null"
    //  |--> MyTestClass
    //       |--> MyTestClass.alpha
    //       |--> MyTestClass.beta
    testSuiteDesc.addChild(testClassDesc);
    testClassDesc.addChild(alphaDesc);
    testClassDesc.addChild(betaDesc);
  }

  private void buildInitializationErrorTree() {
    // InitializationError is treated as its own method in terms of Description structure
    initErrSuiteDesc = Description.createSuiteDescription("null");
    initErrClassDesc = Description.createSuiteDescription(MyTestClass.class);
    initErrDesc = Description.createTestDescription(MY_TEST_CLASS, INIT_ERR, INIT_ERR);
    initErrSuiteDesc.addChild(initErrClassDesc);
    initErrClassDesc.addChild(initErrDesc);
  }

  private static String getVeryLargeString(int size) {
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) {
      sb.append('a');
    }
    return sb.toString();
  }
}
