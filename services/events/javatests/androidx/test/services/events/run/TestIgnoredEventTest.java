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

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.services.events.TestCaseInfo;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link TestIgnoredEvent} parcelable. We write and read from the parcel to
 * verify that {@link TestRunEvent#CREATOR} instantiates it correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestIgnoredEventTest {

  @Test
  public void testIgnoredEvent_created_by_createFromParcel() {
    String className = "Class";
    String methodName = "Method";
    TestCaseInfo testCase =
        new TestCaseInfo(className, methodName, new ArrayList<>(), new ArrayList<>());

    TestIgnoredEvent testIgnoredEvent = new TestIgnoredEvent(testCase);

    Parcel parcel = Parcel.obtain();
    testIgnoredEvent.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);
    TestRunEvent testRunEventFromParcel = TestRunEvent.CREATOR.createFromParcel(parcel);
    assertThat(testRunEventFromParcel).isInstanceOf(TestIgnoredEvent.class);

    TestIgnoredEvent result = (TestIgnoredEvent) testRunEventFromParcel;
    assertThat(result.testCase.className).isEqualTo(className);
    assertThat(result.testCase.methodName).isEqualTo(methodName);
  }
}
