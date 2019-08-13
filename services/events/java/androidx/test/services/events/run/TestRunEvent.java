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

/** Base class for all other {@code TestRunEvents} to extend. */
public class TestRunEvent implements Parcelable {
  /** Creates a {@link TestRunEvent}. */
  TestRunEvent() {}

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
  }

  public static final Parcelable.Creator<TestRunEvent> CREATOR =
      new Parcelable.Creator<TestRunEvent>() {
        @Override
        public TestRunEvent createFromParcel(Parcel source) {
          return new TestRunEvent();
        }

        @Override
        public TestRunEvent[] newArray(int size) {
          return new TestRunEvent[size];
        }
      };
}
