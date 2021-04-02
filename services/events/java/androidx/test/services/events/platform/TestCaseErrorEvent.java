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
import androidx.test.services.events.TestCaseInfo;
import androidx.test.services.events.TimeStamp;

/**
 * This event is sent when an error is encountered while a test case is running. This does not
 * necessarily mean that the test case has failed - not all exceptions are considered fatal.
 *
 * @see TestCaseFinishedEvent to signal an individual test case as completed.
 * @see TestRunErrorEvent to signal errors that are not able to be attributed to an individual test
 *     case.
 */
public class TestCaseErrorEvent extends TestPlatformEvent {
  @NonNull public final TestCaseInfo testCase;
  @NonNull public final ErrorInfo error;
  @NonNull public final TimeStamp timeStamp;

  /**
   * Constructor to create {@link TestCaseErrorEvent}.
   *
   * @param testCase the test case that should be associated with this error.
   * @param error details of the error that was encountered.
   * @param timeStamp time at which this error was encountered.
   */
  public TestCaseErrorEvent(
      @NonNull TestCaseInfo testCase, @NonNull ErrorInfo error, @NonNull TimeStamp timeStamp) {
    this.testCase = checkNotNull(testCase, "testCase cannot be null");
    this.error = checkNotNull(error, "error cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestCaseErrorEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestCaseErrorEvent} from.
   */
  TestCaseErrorEvent(Parcel source) {
    testCase = new TestCaseInfo(source);
    error = new ErrorInfo(source);
    timeStamp = new TimeStamp(source);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_CASE_ERROR;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testCase.writeToParcel(parcel, i);
    error.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }
}
