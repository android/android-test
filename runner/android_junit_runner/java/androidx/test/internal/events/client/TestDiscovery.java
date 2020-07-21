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
import static androidx.test.services.events.ParcelableConverter.getTestCaseFromDescription;

import androidx.annotation.NonNull;
import android.util.Log;
import androidx.test.services.events.TestEventException;
import androidx.test.services.events.discovery.TestDiscoveryFinishedEvent;
import androidx.test.services.events.discovery.TestDiscoveryStartedEvent;
import androidx.test.services.events.discovery.TestFoundEvent;
import org.junit.runner.Description;

/**
 * Uses the {@link TestDiscoveryEventService} to pass test case information back to the
 * Orchestrator.
 */
public final class TestDiscovery {
  private static final String TAG = "TestDiscovery";
  private final TestDiscoveryEventService testDiscoveryEventService;

  public TestDiscovery(@NonNull TestDiscoveryEventService testDiscoveryEventService) {
    this.testDiscoveryEventService =
        checkNotNull(testDiscoveryEventService, "testDiscoveryEventService can't be null");
  }

  /**
   * Recursively sends test information to the remote service.
   *
   * @param description the root JUnit test case description object to traverse
   */
  public void addTests(@NonNull Description description) throws TestEventClientException {
    checkNotNull(description, "description cannot be null");
    testDiscoveryEventService.send(new TestDiscoveryStartedEvent());
    addTest(description);
    testDiscoveryEventService.send(new TestDiscoveryFinishedEvent());
  }

  private void addTest(@NonNull Description description) {
    if (description.isEmpty()) {
      Log.d(TAG, "addTest called with an empty test description");
      return;
    }
    if (description.isTest()) {
      try {
        testDiscoveryEventService.send(new TestFoundEvent(getTestCaseFromDescription(description)));
      } catch (TestEventException e) {
        Log.e(TAG, "Failed to get test description", e);
      }
    } else {
      for (Description child : description.getChildren()) {
        addTest(child);
      }
    }
  }
}
