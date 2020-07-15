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

package androidx.test.services.events.run;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.test.services.events.TestCaseInfo;

/**
 * Denotes that the test ended with a TEST_STARTED. It has the {@link TestCaseInfo} object that this
 * event is associated with.
 */
public class TestStartedEvent extends TestRunEventWithTestCase {

  /**
   * Creates a {@link TestStartedEvent}.
   *
   * @param testCase the test case that this event is for
   */
  public TestStartedEvent(@NonNull TestCaseInfo testCase) {
    super(testCase);
  }

  TestStartedEvent(Parcel source) {
    super(source);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_STARTED;
  }
}
