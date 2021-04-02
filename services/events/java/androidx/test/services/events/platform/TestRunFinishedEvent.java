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
import androidx.test.services.events.TestStatus;
import androidx.test.services.events.TimeStamp;

/**
 * This event indicates that all tests in a test run are finished running. No more events should be
 * sent after this. This event should always be emitted.
 *
 * @see TestRunStartedEvent to begin a test run.
 */
public class TestRunFinishedEvent extends TestPlatformEvent {
  /* The test run that finished */
  public final TestRunInfo testRun;
  /* The overall status of the test run */
  public final TestStatus runStatus;
  /* The time that this test run finished */
  public final TimeStamp timeStamp;

  /**
   * Creates a {@link TestRunFinishedEvent}.
   *
   * @param testRun the test run that finished.
   * @param runStatus the overall status of the test run.
   * @param timeStamp the time that this test run finished.
   */
  public TestRunFinishedEvent(
      @NonNull TestRunInfo testRun, @NonNull TestStatus runStatus, @NonNull TimeStamp timeStamp) {
    this.testRun = checkNotNull(testRun, "testRun cannot be null");
    this.runStatus = checkNotNull(runStatus, "runStatus cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestRunFinishedEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestRunFinishedEvent} from.
   */
  TestRunFinishedEvent(Parcel source) {
    testRun = new TestRunInfo(source);
    runStatus = new TestStatus(source);
    timeStamp = new TimeStamp(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testRun.writeToParcel(parcel, i);
    runStatus.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_RUN_FINISHED;
  }
}
