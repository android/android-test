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

package androidx.test.orchestrator.client;

import static androidx.test.orchestrator.listeners.OrchestrationListenerManager.KEY_TEST_EVENT;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager.TestEvent;
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
  private final TestNotificationService notificationService;

  public OrchestratedInstrumentationListener(TestNotificationService notificationService) {
    super();
    this.notificationService = notificationService;
  }

  @Override
  public void testRunStarted(Description description) {
    try {
      sendTestNotification(
          TestEvent.TEST_RUN_STARTED, BundleJUnitUtils.getBundleFromDescription(description));
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send TestRunStarted Status to Orchestrator", e);
    }
  }

  @Override
  public void testRunFinished(Result result) {
    try {
      sendTestNotification(
          TestEvent.TEST_RUN_FINISHED, BundleJUnitUtils.getBundleFromResult(result));
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send TestRunFinished Status to Orchestrator", e);
    }
  }

  @Override
  public void testStarted(Description description) {
    try {
      sendTestNotification(
          TestEvent.TEST_STARTED, BundleJUnitUtils.getBundleFromDescription(description));
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send TestStarted Status to Orchestrator", e);
    }
  }

  @Override
  public void testFinished(Description description) {
    try {
      sendTestNotification(
          TestEvent.TEST_FINISHED, BundleJUnitUtils.getBundleFromDescription(description));
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send TestFinished Status to Orchestrator", e);
    }
  }

  @Override
  public void testFailure(Failure failure) {
    try {
      sendTestNotification(TestEvent.TEST_FAILURE, BundleJUnitUtils.getBundleFromFailure(failure));
    } catch (RemoteException e) {
      throw new IllegalStateException("Unable to send TestFailure status, terminating", e);
    }
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    try {
      sendTestNotification(
          TestEvent.TEST_ASSUMPTION_FAILURE, BundleJUnitUtils.getBundleFromFailure(failure));
    } catch (RemoteException e) {
      throw new IllegalStateException(
          "Unable to send TestAssumptionFailure status, terminating", e);
    }
  }

  @Override
  public void testIgnored(Description description) {
    try {
      sendTestNotification(
          TestEvent.TEST_IGNORED, BundleJUnitUtils.getBundleFromDescription(description));
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send TestIgnored Status to Orchestrator", e);
    }
  }

  private void sendTestNotification(TestEvent type, Bundle bundle) throws RemoteException {
    bundle.putString(KEY_TEST_EVENT, type.toString());
    notificationService.sendTestNotification(bundle);
  }
}
