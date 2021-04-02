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
import androidx.test.services.events.ErrorInfo;
import androidx.test.services.events.TestRunInfo;
import androidx.test.services.events.TimeStamp;

/**
 * This event is sent when an error is encountered in a test run but cannot be attributed to any
 * specific test case. Multiple {@link TestRunErrorEvent}s may be reported. This event does not mean
 * the test run has finished.
 *
 * @see TestCaseErrorEvent for an event regarding errors for a specific test.
 * @see TestRunFinishedEvent to mark the test run as finished.
 */
public class TestRunErrorEvent extends TestPlatformEvent {
  /* The test run this error is attributed to */
  @NonNull public final TestRunInfo testRun;
  /* The error that occurred */
  @NonNull public final ErrorInfo error;
  /* The time when this error occurred */
  @NonNull public final TimeStamp timeStamp;

  /**
   * Constructor to create {@link TestRunErrorEvent}.
   *
   * @param testRun the test run this error should be attributed to.
   * @param error the error that occurred.
   * @param timeStamp the time when this error occurred.
   */
  public TestRunErrorEvent(
      @NonNull TestRunInfo testRun, @NonNull ErrorInfo error, @NonNull TimeStamp timeStamp) {
    this.testRun = checkNotNull(testRun, "testRun cannot be null");
    this.error = checkNotNull(error, "error cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestRunErrorEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestRunErrorEvent} from.
   */
  TestRunErrorEvent(Parcel source) {
    testRun = new TestRunInfo(source);
    error = new ErrorInfo(source);
    timeStamp = new TimeStamp(source);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_RUN_ERROR;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testRun.writeToParcel(parcel, i);
    error.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }
}
