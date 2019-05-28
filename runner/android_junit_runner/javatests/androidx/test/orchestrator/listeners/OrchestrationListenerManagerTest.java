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
import static androidx.test.orchestrator.listeners.OrchestrationListenerManager.KEY_TEST_EVENT;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.os.Bundle;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager.TestEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link OrchestrationListenerManager}. */
@RunWith(RobolectricTestRunner.class)
public class OrchestrationListenerManagerTest {

  @Mock Instrumentation mockInstrumentation;
  @Mock OrchestrationRunListener mockRunListener1;
  @Mock OrchestrationRunListener mockRunListener2;

  public OrchestrationListenerManager listener;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    listener = new OrchestrationListenerManager(mockInstrumentation);
    listener.addListener(mockRunListener1);
    listener.addListener(mockRunListener2);
  }

  @Test
  public void construct_withNoInstrumentationThrowsException() {
    try {
      new OrchestrationListenerManager(null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void addListeners_haveInstrumentation() {
    verify(mockRunListener1).setInstrumentation(mockInstrumentation);
    verify(mockRunListener2).setInstrumentation(mockInstrumentation);
  }

  @Test
  public void orchestrationRunStarted_passedToListeners() {
    listener.orchestrationRunStarted(5);
    verify(mockRunListener1).orchestrationRunStarted(5);
    verify(mockRunListener2).orchestrationRunStarted(5);
  }

  @Test
  public void handleTestRunStarted() {
    Bundle bundle = makeTestRunStartedBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testRunStarted(getDescription(bundle));
    verify(mockRunListener2).testRunStarted(getDescription(bundle));
  }

  @Test
  public void handleTestStarted() {
    Bundle bundle = makeTestStartedBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testStarted(getDescription(bundle));
    verify(mockRunListener2).testStarted(getDescription(bundle));
  }

  @Test
  public void handleTestFinished() {
    Bundle bundle = makeTestFinishedBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testFinished(getDescription(bundle));
    verify(mockRunListener2).testFinished(getDescription(bundle));
  }

  @Test
  public void handleTestFailure() {
    Bundle bundle = makeTestFailureBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testFailure(getFailure(bundle));
    verify(mockRunListener2).testFailure(getFailure(bundle));
  }

  @Test
  public void handleTestAssumptionFailure() {
    Bundle bundle = makeTestAssumptionFailureBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testAssumptionFailure(getFailure(bundle));
    verify(mockRunListener2).testAssumptionFailure(getFailure(bundle));
  }

  @Test
  public void handleTestIgnored() {
    Bundle bundle = makeTestIgnoredBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testIgnored(getDescription(bundle));
    verify(mockRunListener2).testIgnored(getDescription(bundle));
  }

  @Test
  public void handleTestRunFinished() throws Exception {
    Bundle bundle = makeTestRunFinishedBundle();
    listener.handleNotification(bundle);
    verify(mockRunListener1).testRunFinished(getResult(bundle));
    verify(mockRunListener2).testRunFinished(getResult(bundle));
  }

  @Test
  public void handleTestProcessFinished_normally() throws Exception {
    listener.handleNotification(makeTestRunStartedBundle());
    listener.handleNotification(makeTestRunFinishedBundle());
    listener.testProcessFinished("outputFile");

    // Failure generated inside the manager, so we wildcard
    verify(mockRunListener1, times(0)).testFailure(any());
  }

  @Test
  public void handleTestProcessFinished_inProgress() {
    listener.handleNotification(makeTestRunStartedBundle());
    listener.testProcessFinished("outputFile");

    // Failure generated inside the manager, so we wildcard
    verify(mockRunListener1).testFailure(any());
    verify(mockRunListener2).testFailure(any());
  }

  // Non test convenience methods

  private static Bundle makeTestRunStartedBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromDescription(makeDescription());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_RUN_STARTED.toString());
    return bundle;
  }

  private static Bundle makeTestStartedBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromDescription(makeDescription());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_STARTED.toString());
    return bundle;
  }

  private static Bundle makeTestFinishedBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromDescription(makeDescription());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_FINISHED.toString());
    return bundle;
  }

  private static Bundle makeTestFailureBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromFailure(makeFailure());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_FAILURE.toString());
    return bundle;
  }

  private static Bundle makeTestAssumptionFailureBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromFailure(makeFailure());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_ASSUMPTION_FAILURE.toString());
    return bundle;
  }

  private static Bundle makeTestIgnoredBundle() {
    Bundle bundle = BundleJUnitUtils.getBundleFromDescription(makeDescription());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_IGNORED.toString());
    return bundle;
  }

  private static Bundle makeTestRunFinishedBundle() throws Exception {
    Bundle bundle = BundleJUnitUtils.getBundleFromResult(makeResult());
    bundle.putString(KEY_TEST_EVENT, TestEvent.TEST_RUN_FINISHED.toString());
    return bundle;
  }

  private static Description makeDescription() {
    return Description.createTestDescription(
        androidx.test.orchestrator.SampleJUnitTest.class, "sampleTest");
  }

  private static Failure makeFailure() {
    return new Failure(makeDescription(), new Throwable("Error message"));
  }

  private static Result makeResult() throws Exception {
    Result result = new Result();
    result.createListener().testRunStarted(makeDescription());
    return result;
  }
}
