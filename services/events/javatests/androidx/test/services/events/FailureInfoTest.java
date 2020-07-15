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

package androidx.test.services.events;

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Class to test parcelable {@link FailureInfo}. We write and read from the parcel to test every
 * thing done correctly.
 */
@RunWith(AndroidJUnit4.class)
public class FailureInfoTest {

  @Test
  public void failureToParcelableTest_basicException() {
    String stackTrace = "DummyTrace";
    String exceptionType = "NullPointerException";
    String message = "message";
    TestCaseInfo testCase =
        new TestCaseInfo("MyTest", "testFoo", ImmutableList.of(), ImmutableList.of());
    FailureInfo failure = new FailureInfo(message, exceptionType, stackTrace, testCase);
    Parcel parcel = Parcel.obtain();
    failure.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    FailureInfo failureFromParcel = FailureInfo.CREATOR.createFromParcel(parcel);

    assertThat(failureFromParcel.failureMessage).isEqualTo(message);
    assertThat(failureFromParcel.failureType).isEqualTo(exceptionType);
    assertThat(failureFromParcel.stackTrace).isEqualTo(stackTrace);
    assertThat(failureFromParcel.testCase.className).isEqualTo("MyTest");
    assertThat(failureFromParcel.testCase.methodName).isEqualTo("testFoo");
  }
}
