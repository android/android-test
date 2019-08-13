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
import androidx.test.services.events.TestCase;

/**
 * Denotes that the test ended with a TEST_IGNORED. It has the {@link TestCase} object to denote
 * which case this event is associated to.
 */
public class TestIgnoredEvent extends TestRunEventWithTestCase {
  /**
   * Constructor to create {@link TestFinishedEvent}.
   *
   * @param testCase the test case that this event is for.
   */
  public TestIgnoredEvent(TestCase testCase) {
    super(testCase);
  }

  private TestIgnoredEvent(Parcel source) {
    super(source);
  }

  public static final Creator<TestIgnoredEvent> CREATOR =
      new Creator<TestIgnoredEvent>() {
        @Override
        public TestIgnoredEvent createFromParcel(Parcel source) {
          return new TestIgnoredEvent(source);
        }

        @Override
        public TestIgnoredEvent[] newArray(int size) {
          return new TestIgnoredEvent[size];
        }
      };
}
