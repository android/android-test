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

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.services.events.ParcelableConverter.getTestCaseFromDescription;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.test.services.events.ErrorInfo;
import androidx.test.services.events.TestEventException;
import androidx.test.services.events.TimeStamp;
import androidx.test.services.events.discovery.TestDiscoveryErrorEvent;
import androidx.test.services.events.discovery.TestDiscoveryFinishedEvent;
import androidx.test.services.events.discovery.TestDiscoveryStartedEvent;
import androidx.test.services.events.discovery.TestFoundEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Uses the {@link TestDiscoveryEventService} to pass test case information back to the
 * Orchestrator.
 */
public final class TestDiscoveryListener extends RunListener {
  private static final String TAG = "TestDiscoveryListener";
  private final TestDiscoveryEventService testDiscoveryEventService;
  private final AtomicBoolean discoveryStarted = new AtomicBoolean(false);

  public TestDiscoveryListener(@NonNull TestDiscoveryEventService testDiscoveryEventService) {
    this.testDiscoveryEventService =
        checkNotNull(testDiscoveryEventService, "testDiscoveryEventService can't be null");
  }

  @Override
  public void testRunStarted(Description description) {
    try {
      reportTestRunStarted();
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to send discovery started", e);
    }
  }

  private void reportTestRunStarted() throws TestEventClientException {
    // testRunStarted may be called more than once, if a process crash event arrives from another
    // thread
    if (!discoveryStarted.getAndSet(true)) {
      testDiscoveryEventService.send(new TestDiscoveryStartedEvent());
    }
  }

  @Override
  public void testRunFinished(Result result) {
    try {
      testDiscoveryEventService.send(new TestDiscoveryFinishedEvent());
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to send discovery started", e);
    }
  }

  @Override
  public void testFinished(Description description) {
    if (!JUnitValidator.validateDescription(description)) {
      // will be already reported via testFailure
      Log.d(
          TAG,
          "JUnit reported "
              + description.getClassName()
              + "#"
              + description.getMethodName()
              + "; discarding as bogus.");
      return;
    }
    try {
      testDiscoveryEventService.send(new TestFoundEvent(getTestCaseFromDescription(description)));
    } catch (TestEventException e) {
      Log.e(TAG, "Failed to get test description", e);
    }
  }

  @Override
  public void testFailure(Failure failure) {
    // this is likely a JUnit error loading the class aka a initializationError
    try {
      reportDiscoveryError(failure);
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to send discovery failure", e);
    }
  }

  private void reportDiscoveryError(Failure failure) throws TestEventClientException {
    testDiscoveryEventService.send(
        new TestDiscoveryErrorEvent(ErrorInfo.createFromFailure(failure), TimeStamp.now()));
  }

  public boolean reportProcessCrash(Throwable t) {
    try {
      // report a start event just in case discovery did not start yet
      reportTestRunStarted();
      reportDiscoveryError(new Failure(Description.EMPTY, t));
      // report run finished, since process crashed, we are likely dying
      testDiscoveryEventService.send(new TestDiscoveryFinishedEvent());
      return true;
    } catch (TestEventClientException e) {
      Log.e(TAG, "Failed to report process crash error", e);
      return false;
    }
  }
}
