/*
 * Copyright (C) 2020 The Android Open Source Project
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
import android.os.Parcelable.Creator;
import androidx.test.services.events.discovery.TestDiscoveryEvent.EventType;

/**
 * The factory for {@link TestDiscoveryEvent#CREATOR}. When the parcel was created, the first entry
 * written to it was the {@link TestDiscoveryEvent#instanceType()} enum value, so use the EventType
 * to instantiate the correct subclass.
 */
final class TestDiscoveryEventFactory implements Creator<TestDiscoveryEvent> {
  @Override
  public TestDiscoveryEvent createFromParcel(Parcel source) {
    EventType instanceType = EventType.valueOf(source.readString());
    switch (instanceType) {
      case STARTED:
        return new TestDiscoveryStartedEvent();
      case TEST_FOUND:
        return new TestFoundEvent(source);
      case FINISHED:
        return new TestDiscoveryFinishedEvent();
    }
    throw new IllegalArgumentException("Unhandled event type: " + instanceType);
  }

  @Override
  public TestDiscoveryEvent[] newArray(int size) {
    return new TestDiscoveryEvent[size];
  }
}
