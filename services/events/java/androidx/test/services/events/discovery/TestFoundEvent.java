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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.test.services.events.TestCaseInfo;

/**
 * Denotes that test discovery has found a test case. The {@link TestCaseInfo} is provided along
 * with the event.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class TestFoundEvent extends TestDiscoveryEvent {
  @NonNull public final TestCaseInfo testCase;

  public TestFoundEvent(@NonNull TestCaseInfo testCase) {
    checkNotNull(testCase, "testCase cannot be null");
    this.testCase = testCase;
  }

  TestFoundEvent(Parcel source) {
    this.testCase = new TestCaseInfo(source);
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    testCase.writeToParcel(parcel, i);
  }

  @Override
  EventType instanceType() {
    return EventType.TEST_FOUND;
  }
}
