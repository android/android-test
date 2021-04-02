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

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.services.events.ErrorInfo;
import androidx.test.services.events.TestCaseInfo;
import androidx.test.services.events.TimeStamp;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link TestCaseErrorEvent} parcelable. We write and read from the parcel to
 * verify that {@link TestCaseErrorEvent#CREATOR} instantiates it correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestCaseErrorEventTest {

  @Test
  public void testCaseErrorEvent_created_by_createFromParcel() {
    String className = "Class";
    String methodName = "Method";
    String errorMessage = "Message";
    String errorType = "ErrorType";
    String stackTrace = "StackTrace";
    TestCaseInfo testCase =
        new TestCaseInfo(className, methodName, new ArrayList<>(), new ArrayList<>());
    ErrorInfo error = new ErrorInfo(errorMessage, errorType, stackTrace);
    long seconds = 123;
    int nanos = 456;
    TimeStamp timestamp = new TimeStamp(seconds, nanos);

    TestCaseErrorEvent testFailureEvent = new TestCaseErrorEvent(testCase, error, timestamp);

    Parcel parcel = Parcel.obtain();
    testFailureEvent.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);
    TestPlatformEvent testPlatformEventFromParcel =
        TestPlatformEvent.CREATOR.createFromParcel(parcel);
    assertThat(testPlatformEventFromParcel).isInstanceOf(TestCaseErrorEvent.class);

    TestCaseErrorEvent result = (TestCaseErrorEvent) testPlatformEventFromParcel;
    assertThat(result.testCase.className).isEqualTo(className);
    assertThat(result.testCase.methodName).isEqualTo(methodName);
    assertThat(result.error.errorMessage).isEqualTo(errorMessage);
    assertThat(result.error.errorType).isEqualTo(errorType);
    assertThat(result.error.stackTrace).isEqualTo(stackTrace);
    assertThat(result.timeStamp.seconds).isEqualTo(seconds);
    assertThat(result.timeStamp.nanos).isEqualTo(nanos);
  }
}
