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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.test.internal.events.client.TestEventClientException;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager.TestEvent;
import androidx.test.services.events.FailureInfo;
import androidx.test.services.events.run.TestAssumptionFailureEvent;
import androidx.test.services.events.run.TestFailureEvent;
import androidx.test.services.events.run.TestFinishedEvent;
import androidx.test.services.events.run.TestIgnoredEvent;
import androidx.test.services.events.run.TestRunEvent;
import androidx.test.services.events.run.TestRunEventWithTestCase;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import androidx.test.services.events.run.TestStartedEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts {@link TestRunEvent}-related event messages received from {@link
 * androidx.test.internal.events.client.OrchestratedInstrumentationListener} to a {@link Bundle}
 * suitable for sending to the legacy Orchestrator v1 service with {@link
 * androidx.test.orchestrator.OrchestratorCallback#sendTestNotification(Bundle)}.
 */
class BundleConverter {
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_FAILURE = "failure";
  private static final String KEY_RESULT = "result";

  private BundleConverter() {}

  /**
   * Converts a {@link TestRunEvent} to a {@link Bundle}.
   *
   * @param event any message extending {@link TestRunEvent}
   * @return a {@link Bundle} containing the "TestEvent" key and "description", "failure" or
   *     "result" parcelables
   * @throws TestEventClientException if the {@code event} is not a known event type
   */
  @NonNull
  public static Bundle getBundleFromTestRunEvent(@NonNull TestRunEvent event)
      throws TestEventClientException {
    if (event instanceof TestAssumptionFailureEvent) {
      return getBundleFromFailureEvent(
          (TestAssumptionFailureEvent) event, TestEvent.TEST_ASSUMPTION_FAILURE);
    } else if (event instanceof TestFailureEvent) {
      return getBundleFromFailureEvent((TestFailureEvent) event, TestEvent.TEST_FAILURE);
    } else if (event instanceof TestFinishedEvent) {
      return getBundleFromTestCaseEvent((TestFinishedEvent) event, TestEvent.TEST_FINISHED);
    } else if (event instanceof TestIgnoredEvent) {
      return getBundleFromTestCaseEvent((TestIgnoredEvent) event, TestEvent.TEST_IGNORED);
    } else if (event instanceof TestRunFinishedEvent) {
      return getBundleFromTestRunFinishedEvent((TestRunFinishedEvent) event);
    } else if (event instanceof TestRunStartedEvent) {
      return getBundleFromTestCaseEvent((TestRunStartedEvent) event, TestEvent.TEST_RUN_STARTED);
    } else if (event instanceof TestStartedEvent) {
      return getBundleFromTestCaseEvent((TestStartedEvent) event, TestEvent.TEST_STARTED);
    } else {
      throw new TestEventClientException("Unrecognized test run event type [" + event + "]");
    }
  }

  @NonNull
  private static Bundle getBundleFromFailureEvent(
      @NonNull TestFailureEvent event, @NonNull TestEvent testFailureEventType) {
    Bundle bundle = new Bundle();
    ParcelableDescription description =
        new ParcelableDescription(event.testCase.getClassAndMethodName());
    ParcelableFailure failure = new ParcelableFailure(description, event.failure.stackTrace);
    bundle.putParcelable(KEY_FAILURE, failure);
    bundle.putString(KEY_TEST_EVENT, testFailureEventType.name());
    return bundle;
  }

  @NonNull
  private static Bundle getBundleFromTestCaseEvent(
      @NonNull TestRunEventWithTestCase event, @NonNull TestEvent testEventType) {
    Bundle bundle = new Bundle();
    ParcelableDescription description =
        new ParcelableDescription(event.testCase.getClassAndMethodName());
    bundle.putParcelable(KEY_DESCRIPTION, description);
    bundle.putString(KEY_TEST_EVENT, testEventType.name());
    return bundle;
  }

  @NonNull
  private static Bundle getBundleFromTestRunFinishedEvent(@NonNull TestRunFinishedEvent event) {
    Bundle bundle = new Bundle();
    ParcelableResult result = new ParcelableResult(getParcelableFailureFromList(event.failures));
    bundle.putParcelable(KEY_RESULT, result);
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_RUN_FINISHED.name());
    return bundle;
  }

  @NonNull
  private static List<ParcelableFailure> getParcelableFailureFromList(
      @NonNull List<FailureInfo> failures) {
    List<ParcelableFailure> result = new ArrayList<>();
    for (FailureInfo failure : failures) {
      ParcelableDescription description =
          new ParcelableDescription(failure.testCase.getClassAndMethodName());
      ParcelableFailure parcelable = new ParcelableFailure(description, failure.stackTrace);
      result.add(parcelable);
    }
    return result;
  }
}
