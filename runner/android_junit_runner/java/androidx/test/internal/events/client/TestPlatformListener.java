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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.test.services.events.ErrorInfo;
import androidx.test.services.events.ParcelableConverter;
import androidx.test.services.events.TestCaseInfo;
import androidx.test.services.events.TestEventException;
import androidx.test.services.events.TestRunInfo;
import androidx.test.services.events.TestStatus;
import androidx.test.services.events.TestStatus.Status;
import androidx.test.services.events.TimeStamp;
import androidx.test.services.events.platform.TestCaseErrorEvent;
import androidx.test.services.events.platform.TestCaseFinishedEvent;
import androidx.test.services.events.platform.TestCaseStartedEvent;
import androidx.test.services.events.platform.TestPlatformEvent;
import androidx.test.services.events.platform.TestRunErrorEvent;
import androidx.test.services.events.platform.TestRunFinishedEvent;
import androidx.test.services.events.platform.TestRunStartedEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * This {@link RunListener} is for Android-based JUnit clients to speak with services that use the
 * {@link TestPlatformEvent} protocol.
 */
public final class TestPlatformListener extends RunListener {
  private static final String TAG = "TestPlatformListener";
  private static final String INIT_ERROR = "initializationError";
  private final TestPlatformEventService notificationService;
  private Map<Description, Status> testCaseToStatus;
  private Set<Description> foundTestCases;
  private Set<Description> finishedTestCases;
  private Set<Description> startedTestCases;
  // The {@link Description} for the parent TestRunner/TestSuite. May contain many nested
  // {@link Description}s for other {@link Runner}s and individual test methods.
  private Description testRunDescription = Description.EMPTY;
  private final AtomicReference<Description> currentTestCase =
      new AtomicReference<>(Description.EMPTY);
  private TestRunInfo memoizedTestRun;
  private final AtomicBoolean processCrashed = new AtomicBoolean(false);
  /*
   * ongoingResult and ongoingResultListener enable us to generate a final test run result in the
   * event of an application crash.  This is a bit messy, but Result's internal state is completely
   * dependent on its listener.  If this API changes, we can just subclass Result directly and
   * populate it throughout this RunListener.
   */
  private final AtomicReference<Result> ongoingResult = new AtomicReference<>(new Result());
  private final AtomicReference<RunListener> ongoingResultListener =
      new AtomicReference<>(ongoingResult.get().createListener());

  /**
   * Creates the {@link TestPlatformListener} to communicate with the remote test platform event
   * service.
   *
   * @param notificationService the remote service to send test run events to
   */
  public TestPlatformListener(@NonNull TestPlatformEventService notificationService) {
    super();
    // Instantiates everything on creation so that we can correctly report errors before
    // {@link #testRunStarted} is called.
    initListener();
    this.notificationService =
        checkNotNull(notificationService, "notificationService cannot be null");
  }

