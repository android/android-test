/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.services.events.platform;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import androidx.test.services.events.platform.TestPlatformEvent.EventType;

/**
 * The factory for {@link TestPlatformEvent#CREATOR}. When the parcel was created, the first entry
 * written to it was the {@link TestPlatformEvent#instanceType()} enum value, so use the EventType
 * to instantiate the correct subclass.
 */
public final class TestPlatformEventFactory implements Creator<TestPlatformEvent> {
  @Override
  public TestPlatformEvent createFromParcel(Parcel source) {
    EventType instanceType = EventType.valueOf(source.readString());
    switch (instanceType) {
      case TEST_RUN_STARTED:
        return new TestRunStartedEvent(source);
      case TEST_RUN_ERROR:
        return new TestRunErrorEvent(source);
      case TEST_CASE_STARTED:
        return new TestCaseStartedEvent(source);
      case TEST_CASE_ERROR:
        return new TestCaseErrorEvent(source);
      case TEST_CASE_FINISHED:
        return new TestCaseFinishedEvent(source);
      case TEST_RUN_FINISHED:
        return new TestRunFinishedEvent(source);
    }
    throw new IllegalArgumentException("Unhandled event type: " + instanceType);
  }

  @Override
  public TestPlatformEvent[] newArray(int size) {
    return new TestPlatformEvent[size];
  }
}
