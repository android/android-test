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

package androidx.test.services.events.notification;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.test.services.events.TestCase;

/**
 * Base class for all [TestEvent]s to implement. Every [TestEvent] must have a [TestCase] for it to
 * identified.
 */
public class TestEvent implements Parcelable {

  public TestCase getTestCase() {
    return testCase;
  }

  private final TestCase testCase;

  /**
   * Constructor to create an {@link TestEvent} from an Android Parcel.
   *
   * @param source Android {@link Parcel} to read from.
   */
  TestEvent(Parcel source) {

    testCase = new TestCase(source);
  }

  /**
   * Constructor to create a {@link TestEvent}
   *
   * @param testCase the test case this event represents,
   */
  TestEvent(TestCase testCase) {

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

  public static final Parcelable.Creator<TestEvent> CREATOR =
      new Parcelable.Creator<TestEvent>() {
        @Override
        public TestEvent createFromParcel(Parcel source) {
          return new TestEvent(source);
        }

        @Override
        public TestEvent[] newArray(int size) {
          return new TestEvent[size];
        }
      };
}
