/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Tests for {@link SuiteAssignmentPrinter} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SuiteAssignmentPrinterTest {

  @LargeTest
  private static class SampleTestClassWithSizeAnnotations {

    /** Use platform annotation to see if it works with both annotations */
    @android.test.suitebuilder.annotation.SmallTest
    @Test
    public void smallSizeTest() throws InterruptedException {}

    @MediumTest
    @Test
    public void mediumSizeTest() {}

    @Test
    public void noSizeTest() {}
  }

  @Spy public SuiteAssignmentPrinter suiteAssignmentPrinter;

  @Mock public Instrumentation instrumentation;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    suiteAssignmentPrinter.setInstrumentation(instrumentation);
  }

  @Test
  public void timingInvalid_sendsFailure() throws Exception {
    Description description =
        Description.createTestDescription(
            SampleTestClassWithSizeAnnotations.class, "smallSizeTest");

    doNothing().when(suiteAssignmentPrinter).sendStatus(anyInt(), any(Bundle.class));
    suiteAssignmentPrinter.timingValid = false;
    suiteAssignmentPrinter.testFinished(description);

    verify(suiteAssignmentPrinter).sendString(eq("F"));
  }

  @Test
  public void startTimeSmallerZero_sendsFailure() throws Exception {
    Description description =
        Description.createTestDescription(
            SampleTestClassWithSizeAnnotations.class, "smallSizeTest");

    // Don't output anything in instrumentation status
    doNothing().when(suiteAssignmentPrinter).sendStatus(anyInt(), any(Bundle.class));

    suiteAssignmentPrinter.startTime = -1;
    suiteAssignmentPrinter.testFinished(description);

    verify(suiteAssignmentPrinter).sendString(eq("F"));
  }

  @Test
  public void methodIsAssignedToWrongSuite_sendSuiteSuggestion() throws Exception {
    Description description =
        Description.createTestDescription(
            SampleTestClassWithSizeAnnotations.class,
            "mediumSizeTest",
            SampleTestClassWithSizeAnnotations.class.getMethod("mediumSizeTest").getAnnotations());

    // Don't output anything in instrumentation status
    doNothing().when(suiteAssignmentPrinter).sendStatus(anyInt(), any(Bundle.class));
    // Set end time
    when(suiteAssignmentPrinter.getCurrentTimeMillis()).thenReturn(100L);

    // Set time for small test bucket
    suiteAssignmentPrinter.timingValid = true;
    suiteAssignmentPrinter.startTime = 50L;
    suiteAssignmentPrinter.testFinished(description);

    verify(suiteAssignmentPrinter).sendString(contains("suggested: small"));
  }

  @Test
  public void methodIsAssignedToCorrectSuite() throws Exception {
    Description description =
        Description.createTestDescription(
            SampleTestClassWithSizeAnnotations.class,
            "mediumSizeTest",
            SampleTestClassWithSizeAnnotations.class.getMethod("mediumSizeTest").getAnnotations());

    // Don't output anything in instrumentation status
    doNothing().when(suiteAssignmentPrinter).sendStatus(anyInt(), any(Bundle.class));
    // Set end time
    when(suiteAssignmentPrinter.getCurrentTimeMillis()).thenReturn(1000L);

    // Set time for medium test bucket < 1000ms
    suiteAssignmentPrinter.timingValid = true;
    suiteAssignmentPrinter.startTime = 500L;
    suiteAssignmentPrinter.testFinished(description);

    verify(suiteAssignmentPrinter).sendString(contains("."));
  }

  @Test
  public void noMethodTestSizeFallsBackToClassTestSize() throws Exception {
    Description description =
        Description.createTestDescription(SampleTestClassWithSizeAnnotations.class, "noSizeTest");

    // Don't output anything in instrumentation status
    doNothing().when(suiteAssignmentPrinter).sendStatus(anyInt(), any(Bundle.class));

    // Set time for large test bucket < 1000ms
    suiteAssignmentPrinter.timingValid = true;
    suiteAssignmentPrinter.startTime = 0L;
    suiteAssignmentPrinter.testFinished(description);

    verify(suiteAssignmentPrinter).sendString(contains("."));
  }
}
