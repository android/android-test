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

import androidx.annotation.NonNull;
import android.util.Log;
import androidx.test.services.events.run.TestAssumptionFailureEvent;
import androidx.test.services.events.run.TestFailureEvent;
import androidx.test.services.events.run.TestFinishedEvent;
import androidx.test.services.events.run.TestIgnoredEvent;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import androidx.test.services.events.run.TestStartedEvent;
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

  public OrchestratedInstrumentationListener(@NonNull TestRunEventService notificationService) {
    super();
    this.notificationService = notificationService;
  }

  /** TestEvent.TEST_RUN_STARTED */
  @Override
  public void testRunStarted(Description description) {
    try {
      notificationService.send(new TestRunStartedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestRunStartedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_RUN_FINISHED */
  @Override
  public void testRunFinished(Result result) {
    try {
      notificationService.send(
          new TestRunFinishedEvent(
              result.getRunCount(),
              result.getIgnoreCount(),
              result.getRunTime(),
              getFailuresFromList(result.getFailures())));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestRunFinishedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_STARTED */
  @Override
  public void testStarted(Description description) {
    try {
      notificationService.send(new TestStartedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestStartedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_FINISHED */
  @Override
  public void testFinished(Description description) {
    try {
      notificationService.send(new TestFinishedEvent(getTestCaseFromDescription(description)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestFinishedEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_FAILURE */
  @Override
  public void testFailure(Failure failure) {
    try {
      notificationService.send(
          new TestFailureEvent(
              getTestCaseFromDescription(failure.getDescription()), getFailure(failure)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestFailureEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_ASSUMPTION_FAILURE */
  @Override
  public void testAssumptionFailure(Failure failure) {
    try {
      notificationService.send(
          new TestAssumptionFailureEvent(
              getTestCaseFromDescription(failure.getDescription()), getFailure(failure)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestAssumptionFailureEvent to Orchestrator", e);
    }
  }

  /** TestEvent.TEST_IGNORED */
  @Override
  public void testIgnored(Description description) {
    try {
      notificationService.send(new TestIgnoredEvent(getTestCaseFromDescription(description)));
    } catch (TestEventClientException e) {
      Log.e(TAG, "Unable to send TestIgnoredEvent to Orchestrator", e);
    }
  }
}
