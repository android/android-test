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

import androidx.annotation.NonNull;
import androidx.test.services.events.discovery.TestDiscoveryEvent;

/**
 * Base interface implemented by the Orchestrator test discovery client connection, e.g. {@link
 * TestDiscoveryEventServiceConnection} and {@code OrchestratorV1Connection}.
 */
public interface TestDiscoveryEventService {
  /**
   * Sends a {@link TestDiscoveryEvent} e.g. {@link
   * androidx.test.services.events.discovery.TestFoundEvent} that contains the {@link
   * androidx.test.services.events.TestCase} to the remote {@link
   * androidx.test.services.events.discovery.ITestDiscoveryEvent} service.
   *
   * <p>The remote service expects to receive {@link
   * androidx.test.services.events.discovery.TestDiscoveryStartedEvent}, followed by one or more
   * {@link androidx.test.services.events.discovery.TestFoundEvent}s, and finally the {@link
   * androidx.test.services.events.discovery.TestDiscoveryFinishedEvent} to indicate that test
   * discovery is complete.
   *
   * @param testDiscoveryEvent an object that extends {@link TestDiscoveryEvent} containing the test
   *     data
   * @throws TestEventClientException if the connection to the remote Orchestrator service fails
   */
  void send(@NonNull TestDiscoveryEvent testDiscoveryEvent) throws TestEventClientException;
}
