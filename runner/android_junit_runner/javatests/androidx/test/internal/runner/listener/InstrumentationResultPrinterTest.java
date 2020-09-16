/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.internal.runner.listener;

import static androidx.test.internal.runner.listener.InstrumentationResultPrinter.REPORT_KEY_STACK;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InstrumentationResultPrinterTest {

  /** Ensure the correct result code along with a stack trace was sent back to instrumentation */
  @Test
  public void testExecutionFlowDuringProcessCrash() throws Exception {
    int[] resultCode = new int[1];
    Bundle[] resultBundle = new Bundle[1];

    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            resultCode[0] = code;
            resultBundle[0] = bundle;
          }
        };

    // fake start test execution
    intrResultPrinter.testStarted(Description.EMPTY);
    Assert.assertEquals(InstrumentationResultPrinter.REPORT_VALUE_RESULT_START, resultCode[0]);
    Assert.assertFalse(resultBundle[0].containsKey(REPORT_KEY_STACK));

    // define and report a RunTimeException
    Throwable e = new RuntimeException();
    intrResultPrinter.reportProcessCrash(e);

    // ensure the correct result code along with a stack trace was sent back to instrumentation
    Assert.assertEquals(InstrumentationResultPrinter.REPORT_VALUE_RESULT_FAILURE, resultCode[0]);
    assertTrue(resultBundle[0].containsKey(REPORT_KEY_STACK));
  }

  @Test
  public void verifyFailureDescriptionPropagatedToStartAndFinishMethods() throws Exception {
    Description[] descriptions = new Description[2];
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void testStarted(Description description) throws Exception {
            descriptions[0] = description;
          }

          @Override
          public void testFinished(Description description) throws Exception {
            descriptions[1] = description;
          }
        };

    Description d = Description.createTestDescription(this.getClass(), "Failure Description");
    Failure testFailure = new Failure(d, new Exception());
    intrResultPrinter.testFailure(testFailure);

    assertEquals(d, descriptions[0]);
    assertEquals(d, descriptions[1]);
  }
}
