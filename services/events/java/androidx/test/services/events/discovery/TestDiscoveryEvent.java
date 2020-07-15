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

/** Base class for different test discovery events to implement. */
public abstract class TestDiscoveryEvent implements Parcelable {
  TestDiscoveryEvent() {}

  /** Each derived class will return its corresponding EventType in {@link #instanceType()}. */
  enum EventType {
    STARTED,
    TEST_FOUND,
    FINISHED
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    // The first entry in the Parcel is the EventType enum value to identify the derived class type.
    parcel.writeString(instanceType().name());
  }

  /**
   * The {@code ITestDiscoveryEvent#send(TestDiscoveryEvent)} service method receives an instance of
   * the {@link TestDiscoveryEvent} base class, so the {@link #CREATOR} factory in this class is
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

  public static final Parcelable.Creator<TestDiscoveryEvent> CREATOR =
      new TestDiscoveryEventFactory();
}
