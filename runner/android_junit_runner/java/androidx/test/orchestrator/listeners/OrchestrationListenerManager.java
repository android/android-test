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
package androidx.test.orchestrator.listeners;

import static androidx.test.orchestrator.junit.BundleJUnitUtils.getDescription;
import static androidx.test.orchestrator.junit.BundleJUnitUtils.getFailure;
import static androidx.test.orchestrator.junit.BundleJUnitUtils.getResult;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import java.util.ArrayList;
import java.util.List;

/** Container class for all orchestration listeners */
public final class OrchestrationListenerManager {

  private static final String TAG = "ListenerManager";

  /** Message types sent from the remote instrumentation */
  public enum TestEvent {
    TEST_RUN_STARTED,
    TEST_RUN_FINISHED,
    TEST_STARTED,
    TEST_FINISHED,
    TEST_FAILURE,
    TEST_ASSUMPTION_FAILURE,
    TEST_IGNORED
  }

  public static final String KEY_TEST_EVENT = "TestEvent";

  private final List<OrchestrationRunListener> listeners = new ArrayList<>();
  private final Instrumentation instrumentation;

  private boolean markTerminationAsFailure = false;
  private ParcelableDescription lastDescription;

  public OrchestrationListenerManager(Instrumentation instrumentation) {
    if (null == instrumentation) {
      throw new IllegalArgumentException("Instrumentation must not be null");
    }

    this.instrumentation = instrumentation;
  }

  public void addListener(OrchestrationRunListener listener) {
    listener.setInstrumentation(instrumentation);
    listeners.add(listener);
  }

  /** To be called after test collection, before the first test begins. */
  public void orchestrationRunStarted(int testCount) {
    for (OrchestrationRunListener listener : listeners) {
      listener.orchestrationRunStarted(testCount);
    }
  }

  /** To be called when the test process begins */
  public void testProcessStarted(ParcelableDescription description) {
    lastDescription = description;
    markTerminationAsFailure = true;
  }

  /** To be called when the test process terminates, with the result from standard out. */
  public void testProcessFinished(String outputFile) {
    if (markTerminationAsFailure) {
      for (OrchestrationRunListener listener : listeners) {
        listener.testFailure(
            new ParcelableFailure(
                lastDescription,
                new Throwable(
                    "Test instrumentation process crashed. Check " + outputFile + " for details")));
        listener.testFinished(lastDescription);
      }
    }
  }

  /**
   * Takes a test message and parses it out for all the listeners.
   *
   * @param bundle A bundle containing a key describing the type of message, and a bundle with the
   *     appropriate parcelable imitation of aJ Unit object.
   */
  public void handleNotification(Bundle bundle) {
    bundle.setClassLoader(getClass().getClassLoader());
    cacheStatus(bundle);
    for (OrchestrationRunListener listener : listeners) {
      handleNotificationForListener(listener, bundle);
    }
  }

  private void cacheStatus(Bundle bundle) {
    if (getDescription(bundle) != null) {
      lastDescription = getDescription(bundle);
    }

    TestEvent status = TestEvent.valueOf(bundle.getString(KEY_TEST_EVENT));
    switch (status) {
      case TEST_RUN_STARTED:
        // Likely already set true in testProcessStarted(), but no reason to not set again.
        markTerminationAsFailure = true;
        break;
      case TEST_FAILURE:
        // After failure, no need to report further failures if process crashes
        markTerminationAsFailure = false;
        break;
      case TEST_RUN_FINISHED:
        // It's now ok to terminate safely.
        markTerminationAsFailure = false;
        break;
      default:
        // We only care about three cases.
    }
  }

  private void handleNotificationForListener(OrchestrationRunListener listener, Bundle bundle) {

    TestEvent status = TestEvent.valueOf(bundle.getString(KEY_TEST_EVENT));

    switch (status) {
      case TEST_RUN_STARTED:
        listener.testRunStarted(getDescription(bundle));
        break;

      case TEST_STARTED:
        listener.testStarted(getDescription(bundle));
        break;

      case TEST_FINISHED:
        listener.testFinished(getDescription(bundle));
        break;

      case TEST_FAILURE:
        listener.testFailure(getFailure(bundle));
        break;

      case TEST_ASSUMPTION_FAILURE:
        listener.testAssumptionFailure(getFailure(bundle));
        break;

      case TEST_IGNORED:
        listener.testIgnored(getDescription(bundle));
        break;

      case TEST_RUN_FINISHED:
        listener.testRunFinished(getResult(bundle));
        break;

      default:
        Log.e(TAG, "Unknown notification type");
    }
  }
}
