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

package androidx.test.services.events.discovery;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.test.services.events.TestCase;

/**
 * Denotes that test discovery has found a test case. The {@link TestCase} is provided along with
 * the event.
 */
public class TestFoundEvent extends TestDiscoveryEvent {
  public final TestCase testCase;

  public TestFoundEvent(TestCase testCase) {
    this.testCase = testCase;
  }

  private TestFoundEvent(Parcel source) {
    this.testCase = new TestCase(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    testCase.writeToParcel(parcel, i);
  }

  public static final Parcelable.Creator<TestFoundEvent> CREATOR =
      new Parcelable.Creator<TestFoundEvent>() {
        @Override
        public TestFoundEvent createFromParcel(Parcel source) {
          return new TestFoundEvent(source);
        }

        @Override
        public TestFoundEvent[] newArray(int size) {
          return new TestFoundEvent[size];
        }
      };
}
