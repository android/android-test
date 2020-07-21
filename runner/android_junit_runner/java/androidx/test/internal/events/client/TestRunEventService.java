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
import androidx.test.services.events.run.TestRunEvent;

/**
 * Base interface implemented by the test notification client connection, e.g. {@link
 * TestRunEventServiceConnection} and {@code OrchestratorV1Connection}.
 */
public interface TestRunEventService {
  /**
   * Sends a test status event and its related {@link androidx.test.services.events.TestCase} and
   * {@link androidx.test.services.events.Failure} parcelables.
   *
   * @param event an object that extends {@link androidx.test.services.events.run.TestRunEvent} to
   *     indicate the test run progress and status
   * @throws TestEventClientException throws an exception if the connection to the Orchestrator
   *     fails
   */
  void send(@NonNull TestRunEvent event) throws TestEventClientException;
}
