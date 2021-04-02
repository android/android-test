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
import androidx.test.services.events.platform.ITestPlatformEvent;
import androidx.test.services.events.platform.TestPlatformEvent;

/** Handles the connection to the remote {@link ITestPlatformEvent} service. */
public class TestPlatformEventServiceConnection
    extends TestEventServiceConnectionBase<ITestPlatformEvent> implements TestPlatformEventService {

  TestPlatformEventServiceConnection(
      @NonNull String serviceName, @NonNull TestEventClientConnectListener listener) {
    super(serviceName, ITestPlatformEvent.Stub::asInterface, listener);
  }

  /** {@inheritDoc} */
  @Override
  public void send(@NonNull TestPlatformEvent testPlatformEvent) throws TestEventClientException {
    checkNotNull(testPlatformEvent, "testPlatformEvent cannot be null");
    if (service == null) {
      throw new TestEventClientException("Can't send test platform event, service not connected");
    }
    try {
      service.send(testPlatformEvent);
    } catch (RemoteException e) {
      throw new TestEventClientException("Failed to send test platform event", e);
    }
  }
}
