/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.services.events.platform;

import androidx.test.services.events.platform.TestPlatformEvent;

/**
 * Defines an interface for remote {@link Instrumentation} service to send notifications / run
 * events to the Test Runner.
 */
interface ITestPlatformEvent {

  /**
   * Sends back notifications for the status of test execution.
   */
  void send(in TestPlatformEvent testPlatformEvent);
}
