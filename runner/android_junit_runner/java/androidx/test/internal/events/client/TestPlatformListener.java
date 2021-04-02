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
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.annotation.NonNull;
import android.util.Log;
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

  /**
   * Creates the {@link TestPlatformListener} to communicate with the remote test platform event
   * service.
   *
   * @param notificationService the remote service to send test run events to
   */
  public TestPlatformListener(@NonNull TestPlatformEventService notificationService) {
    super();
    this.notificationService =
        checkNotNull(notificationService, "notificationService cannot be null");
  }

  /** {@inheritDoc} */
  @Override
  public void testRunStarted(Description description) {
    TimeStamp timeStamp = getTimeStamp();
    resetListener();
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
      notificationService.send(new TestRunStartedEvent(memoizedTestRun, timeStamp));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunStartedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testRunFinished(Result result) {
    TimeStamp timeStamp = getTimeStamp();
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
          testFinished(test);
        }
      }
    }
    try {
      notificationService.send(
          new TestRunFinishedEvent(memoizedTestRun, new TestStatus(status), timeStamp));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunFinishedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testStarted(Description description) {
    TimeStamp timeStamp = getTimeStamp();
    if (isInitError(description)) {
      return; // This isn't a real test method, don't send an update to the service
    }
    startedTestCases.add(description);
    currentTestCase.set(description); // Caches the test description in case of a crash
    try {
      notificationService.send(new TestCaseStartedEvent(convertToTestCase(description), timeStamp));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestStartedEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testFinished(Description description) {
    TimeStamp timeStamp = getTimeStamp();
    if (isInitError(description)) {
      return; // This isn't a real test method, don't send an update to the service
    }
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
  public void testFailure(Failure failure) {
    TimeStamp timeStamp = getTimeStamp();
    Description description = failure.getDescription();
    if (description.isTest() && !isInitError(description)) {
      testCaseToStatus.put(description, Status.FAILED);
    }
    try {
      TestPlatformEvent event = createErrorEvent(failure, timeStamp);
      notificationService.send(event);
    } catch (TestEventException e) {
      throw new IllegalStateException("Unable to send error event", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testAssumptionFailure(Failure failure) {
    TimeStamp timeStamp = getTimeStamp();
    if (failure.getDescription().isTest()) {
      testCaseToStatus.put(failure.getDescription(), Status.SKIPPED);
    }
    try {
      TestPlatformEvent event = createErrorEvent(failure, timeStamp);
      notificationService.send(event);
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestAssumptionFailureEvent to Test Platform", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void testIgnored(Description description) {
    Log.i(
        TAG,
        "TestIgnoredEvent("
            + description.getDisplayName()
            + "): "
            + description.getClassName()
            + "#"
            + description.getMethodName());
    testCaseToStatus.put(description, Status.IGNORED);
    testFinished(description);
  }

  /** Reports the process crash event with a given exception. */
  public void reportProcessCrash(Throwable t, long timeoutMillis) {
    processCrashed.set(true);
    Description failingDescription = currentTestCase.get();
    if (failingDescription.equals(Description.EMPTY)) {
      failingDescription = testRunDescription;
    }
    testFailure(new Failure(failingDescription, t));
  }

  private void resetListener() {
    finishedTestCases = new HashSet<>();
    foundTestCases = new HashSet<>();
    startedTestCases = new HashSet<>();
    testCaseToStatus = new HashMap<>();
    currentTestCase.set(Description.EMPTY);
    testRunDescription = Description.EMPTY;
    memoizedTestRun = null;
    processCrashed.set(false);
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

  private TestPlatformEvent createErrorEvent(Failure failure, TimeStamp timeStamp) {
    Description descriptionToUse = failure.getDescription();
    if (!descriptionToUse.isTest() && !isInitError(descriptionToUse)) {
      descriptionToUse = testRunDescription;
    }
    ErrorInfo errorInfo =
        new ErrorInfo(
            failure.getMessage(), failure.getException().getClass().getName(), failure.getTrace());
    // If the description is a run description, report a run error. Otherwise report a test error.
    if (!descriptionToUse.equals(testRunDescription)) {
      try {
        return new TestCaseErrorEvent(convertToTestCase(descriptionToUse), errorInfo, timeStamp);
      } catch (TestEventException e) {
        Log.e(TAG, "Unable to create TestCaseErrorEvent", e);
      }
    }
    return new TestRunErrorEvent(memoizedTestRun, errorInfo, timeStamp);
  }

  private TimeStamp getTimeStamp() {
    long epochNanos = System.nanoTime();
    long epochSeconds = NANOSECONDS.toSeconds(epochNanos);
    return new TimeStamp(epochSeconds, (int) (epochNanos - SECONDS.toNanos(epochSeconds)));
  }
}
