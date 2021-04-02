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
import androidx.test.services.events.TestCaseInfo;
import androidx.test.services.events.TestStatus;
import androidx.test.services.events.TimeStamp;

/**
 * Marks the end of an individual test case and the overall status of that test.
 *
 * <p>There should always be a {@link TestCaseFinishedEvent} emitted for all test cases in a test
 * run, though there are a couple of exceptions.
 *
 * <ul>
 *   <li>{@link TestStatus.Status.IGNORED}: The test was never started and will not be run.
 *   <li>{@link TestStatus.Status.CANCELLED}: The test was supposed to run, but this client
 *       encountered a state that required it to exit before the test could run.
 * </ul>
 *
 * @see TestCaseStartedEvent to signal that a test case has started.
 * @see TestCaseErrorEvent for reporting errors that occurred while this test case was running.
 * @see TestStatus.Status for more information on our test statuses.
 */
public class TestCaseFinishedEvent extends TestPlatformEvent {
  /* The test case that finished */
  @NonNull public final TestCaseInfo testCase;
  /* The final status of this test case */
  @NonNull public final TestStatus testStatus;
  /* The time this test was finished */
  @NonNull public final TimeStamp timeStamp;
  /**
   * Constructor to create {@link TestCaseFinishedEvent}.
   *
   * @param testCase the test case that finished.
   * @param testStatus the final status of this test case.
   * @param timeStamp the time this test was finished.
   */
  public TestCaseFinishedEvent(
      @NonNull TestCaseInfo testCase,
      @NonNull TestStatus testStatus,
      @NonNull TimeStamp timeStamp) {
    this.testCase = checkNotNull(testCase, "testCase cannot be null");
    this.testStatus = checkNotNull(testStatus, "testStatus cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestCaseFinishedEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestCaseFinishedEvent} from.
   */
  TestCaseFinishedEvent(Parcel source) {
    this.testCase = new TestCaseInfo(source);
    this.testStatus = new TestStatus(source);
    this.timeStamp = new TimeStamp(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testCase.writeToParcel(parcel, i);
    testStatus.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_CASE_FINISHED;
  }
}
