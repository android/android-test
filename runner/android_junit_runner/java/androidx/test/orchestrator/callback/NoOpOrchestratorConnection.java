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

package androidx.test.orchestrator.callback;

import android.content.Context;
import androidx.test.internal.events.client.TestDiscoveryEventService;
import androidx.test.internal.events.client.TestEventServiceConnection;
import androidx.test.internal.events.client.TestRunEventService;
import androidx.test.services.events.discovery.TestDiscoveryEvent;
import androidx.test.services.events.run.TestRunEvent;

/** A no-op connection to the Android test orchestrator. */
public class NoOpOrchestratorConnection
    implements TestEventServiceConnection, TestRunEventService, TestDiscoveryEventService {

  @Override
  public void send(TestDiscoveryEvent testDiscoveryEvent) {
    // Do nothing.
  }

  @Override
  public void send(TestRunEvent event) {
    // Do nothing.
  }

  @Override
  public void connect(Context context) {}
}
