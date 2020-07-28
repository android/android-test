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

package androidx.test.services.events.discovery;

import androidx.test.services.events.discovery.TestDiscoveryEvent;

/**
 * Defines an interface for {@link Instrumentation} (e.g. {@code AndroidJUnitRunner} to
 * communicate with the remote Orchestrator test discovery service.
 */
interface ITestDiscoveryEvent {

  /**
   * Sends back notifications for the status of test discovery.
   */
  void send(in TestDiscoveryEvent testDiscoveryEvent);
}
