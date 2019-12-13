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
import android.os.Parcelable;
import androidx.test.services.events.Failure;
import androidx.test.services.events.TestCase;

/**
 * Denotes that the test ended with a TEST_ASSUMPTION_FAILURE. It has the {@link Failure} object to
 * denote what was the cause of the failure/error.
 */
public class TestAssumptionFailureEvent extends TestFailureEvent {
  /**
   * Creates a {@link TestAssumptionFailureEvent} from {@link TestCase} and {@link Failure}.
   *
   * @param testCase the test case that this event is for.
   * @param failure the failure associated with the test case.
   */
  public TestAssumptionFailureEvent(TestCase testCase, Failure failure) {
    super(testCase, failure);
  }

  private TestAssumptionFailureEvent(Parcel source) {
    super(source);
  }

  public static final Parcelable.Creator<TestAssumptionFailureEvent> CREATOR =
      new Parcelable.Creator<TestAssumptionFailureEvent>() {
        @Override
        public TestAssumptionFailureEvent createFromParcel(Parcel source) {
          return new TestAssumptionFailureEvent(source);
        }

        @Override
        public TestAssumptionFailureEvent[] newArray(int size) {
          return new TestAssumptionFailureEvent[size];
        }
      };
}
