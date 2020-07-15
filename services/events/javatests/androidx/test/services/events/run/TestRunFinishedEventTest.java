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
import androidx.test.services.events.FailureInfo;
import androidx.test.services.events.TestCaseInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link TestRunFinishedEvent} parcelable. We write and read from the parcel to
 * verify that {@link TestRunEvent#CREATOR} instantiates it correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestRunFinishedEventTest {

  @Test
  public void testRunFinishedEvent_created_by_createFromParcel() {
    int count = 3;
    int ignoreCount = 1;
    int runTime = 5;
    String className = "Class";
    String methodName = "Method";
    String failureMessage = "Message";
    String failureType = "FailureType";
    String stackTrace = "StackTrace";
    TestCaseInfo testCase =
        new TestCaseInfo(className, methodName, new ArrayList<>(), new ArrayList<>());
    FailureInfo failure = new FailureInfo(failureMessage, failureType, stackTrace, testCase);
    List<FailureInfo> failures = new ArrayList<>(1);
    failures.add(failure);
    TestRunFinishedEvent testRunFinishedEvent =
        new TestRunFinishedEvent(count, ignoreCount, runTime, failures);

    Parcel parcel = Parcel.obtain();
    testRunFinishedEvent.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);
    TestRunEvent testRunEventFromParcel = TestRunEvent.CREATOR.createFromParcel(parcel);
    assertThat(testRunEventFromParcel).isInstanceOf(TestRunFinishedEvent.class);

    TestRunFinishedEvent result = (TestRunFinishedEvent) testRunEventFromParcel;
    assertThat(result.count).isEqualTo(count);
    assertThat(result.ignoreCount).isEqualTo(ignoreCount);
    assertThat(result.runTime).isEqualTo(runTime);
    assertThat(result.failures).hasSize(1);
    assertThat(result.failures.get(0).testCase.className).isEqualTo(className);
    assertThat(result.failures.get(0).testCase.methodName).isEqualTo(methodName);
    assertThat(result.failures.get(0).failureMessage).isEqualTo(failureMessage);
    assertThat(result.failures.get(0).failureType).isEqualTo(failureType);
    assertThat(result.failures.get(0).stackTrace).isEqualTo(stackTrace);
  }
}
