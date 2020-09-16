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
package androidx.test.services.events.internal;

import static androidx.test.services.events.internal.StackTrimmer.MAX_TRACE_SIZE;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.assertTrue;

import android.os.Environment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 19)
public class StackTrimmerTest {

  @Test
  public void verifyFailureStackTraceIsTrimmed() throws Exception {

    Failure testFailure = new Failure(Description.EMPTY, new Exception());

    // ensure trace contains the current method, but not any of the junit + androidx.test framework
    // traces
    String trace = StackTrimmer.getTrimmedStackTrace(testFailure);
    assertThat(trace).contains("StackTrimmerTest.verifyFailureStackTraceIsTrimmed");
    // negative test are suspect, but just check for a few known trace elements
    assertThat(trace).doesNotContain("androidx.test.runner.AndroidJUnitRunner.onStart");
    assertThat(trace).doesNotContain("org.junit.runners.ParentRunner.run");

    Environment.getDataDirectory();
  }

  @Test
  public void verifyFailureStackTraceIsTruncated() throws Exception {
    Failure testFailure = new Failure(Description.EMPTY, new Exception(getVeryLargeString()));

    int testResultTraceLength = StackTrimmer.getTrimmedStackTrace(testFailure).length() - 1;
    assertTrue(
        String.format(
            "The stack trace length: %s, exceeds the max: %s",
            testResultTraceLength, MAX_TRACE_SIZE),
        testResultTraceLength <= MAX_TRACE_SIZE);
  }

  private static String getVeryLargeString() {
    return new String(new char[1000000]);
  }
}
