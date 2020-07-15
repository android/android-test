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

package androidx.test.services.events.run;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import androidx.test.services.events.run.TestRunEvent.EventType;

/**
 * The factory for {@link TestRunEvent#CREATOR}. When the parcel was created, the first entry
 * written to it was the {@link TestRunEvent#instanceType()} enum value, so use the EventType to
 * instantiate the correct subclass.
 */
final class TestRunEventFactory implements Creator<TestRunEvent> {
  @Override
  public TestRunEvent createFromParcel(Parcel source) {
    EventType instanceType = EventType.valueOf(source.readString());
    switch (instanceType) {
      case STARTED:
        return new TestRunStartedEvent(source);
      case TEST_STARTED:
        return new TestStartedEvent(source);
      case TEST_FINISHED:
        return new TestFinishedEvent(source);
      case TEST_ASSUMPTION_FAILURE:
        return new TestAssumptionFailureEvent(source);
      case TEST_FAILURE:
        return new TestFailureEvent(source);
      case TEST_IGNORED:
        return new TestIgnoredEvent(source);
      case FINISHED:
        return new TestRunFinishedEvent(source);
    }
    throw new IllegalArgumentException("Unhandled event type: " + instanceType);
  }

  @Override
  public TestRunEvent[] newArray(int size) {
    return new TestRunEvent[size];
  }
}
