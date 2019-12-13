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

package androidx.test.orchestrator.callback;

import static androidx.test.orchestrator.callback.BundleConverter.getBundleFromTestRunEvent;

import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.test.services.events.client.ConnectionBase;
import androidx.test.services.events.client.TestDiscoveryEventService;
import androidx.test.services.events.client.TestEventClientConnectListener;
import androidx.test.services.events.client.TestEventClientException;
import androidx.test.services.events.client.TestRunEventService;
import androidx.test.services.events.discovery.TestDiscoveryEvent;
import androidx.test.services.events.discovery.TestFoundEvent;
import androidx.test.services.events.run.TestRunEvent;

/**
 * Handles the communication with the remote {@code androidx.test.orchestrator.OrchestratorService}.
 * The Orchestrator v1 service supports both test discovery notifications and test run event
 * notifications.
 */
public final class LegacyOrchestratorConnection extends ConnectionBase<OrchestratorCallback>
    implements TestRunEventService, TestDiscoveryEventService {
  private static final String ORCHESTRATOR_SERVICE =
      "androidx.test.orchestrator/.OrchestratorService";

  public LegacyOrchestratorConnection(@NonNull TestEventClientConnectListener listener) {
    super(ORCHESTRATOR_SERVICE, OrchestratorCallback.Stub::asInterface, listener);
  }

  /** {@inheritDoc} */
  @Override
  public void send(@NonNull TestRunEvent event) throws TestEventClientException {
    if (null == service) {
      throw new TestEventClientException(
          "Unable to send notification, Orchestrator callback is null");
    }
    try {
      service.sendTestNotification(getBundleFromTestRunEvent(event));
    } catch (RemoteException e) {
      throw new TestEventClientException(
          "Unable to send test run event [" + event.getClass() + "]", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void send(@NonNull TestDiscoveryEvent event) throws TestEventClientException {
    if (null == service) {
      throw new TestEventClientException("Unable to add test, Orchestrator callback is null");
    }
    if (event instanceof TestFoundEvent) {
      String testName = ((TestFoundEvent) event).testCase.getClassAndMethodName();
      try {
        service.addTest(testName);
      } catch (RemoteException e) {
        throw new TestEventClientException("Failed to add test [" + testName + "]", e);
      }
    }
  }
}
