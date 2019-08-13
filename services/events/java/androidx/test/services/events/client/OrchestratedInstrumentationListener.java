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

/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static androidx.test.services.events.ParcelableConverter.getFailure;
import static androidx.test.services.events.ParcelableConverter.getFailuresFromList;
import static androidx.test.services.events.ParcelableConverter.getTestCaseFromDescription;
import static java.util.Collections.emptyList;

import android.os.ConditionVariable;
import androidx.annotation.NonNull;
import android.util.Log;
import androidx.test.services.events.TestCase;
import androidx.test.services.events.TestEventException;
import androidx.test.services.events.run.TestAssumptionFailureEvent;
import androidx.test.services.events.run.TestFailureEvent;
import androidx.test.services.events.run.TestFinishedEvent;
import androidx.test.services.events.run.TestIgnoredEvent;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import androidx.test.services.events.run.TestStartedEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A {@link RunListener} for the orchestrated instrumentation to communicate test run notifications
 * back to the {@link androidx.test.orchestrator.OrchestratorService}.
 */
public final class OrchestratedInstrumentationListener extends RunListener {
  private static final String TAG = "OrchestrationListener";
  private final TestRunEventService notificationService;
  private final ConditionVariable testFinishedCondition = new ConditionVariable();
  private final AtomicBoolean isTestFailed = new AtomicBoolean(false);
  private Description description = Description.EMPTY; // Cached test description

  public OrchestratedInstrumentationListener(@NonNull TestRunEventService notificationService) {
    super();
    this.notificationService = notificationService;
  }

  /** TestEvent.TEST_RUN_STARTED */
  @Override
  public void testRunStarted(Description description) {
    try {
      notificationService.send(new TestRunStartedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunStartedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_RUN_FINISHED */
  @Override
  public void testRunFinished(Result result) {
    List<androidx.test.services.events.Failure> failures;
    try {
      failures = getFailuresFromList(result.getFailures());
    } catch (TestEventException e) {
      Log.w(TAG, "Failure event doesn't contain a test case");
      failures = emptyList();
    }
    try {
      notificationService.send(
          new TestRunFinishedEvent(
              result.getRunCount(), result.getIgnoreCount(), result.getRunTime(), failures));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestRunFinishedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_STARTED */
  @Override
  public void testStarted(Description description) {
    this.description = description; // Caches the test description in case of a crash
    try {
      notificationService.send(new TestStartedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestStartedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_FINISHED */
  @Override
  public void testFinished(Description description) {
    try {
      notificationService.send(new TestFinishedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestFinishedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_FAILURE */
  @Override
  public void testFailure(Failure failure) {
    // This block can be called by the JUnit test framework when a failure happened in the test,
    // or {@link #reportProcessCrash(Throwable)} when we'd like to report a process crash as a
    // failure.
    // We'd like to make sure only one failure gets sent so that the isTestFailed variable is
    // checked and set without possibly racing between two thread calls.
    if (isTestFailed.compareAndSet(false, true)) {
      Log.d(TAG, "Sending TestFailure event [" + failure.getException().getMessage() + "]");
      TestFailureEvent event;
      try {
        event =
            new TestFailureEvent(
                getTestCaseFromDescription(failure.getDescription()), getFailure(failure));
      } catch (TestEventException e) {
        Log.e(TAG, "Unable to determine test case from failure [" + failure + "]", e);
        TestCase testCase;
        try {
          testCase = getTestCaseFromDescription(description);
        } catch (TestEventException ex) {
          Log.e(TAG, "Unable to determine test case from description [" + description + "]", ex);
          return;
        }
        event =
            new TestFailureEvent(
                testCase,
                new androidx.test.services.events.Failure(
                    failure.getMessage(), failure.getTestHeader(), failure.getTrace(), testCase));
      }
      try {
        notificationService.send(event);
      } catch (TestEventException e) {
        Log.e(TAG, "Unable to send TestFailureEvent, terminating", e);
      }
    }
  }

  /** TestEvent.TEST_ASSUMPTION_FAILURE */
  @Override
  public void testAssumptionFailure(Failure failure) {
    try {
      notificationService.send(
          new TestAssumptionFailureEvent(
              getTestCaseFromDescription(failure.getDescription()), getFailure(failure)));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestAssumptionFailureEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_IGNORED */
  @Override
  public void testIgnored(Description description) {
    try {
      notificationService.send(new TestIgnoredEvent(getTestCaseFromDescription(description)));
    } catch (TestEventException e) {
      Log.e(TAG, "Unable to send TestIgnoredEvent to Orchestrator", e);
    }
  }

  /** Reports the process crash event with a given exception. */
  public void reportProcessCrash(Throwable t, long timeoutMillis) {
    // Waits until the orchestrator gets a chance to handle the test failure (if any) before
    // bringing down the entire Instrumentation process.
    //
    // It's also possible that the process crashes in the middle of a test, so no TestFinish event
    // will be received. In this case, it will wait until timeoutMillis is reached.
    waitUntilTestFinished(timeoutMillis);

    // Need to report the process crashed event to the orchestrator.
    // This is to handle the case when the test body finishes but process crashes during
    // Instrumentation cleanup (e.g. stopping the app). Otherwise, the test will be marked as
    // passed.
    if (!isTestFailed.get()) {
      Log.i(TAG, "No test failure has been reported. Report the process crash.");
      reportProcessCrash(t);
    }
  }

  /**
   * Blocks until the test running within this Instrumentation has finished, whether the test
   * succeeds or fails.
   *
   * <p>We consider a test finished when the {@link #testFinished(Description)} method has been
   * called.
   */
  private void waitUntilTestFinished(long timeoutMillis) {
    if (!testFinishedCondition.block(timeoutMillis)) {
      Log.w(TAG, "Timeout waiting for the test to finish");
    }
  }

  /** Reports the process crash event with a given exception. */
  private void reportProcessCrash(Throwable t) {
    testFailure(new Failure(description, t));
    testFinished(description);
  }
}
