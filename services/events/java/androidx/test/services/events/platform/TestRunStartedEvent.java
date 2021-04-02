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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.test.services.events.TestRunInfo;
import androidx.test.services.events.TimeStamp;

/**
 * This event should be sent once at the beginning of a test run to signal that more events will
 * follow. This should always be the first event that is sent - before any tests are actually run.
 *
 * @see TestRunFinishedEvent to report that a test run is finished.
 * @see TestCaseStartedEvent to report individual test cases have begun.
 */
public class TestRunStartedEvent extends TestPlatformEvent {
  /* The test run that started */
  public final TestRunInfo testRun;
  /* The time this test run started */
  public final TimeStamp timeStamp;

  /**
   * Creates a {@link TestRunStartedEvent}.
   *
   * @param testRun the test run that started.
   * @param timeStamp the time this test run started.
   */
  public TestRunStartedEvent(@NonNull TestRunInfo testRun, @NonNull TimeStamp timeStamp) {
    this.testRun = checkNotNull(testRun, "testRun cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestRunStartedEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestRunStartedEvent} from.
   */
  public TestRunStartedEvent(Parcel source) {
    testRun = new TestRunInfo(source);
    timeStamp = new TimeStamp(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testRun.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_RUN_STARTED;
  }
}
