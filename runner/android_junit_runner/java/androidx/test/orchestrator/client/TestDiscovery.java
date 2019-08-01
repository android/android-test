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

import android.os.RemoteException;
import org.junit.runner.Description;

/**
 * Uses the {@link androidx.test.orchestrator.client.TestDiscoveryService} to pass test case
 * information back to the Orchestrator.
 */
public class TestDiscovery {
  private final TestDiscoveryService testDiscoveryService;

  public TestDiscovery(TestDiscoveryService testDiscoveryService) {
    this.testDiscoveryService = testDiscoveryService;
  }

  /**
   * Recursively sends test information to the remote service.
   *
   * @param description the root JUnit test case description object to traverse
   */
  public void addTests(Description description) {
    if (description.isEmpty()) {
      return;
    }
    if (description.isTest()) {
      try {
        testDiscoveryService.addTest(description);
      } catch (RemoteException e) {
        throw new IllegalStateException("Unable to send test description to Orchestrator", e);
      }
    } else {
      for (Description child : description.getChildren()) {
        addTests(child);
      }
    }
  }
}
