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

package androidx.test.orchestrator.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import org.junit.runner.Description;

/**
 * Handles the communication with the remote {@code androidx.test.orchestrator.OrchestratorService}.
 * The Orchestrator v1 service supports both test discovery notifications and test run event
 * notifications.
 */
public class LegacyOrchestratorConnection
    implements OrchestratorConnection, TestNotificationService, TestDiscoveryService {
  private static final String TAG = "OrchestratorConnection";
  private static final String ORCHESTRATOR_PACKAGE = "androidx.test.orchestrator";
  private static final String ODO_SERVICE_PACKAGE =
      "androidx.test.orchestrator.OrchestratorService";

  private final OnConnectListener listener;

  public LegacyOrchestratorConnection(OnConnectListener listener) {
    this.listener = listener;
  }

  @VisibleForTesting OrchestratorCallback odoCallback = null;

  private final ServiceConnection connection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
          odoCallback = OrchestratorCallback.Stub.asInterface(service);
          Log.i(TAG, "Connected to OrchestratorService v1");
          listener.onOrchestratorConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
          odoCallback = null;
          Log.i(TAG, "Disconnected from OrchestratorService v1");
        }
      };

  @Override
  public void connect(Context context) {
    Intent intent = new Intent(ODO_SERVICE_PACKAGE);
    intent.setPackage(ORCHESTRATOR_PACKAGE);
    if (!context.bindService(intent, connection, Service.BIND_AUTO_CREATE)) {
      throw new RuntimeException("Cannot connect to " + ODO_SERVICE_PACKAGE);
    }
  }

  @Override
  public void sendTestNotification(Bundle bundle) throws RemoteException {
    if (null == odoCallback) {
      throw new IllegalStateException("Unable to send notification, Orchestrator callback is null");
    }
    odoCallback.sendTestNotification(bundle);
  }

  @Override
  public void addTest(Description description) {
    addTest(description.getClassName() + "#" + description.getMethodName());
  }

  private void addTest(String test) {
    if (null == odoCallback) {
      throw new IllegalStateException("Unable to send test, Orchestrator callback is null");
    }
    try {
      odoCallback.addTest(test);
    } catch (RemoteException e) {
      Log.e(TAG, "Unable to send test [" + test + "]", e);
    }
  }
}
