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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.test.services.events.TestCaseInfo;

/** Represents a {@link TestRunEvent} with an associated {@link TestCaseInfo}. */
public abstract class TestRunEventWithTestCase extends TestRunEvent {
  /** The {@link TestCaseInfo} this event is associated with. */
  @NonNull public final TestCaseInfo testCase;

  /**
   * Creates a {@link TestRunEventWithTestCase} from an {@link Parcel}.
   *
   * @param source {@link Parcel} to create the {@link TestCaseInfo} from
   */
  TestRunEventWithTestCase(Parcel source) {
    testCase = new TestCaseInfo(source);
  }

  /**
   * Creates a {@link TestRunEventWithTestCase}.
   *
   * @param testCase the test case this event represents,
   */
  TestRunEventWithTestCase(@NonNull TestCaseInfo testCase) {
    checkNotNull(testCase, "testCase cannot be null");
    this.testCase = testCase;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testCase.writeToParcel(parcel, i);
  }
}
