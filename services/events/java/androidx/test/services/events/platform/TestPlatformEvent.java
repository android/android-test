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
import android.os.Parcelable;

/** Base class for all other {@code TestPlatformEvents} to extend. */
public abstract class TestPlatformEvent implements Parcelable {
  /** Creates a {@link TestPlatformEvent}. */
  TestPlatformEvent() {}

  /** Each derived class will return its corresponding EventType in {@link #instanceType()}. */
  enum EventType {
    TEST_RUN_STARTED,
    TEST_RUN_ERROR,
    TEST_RUN_FINISHED,
    TEST_CASE_STARTED,
    TEST_CASE_ERROR,
    TEST_CASE_FINISHED
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(instanceType().name());
  }

  /**
   * The {@code ITestPlatformEvent#send(TestPlatformEvent)} service method receives an instance of
   * the {@link TestPlatformEvent} base class, so the {@link #CREATOR} factory in this class is
   * being used to create the event instances, not the {@code CREATOR} of one of its derived
   * instances.
   *
   * <p>Therefore the {@code createFromParcel} method first needs to read a String containing the
   * EventType enum value of the correct derived type to instantiate. Derived classes should
   * override this method to return the applicable event type.
   *
   * <p>Also note that this means only this base class provides a {@code CREATOR}, since the derived
   * classes don't need one.
   *
   * @return the EventType of the final derived event class that extends this base class
   */
  abstract EventType instanceType();

  public static final Parcelable.Creator<TestPlatformEvent> CREATOR =
      new TestPlatformEventFactory();
}
