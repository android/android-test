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
import androidx.test.services.events.TimeStamp;

/**
 * Represents the start of an individual test case. One {@link TestCaseStartedEvent} should always
 * be emitted for each individual test case in a test run. However, there are a couple of test
 * statuses that do not require an explicit {@link TestCaseStartedEvent}.
 *
 * <ul>
 *   <li>{@link androidx.test.services.events.TestStatus.Status.IGNORED}: The test was never started
 *       and will not be run.
 *   <li>{@link androidx.test.services.events.TestStatus.Status.CANCELLED}: The test was supposed to
 *       run, but this client encountered a state that required it to exit before the test could
 *       run.
 * </ul>
 *
 * @see TestCaseFinishedEvent to indicate the end of a test case
 * @see TestCaseErrorEvent for reporting various errors encountered during a test run.
 */
public final class TestCaseStartedEvent extends TestPlatformEvent {
  /** The test case that started. */
  @NonNull public final TestCaseInfo testCase;
  /** The time this test case started. */
  @NonNull public final TimeStamp timeStamp;

  /**
   * Creates a {@link TestCaseStartedEvent}.
   *
   * @param testCase the test case that started.
   * @param timeStamp the time the test case began.
   */
  public TestCaseStartedEvent(@NonNull TestCaseInfo testCase, @NonNull TimeStamp timeStamp) {
    this.testCase = checkNotNull(testCase, "testCase cannot be null");
    this.timeStamp = checkNotNull(timeStamp, "timeStamp cannot be null");
  }

  /**
   * Creates a {@link TestCaseStartedEvent} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestCaseStartedEvent} from.
   */
  public TestCaseStartedEvent(Parcel source) {
    testCase = new TestCaseInfo(source);
    timeStamp = new TimeStamp(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testCase.writeToParcel(parcel, i);
    timeStamp.writeToParcel(parcel, i);
  }

  @Override
  public EventType instanceType() {
    return EventType.TEST_CASE_STARTED;
  }
}