  /** {@inheritDoc} */
  @Override
  public void testRunStarted(Description description) throws Exception {
    initListener();
    ongoingResultListener.get().testRunStarted(description);
    setRunDescription(description);
    List<Description> testCases =
        JUnitDescriptionParser.getAllTestCaseDescriptions(testRunDescription);
    for (Description testCase : testCases) {
      foundTestCases.add(testCase);
      // Tests are considered passed if nothing changes their status.
      testCaseToStatus.put(testCase, Status.PASSED);
    }
    try {
      memoizedTestRun = convertToTestRun(testRunDescription);
      notificationService.send(new TestRunStartedEvent(memoizedTestRun, TimeStamp.now()));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunStartedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testRunFinished(Result result) throws Exception {
    ongoingResultListener.get().testRunFinished(result);
    Status status = result.wasSuccessful() ? Status.PASSED : Status.FAILED;
    // If the process crashed at any point, this is failed.
    status = processCrashed.get() ? Status.FAILED : status;
    // Mark all test cases that haven't run as CANCELLED or ABORTED (if started)
    if (foundTestCases.size() > finishedTestCases.size()) {
      // This was aborted mid test run. Mark it if this isn't already failing for some other
      // reason.
      status = status.equals(Status.PASSED) ? Status.ABORTED : status;
      for (Description test :
          JUnitDescriptionParser.getAllTestCaseDescriptions(testRunDescription)) {
        if (!finishedTestCases.contains(test)) {
          if (startedTestCases.contains(test)) {
            // The test isn't completed, but it was started and not finished.
            testCaseToStatus.put(test, Status.ABORTED);
          } else {
            // The test was supposed to be run but was never finished
            testCaseToStatus.put(test, Status.CANCELLED);
          }
          testFinishedInternal(test, TimeStamp.now());
        }
      }
    }
    try {
      notificationService.send(
          new TestRunFinishedEvent(memoizedTestRun, new TestStatus(status), TimeStamp.now()));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunFinishedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testStarted(Description description) throws Exception {
    if (isInitError(description)) {
      return; // This isn't a real test method, don't send an update to the service
    }
    ongoingResultListener.get().testStarted(description);
    startedTestCases.add(description);
    currentTestCase.set(description); // Caches the test description in case of a crash
    try {
      notificationService.send(
          new TestCaseStartedEvent(convertToTestCase(description), TimeStamp.now()));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestStartedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testFinished(Description description) throws Exception {
    testFinishedInternal(description, TimeStamp.now());
  }

  // If the test is marked as finished during the test run finish, we use the same timestamp
  private void testFinishedInternal(Description description, TimeStamp timeStamp) throws Exception {
    if (isInitError(description)) {
      return; // This isn't a real test method, don't send an update to the service
    }
    ongoingResultListener.get().testFinished(description);
    finishedTestCases.add(description);
    try {
      notificationService.send(
          new TestCaseFinishedEvent(
              convertToTestCase(description),
              new TestStatus(testCaseToStatus.get(description)),
              timeStamp));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestFinishedEvent to Test Platform", e);
    } finally {
      // reset test case
      currentTestCase.set(Description.EMPTY);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testFailure(Failure failure) throws Exception {
    Description description = failure.getDescription();
    ongoingResultListener.get().testFailure(failure);
    if (description.isTest() && !isInitError(description)) {
      testCaseToStatus.put(description, Status.FAILED);
    }
    try {
      TestPlatformEvent event = createErrorEvent(failure, TimeStamp.now());
      notificationService.send(event);
    } catch (TestEventException e) {
      throw new IllegalStateException("Unable to send error event", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testAssumptionFailure(Failure failure) {
    ongoingResultListener.get().testAssumptionFailure(failure);
    if (failure.getDescription().isTest()) {
      testCaseToStatus.put(failure.getDescription(), Status.SKIPPED);
    }
    try {
      TestPlatformEvent event = createErrorEvent(failure, TimeStamp.now());
      notificationService.send(event);
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestAssumptionFailureEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testIgnored(Description description) throws Exception {
    ongoingResultListener.get().testIgnored(description);
    Log.i(
        TAG,
        "TestIgnoredEvent("
            + description.getDisplayName()
            + "): "
            + description.getClassName()
            + "#"
            + description.getMethodName());
    testCaseToStatus.put(description, Status.IGNORED);
    testFinishedInternal(description, TimeStamp.now());
  }

  /**
   * Reports the process crash event with a given exception. It is assumed that AJUR is crashing and
   * not recovering from this. This will inform all clients that:
   *
   * <ol>
   *   <li>A test has encountered an error (or the run has encountered an error if no test is in
   *       progress)
   *   <li>The currently running test has finished (if it didn't already finished normally)
   *   <li>The test run has finished.
   * </ol>
   */
  public boolean reportProcessCrash(Throwable t) {
    processCrashed.set(true);
    boolean isTestCase = true;
    Description failingDescription = currentTestCase.get();
    if (failingDescription.equals(Description.EMPTY)) {
      isTestCase = false;
      failingDescription = testRunDescription;
    }
    try {
      Log.e("TestPlatformListener", "reporting crash as testfailure", t);
      testFailure(new Failure(failingDescription, t));
      if (isTestCase) {
        testFinished(failingDescription);
      }
      testRunFinished(ongoingResult.get());
    } catch (Exception e) {
      Log.e(TAG, "An exception was encountered while reporting the process crash", e);
      return false;
    }
    return true;
  }

  private void initListener() {
    finishedTestCases = new HashSet<>();
    foundTestCases = new HashSet<>();
    startedTestCases = new HashSet<>();
    testCaseToStatus = new HashMap<>();
    currentTestCase.set(Description.EMPTY);
    testRunDescription = Description.EMPTY;
    memoizedTestRun = null;
    processCrashed.set(false);
    ongoingResult.set(new Result());
    ongoingResultListener.set(ongoingResult.get().createListener());
  }

  private void setRunDescription(Description description) {
    testRunDescription = description;
    // Ignore around the "null" top-level Runner Description in AJUR or any unnecessarily nested
    // Runner structures.
    while (testRunDescription.getDisplayName().equals("null")
        && testRunDescription.getChildren().size() == 1) {
      testRunDescription = testRunDescription.getChildren().get(0);
    }
  }

  private static TestCaseInfo convertToTestCase(Description testCase) throws TestEventException {
    return ParcelableConverter.getTestCaseFromDescription(testCase);
  }

  private static TestRunInfo convertToTestRun(Description testRun) throws TestEventException {
    List<TestCaseInfo> testCases = new ArrayList<>();
    for (Description testCase : JUnitDescriptionParser.getAllTestCaseDescriptions(testRun)) {
      testCases.add(convertToTestCase(testCase));
    }
    return new TestRunInfo(testRun.getDisplayName(), testCases);
  }

  private static boolean isInitError(Description description) {
    return description.getMethodName() != null && description.getMethodName().equals(INIT_ERROR);
  }

  private TestPlatformEvent createErrorEvent(Failure failure, TimeStamp timeStamp)
      throws TestEventException {
    Description descriptionToUse = failure.getDescription();
    if (!descriptionToUse.isTest() || isInitError(descriptionToUse)) {
      descriptionToUse = testRunDescription;
    }
    ErrorInfo errorInfo = ErrorInfo.createFromFailure(failure);
    // If the description is a run description, report a run error. Otherwise report a test error.
    if (!descriptionToUse.equals(testRunDescription)) {
      try {
        return new TestCaseErrorEvent(convertToTestCase(descriptionToUse), errorInfo, timeStamp);
      } catch (TestEventException e) {
        Log.e(TAG, "Unable to create TestCaseErrorEvent", e);
      }
    }
    if (memoizedTestRun == null) {
      Log.d(TAG, "No test run info. Reporting an error before test run has ever started.");
      memoizedTestRun = convertToTestRun(Description.EMPTY);
    }
    return new TestRunErrorEvent(memoizedTestRun, errorInfo, timeStamp);
  }

}
