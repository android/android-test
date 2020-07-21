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

import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.test.services.events.run.ITestRunEvent;
import androidx.test.services.events.run.TestRunEvent;

/** Handles the connection to the remote {@link ITestRunEvent} service. */
public class TestRunEventServiceConnection extends TestEventServiceConnectionBase<ITestRunEvent>
    implements TestRunEventService {

  TestRunEventServiceConnection(
      @NonNull String serviceName, @NonNull TestEventClientConnectListener listener) {
    super(serviceName, ITestRunEvent.Stub::asInterface, listener);
  }

  /** {@inheritDoc} */
  @Override
  public void send(@NonNull TestRunEvent testRunEvent) throws TestEventClientException {
    checkNotNull(testRunEvent, "testRunEvent cannot be null");
    if (service == null) {
      throw new TestEventClientException("Can't send test run event, service not connected");
    }
    try {
      service.send(testRunEvent);
    } catch (RemoteException e) {
      throw new TestEventClientException("Failed to send test run event", e);
    }
  }
}
