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

package androidx.test.internal.runner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

/** Tests for {@link TestSize} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class TestSizeTest {

  private static class SampleTestClassWithAnnotatedMethods {

    @SmallTest
    @Test
    public void runnerFilterSmallSize() {}

    @MediumTest
    @Test
    public void runnerFilterMediumSize() {}

    @LargeTest
    @Test
    public void runnerFilterLargeSize() {}

    /** Use platform annotation to see if it works with both annotations types */
    @android.test.suitebuilder.annotation.SmallTest
    @Test
    public void platformFilterSmallSize() {}
  }

  private static class SampleTestClassNoSizeAnnotations {

    @Test
    public void notSizeAnnotatedTestMethod() {}
  }

  @SmallTest
  private static class SampleTestRunnerFilterSizeAnnotatedClass {

    @Test
    public void testMethod() {}
  }

  @SmallTest
  private static class SampleTestClassAndMethodSizeAnnotated {

    @MediumTest
    @Test
    public void mediumSize() {}
  }

  @android.test.suitebuilder.annotation.SmallTest
  private static class SampleTestPlatformSizeAnnotatedClass {

    @Test
    public void testMethod() {}
  }

  private TestSize testSize;

  @Before
  public void setUp() {
    testSize = TestSize.SMALL;
  }

  @Test
  public void methodIsAnnotatedWithTestSize_ReturnsTrue() throws NoSuchMethodException {
    Description runnerFilterSmallSizeTestDescription =
        Description.createTestDescription(
            SampleTestClassWithAnnotatedMethods.class,
            "runnerFilterSmallSize",
            SampleTestClassWithAnnotatedMethods.class
                .getMethod("runnerFilterSmallSize")
                .getAnnotations());
    assertThat(
        testSize.testMethodIsAnnotatedWithTestSize(runnerFilterSmallSizeTestDescription),
        equalTo(true));

    Description platformSmallSizeTestDescription =
        Description.createTestDescription(
            SampleTestClassWithAnnotatedMethods.class,
            "platformFilterSmallSize",
            SampleTestClassWithAnnotatedMethods.class
                .getMethod("platformFilterSmallSize")
                .getAnnotations());
    assertThat(
        testSize.testMethodIsAnnotatedWithTestSize(platformSmallSizeTestDescription),
        equalTo(true));
  }

  @Test
  public void methodNotAnnotatedWithTestSize_ReturnsFalse() throws NoSuchMethodException {
    Description runnerFilterSmallSizeTestDescription =
        Description.createTestDescription(
            SampleTestClassNoSizeAnnotations.class,
            "notSizeAnnotatedTestMethod",
            SampleTestClassNoSizeAnnotations.class
                .getMethod("notSizeAnnotatedTestMethod")
                .getAnnotations());
    assertThat(
        testSize.testMethodIsAnnotatedWithTestSize(runnerFilterSmallSizeTestDescription),
        equalTo(false));
  }

  @Test
  public void classNotAnnotatedWithTestSize_ReturnsTrue() {
    Description testClassRunnerFilterSmallSizeTestDescription =
        Description.createTestDescription(
            SampleTestRunnerFilterSizeAnnotatedClass.class, "testMethod");
    assertThat(
        testSize.testClassIsAnnotatedWithTestSize(testClassRunnerFilterSmallSizeTestDescription),
        equalTo(true));

    Description testClassPlatformSmallSizeTestDescription =
        Description.createTestDescription(SampleTestPlatformSizeAnnotatedClass.class, "testMethod");
    assertThat(
        testSize.testClassIsAnnotatedWithTestSize(testClassPlatformSmallSizeTestDescription),
        equalTo(true));
  }

  @Test
  public void classNotAnnotatedWithTestSize_ReturnsFalse() {
    Description testClassNoSmallSizeTestDescription =
        Description.createTestDescription(
            SampleTestClassNoSizeAnnotations.class, "notSizeAnnotatedTestMethod");
    assertThat(
        testSize.testClassIsAnnotatedWithTestSize(testClassNoSmallSizeTestDescription),
        equalTo(false));
  }

  @Test
  public void runTimeResolvesToCorrectSizeBucket() {
    float smallTestRunTime = 100 /* in ms*/;
    assertThat(TestSize.getTestSizeForRunTime(smallTestRunTime), equalTo(TestSize.SMALL));

    float mediumTestRunTime = 500 /* in ms*/;
    assertThat(TestSize.getTestSizeForRunTime(mediumTestRunTime), equalTo(TestSize.MEDIUM));

    float largeTestRunTime = 2000 /* in ms*/;
    assertThat(TestSize.getTestSizeForRunTime(largeTestRunTime), equalTo(TestSize.LARGE));
  }

  @Test
  public void isAnyTestSize_ContainsPlatformAndRunnerFilterAnnotations() {
    assertThat(TestSize.isAnyTestSize(SmallTest.class), equalTo(true));
    assertThat(TestSize.isAnyTestSize(MediumTest.class), equalTo(true));
    assertThat(TestSize.isAnyTestSize(LargeTest.class), equalTo(true));

    assertThat(
        TestSize.isAnyTestSize(android.test.suitebuilder.annotation.SmallTest.class),
        equalTo(true));
    assertThat(
        TestSize.isAnyTestSize(android.test.suitebuilder.annotation.MediumTest.class),
        equalTo(true));
    assertThat(
        TestSize.isAnyTestSize(android.test.suitebuilder.annotation.LargeTest.class),
        equalTo(true));
  }

  @Test
  public void isAnyTestSize_ReturnsFalseForInvalidClass() {
    assertThat(TestSize.isAnyTestSize(Suppress.class), equalTo(false));
  }

  @Test
  public void fromString_withValidSizeString_returnsInstance() {
    TestSize testSizeSmall = TestSize.fromString("small");
    assertThat(testSizeSmall, equalTo(TestSize.SMALL));

    TestSize testSizeMedium = TestSize.fromString("medium");
    assertThat(testSizeMedium, equalTo(TestSize.MEDIUM));

    TestSize testSizeLarge = TestSize.fromString("large");
    assertThat(testSizeLarge, equalTo(TestSize.LARGE));
  }

  @Test
  public void fromString_withInvalidInstance_returnsTestSizeNone() {
    TestSize testSizeSmall = TestSize.fromString("invalid");
    assertThat(testSizeSmall, equalTo(TestSize.NONE));
  }

  @Test
  public void fromDescription_MethodAnnotatedReturnsInstance() throws NoSuchMethodException {
    Description smallDescription =
        Description.createTestDescription(
            SampleTestClassWithAnnotatedMethods.class,
            "runnerFilterSmallSize",
            SampleTestClassWithAnnotatedMethods.class
                .getMethod("runnerFilterSmallSize")
                .getAnnotations());
    assertThat(TestSize.fromDescription(smallDescription), equalTo(TestSize.SMALL));

    Description mediumDescription =
        Description.createTestDescription(
            SampleTestClassWithAnnotatedMethods.class,
            "runnerFilterMediumSize",
            SampleTestClassWithAnnotatedMethods.class
                .getMethod("runnerFilterMediumSize")
                .getAnnotations());
    assertThat(TestSize.fromDescription(mediumDescription), equalTo(TestSize.MEDIUM));

    Description largeDescription =
        Description.createTestDescription(
            SampleTestClassWithAnnotatedMethods.class,
            "runnerFilterLargeSize",
            SampleTestClassWithAnnotatedMethods.class
                .getMethod("runnerFilterLargeSize")
                .getAnnotations());
    assertThat(TestSize.fromDescription(largeDescription), equalTo(TestSize.LARGE));
  }

  @Test
  public void fromDescription_ClassAnnotationReturnsInstance() {
    Description description =
        Description.createTestDescription(
            SampleTestRunnerFilterSizeAnnotatedClass.class, "testMethod");
    assertThat(TestSize.fromDescription(description), equalTo(TestSize.SMALL));
  }

  @Test
  public void fromDescription_ClassAndMethodAnnotationReturnsMethodSizeInstance()
      throws NoSuchMethodException {
    Description description =
        Description.createTestDescription(
            SampleTestClassAndMethodSizeAnnotated.class,
            "mediumSize",
            SampleTestClassAndMethodSizeAnnotated.class.getMethod("mediumSize").getAnnotations());
    assertThat(TestSize.fromDescription(description), equalTo(TestSize.MEDIUM));
  }

  @Test
  public void fromDescription_NoSizeAnnotationsReturnsNoneInstance() throws NoSuchMethodException {
    Description description =
        Description.createTestDescription(
            SampleTestClassNoSizeAnnotations.class,
            "notSizeAnnotatedTestMethod",
            SampleTestClassNoSizeAnnotations.class
                .getMethod("notSizeAnnotatedTestMethod")
                .getAnnotations());
    assertThat(TestSize.fromDescription(description), equalTo(TestSize.NONE));
  }
}
