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
import androidx.test.services.events.TestCase;

/**
 * Base class for all [TestRunEvent]s to implement. Every {@link TestRunEvent} must have a {@link
 * TestCase} for it to be identified.
 */
public class TestRunEvent implements Parcelable {

  /** Returns the {@link TestCase} this event is associated to. */
  public TestCase getTestCase() {
    return testCase;
  }

  private final TestCase testCase;

  /**
   * Creates a {@link TestRunEvent} from an {@link Parcel}.
   *
   * @param source Android {@link Parcel} to read from.
   */
  TestRunEvent(Parcel source) {
    testCase = new TestCase(source);
  }

  /**
   * Constructor to create a {@link TestRunEvent}
   *
   * @param testCase the test case this event represents,
   */
  public TestRunEvent(TestCase testCase) {
    this.testCase = testCase;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    testCase.writeToParcel(parcel, 0);
  }

  public static final Parcelable.Creator<TestRunEvent> CREATOR =
      new Parcelable.Creator<TestRunEvent>() {
        @Override
        public TestRunEvent createFromParcel(Parcel source) {
          return new TestRunEvent(source);
        }

        @Override
        public TestRunEvent[] newArray(int size) {
          return new TestRunEvent[size];
        }
      };
}
