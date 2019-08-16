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
import androidx.test.services.events.TestCase;

/**
 * Denotes that the test ended with a TEST_RUN_FINISHED. It has the {@link TestCase} object to
 * denote which case this event is associated to.
 */
public class TestRunFinishedEvent extends TestEvent {

  /**
   * Constructor to create an {@link TestEvent} from an Android Parcel.
   *
   * @param source Android {@link Parcel} to read from.
   */
  TestRunFinishedEvent(Parcel source) {
    super(source);
  }

  /**
   * Constructor to create {@link TestRunFinishedEvent}.
   *
   * @param testCase the test case that this event is for.
   */
  TestRunFinishedEvent(TestCase testCase) {
    super(testCase);
  }

  public static final Creator<TestRunFinishedEvent> CREATOR =
      new Creator<TestRunFinishedEvent>() {
        @Override
        public TestRunFinishedEvent createFromParcel(Parcel source) {
          return new TestRunFinishedEvent(source);
        }

        @Override
        public TestRunFinishedEvent[] newArray(int size) {
          return new TestRunFinishedEvent[size];
        }
      };
}
