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

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.orchestrator.callback.BundleConverter.getBundleFromTestRunEvent;

import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.test.internal.events.client.TestDiscoveryEventService;
import androidx.test.internal.events.client.TestEventClientConnectListener;
import androidx.test.internal.events.client.TestEventClientException;
import androidx.test.internal.events.client.TestEventServiceConnectionBase;
import androidx.test.internal.events.client.TestRunEventService;
import androidx.test.services.events.discovery.TestDiscoveryEvent;
import androidx.test.services.events.discovery.TestFoundEvent;
import androidx.test.services.events.run.TestRunEvent;

/**
 * Handles the communication with the remote {@code androidx.test.orchestrator.OrchestratorService}.
 * The Orchestrator v1 service supports both test discovery notifications and test run event
 * notifications.
 */
public final class OrchestratorV1Connection
    extends TestEventServiceConnectionBase<OrchestratorCallback>
    implements TestRunEventService, TestDiscoveryEventService {
  // TODO(b/161828929): move this to internal/events/client
  private static final String ORCHESTRATOR_SERVICE =
      "androidx.test.orchestrator/.OrchestratorService";

  /**
   * Creates a new {@code OrchestratorV1Connection} to handle the communication with the remote
   * {@code androidx.test.orchestrator.OrchestratorService}.
   */
  public OrchestratorV1Connection(@NonNull TestEventClientConnectListener listener) {
    super(ORCHESTRATOR_SERVICE, OrchestratorCallback.Stub::asInterface, listener);
  }

  /** {@inheritDoc} */
  @Override
  public void send(@NonNull TestRunEvent event) throws TestEventClientException {
    checkNotNull(event, "event cannot be null");
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
    checkNotNull(event, "event cannot be null");
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
