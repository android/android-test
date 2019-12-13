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

/** Denotes that test discovery has finished */
public class TestDiscoveryFinishedEvent extends TestDiscoveryEvent {
  public static final Parcelable.Creator<TestDiscoveryFinishedEvent> CREATOR =
      new Parcelable.Creator<TestDiscoveryFinishedEvent>() {
        @Override
        public TestDiscoveryFinishedEvent createFromParcel(Parcel source) {
          return new TestDiscoveryFinishedEvent();
        }

        @Override
        public TestDiscoveryFinishedEvent[] newArray(int size) {
          return new TestDiscoveryFinishedEvent[size];
        }
      };
}
