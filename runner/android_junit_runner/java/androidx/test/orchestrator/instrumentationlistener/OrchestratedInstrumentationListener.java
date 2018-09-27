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

package androidx.test.orchestrator.instrumentationlistener;

import static androidx.test.orchestrator.listeners.OrchestrationListenerManager.KEY_TEST_EVENT;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager.TestEvent;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A {@link RunListener} for the orchestrated instrumentation to communicate information back to the
 * {@link androidx.test.orchestrator.OrchestratorService}. Not to be attached to {@link
 * androidx.test.orchestrator.AndroidTestOrchestrator} itself.
 */
public final class OrchestratedInstrumentationListener extends RunListener {

  private static final String TAG = "OrchestrationListener";

  private static final String ORCHESTRATOR_PACKAGE = "androidx.test.orchestrator";

  private static final String ODO_SERVICE_PACKAGE =
      "androidx.test.orchestrator.OrchestratorService";

  private final OnConnectListener listener;

  OrchestratorCallback odoCallback;

  /** Interface to notify when the listener has connected to the orchestrator. */
  public interface OnConnectListener {
    void onOrchestratorConnect();
  }

  public OrchestratedInstrumentationListener(OnConnectListener listener) {
    super();
    this.listener = listener;
  }

  private final ServiceConnection connection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
          odoCallback = OrchestratorCallback.Stub.asInterface(service);
          Log.i(TAG, "OrchestrationListener connected to service");
          listener.onOrchestratorConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
          odoCallback = null;
          Log.i(TAG, "OrchestrationListener disconnected from service");
        }
      };

  public void connect(Context context) {
    Intent intent = new Intent(ODO_SERVICE_PACKAGE);
    intent.setPackage(ORCHESTRATOR_PACKAGE);

    if (!context.bindService(intent, connection, Service.BIND_AUTO_CREATE)) {
      throw new RuntimeException("Cannot connect to " + ODO_SERVICE_PACKAGE);
    }
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

  public void sendTestNotification(TestEvent type, Bundle bundle) throws RemoteException {
    if (null == odoCallback) {
      throw new IllegalStateException("Unable to send notification, callback is null");
    }
    bundle.putString(KEY_TEST_EVENT, type.toString());

    odoCallback.sendTestNotification(bundle);
  }

  public void addTests(Description description) {
    if (description.isEmpty()) {
      return;
    }

    if (description.isTest()) {
      addTest(description.getClassName() + "#" + description.getMethodName());
    } else {
      for (Description child : description.getChildren()) {
        addTests(child);
      }
    }
  }

  public void addTest(String test) {
    if (null == odoCallback) {
      throw new IllegalStateException("Unable to send test, callback is null");
    }

    try {
      odoCallback.addTest(test);
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send test", e);
    }
  }
}
