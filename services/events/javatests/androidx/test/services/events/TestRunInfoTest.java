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

package androidx.test.services.events;

import static androidx.test.services.events.ParcelableConverter.getAnnotationsFromArray;
import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the parcelable {@link TestRunInfo}. We write and read from the parcel to test
 * everything is done correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestRunInfoTest {
  private static final String CLASS_NAME = "Class1";
  private static final String METHOD_NAME = "Method1";

  @Test
  public void testCaseToParcelableTest_basicClassNameAndMethodNameGiven() {
    String className2 = "Class2";
    String methodName2 = "Method2";
    List<TestCaseInfo> testCases = new ArrayList<>();
    testCases.add(new TestCaseInfo(CLASS_NAME, METHOD_NAME, new ArrayList<>(), new ArrayList<>()));
    testCases.add(new TestCaseInfo(className2, methodName2, new ArrayList<>(), new ArrayList<>()));
    TestRunInfo testRun = new TestRunInfo(CLASS_NAME, testCases);
    Parcel parcel = Parcel.obtain();
    testRun.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestRunInfo testRunFromParcel = TestRunInfo.CREATOR.createFromParcel(parcel);

    assertThat(testRunFromParcel.testRunName).isEqualTo(CLASS_NAME);
    assertThat(testRunFromParcel.testCases.size()).isEqualTo(2);
    assertThat(testRunFromParcel.testCases.get(0).className).isEqualTo(CLASS_NAME);
    assertThat(testRunFromParcel.testCases.get(0).methodName).isEqualTo(METHOD_NAME);
    assertThat(testRunFromParcel.testCases.get(1).className).isEqualTo(className2);
    assertThat(testRunFromParcel.testCases.get(1).methodName).isEqualTo(methodName2);
  }

  @DummyAnnotation(
      foo = "bar",
      numbers = {1, 2})
  @SuppressWarnings("unused")
  private static void testMethodToGetAnnotations() {}

  @Test
  public void testRunToParcelableTest_testCasesKeepAnnotationInfo() throws NoSuchMethodException {
    Annotation[] annotations =
        TestRunInfoTest.class
            .getDeclaredMethod("testMethodToGetAnnotations")
            .getDeclaredAnnotations();

    List<TestCaseInfo> testCases = new ArrayList<>();
    testCases.add(
        new TestCaseInfo(
            CLASS_NAME, METHOD_NAME, getAnnotationsFromArray(annotations), new ArrayList<>()));
    TestRunInfo testRun = new TestRunInfo(CLASS_NAME, testCases);
    Parcel parcel = Parcel.obtain();
    testRun.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestRunInfo testRunFromParcel = TestRunInfo.CREATOR.createFromParcel(parcel);

    assertThat(testRunFromParcel.testRunName).isEqualTo(CLASS_NAME);
    assertThat(testRunFromParcel.testCases.size()).isEqualTo(1);
    TestCaseInfo testCaseFromParcel = testRunFromParcel.testCases.get(0);
    assertThat(testCaseFromParcel.className).isEqualTo(CLASS_NAME);
    assertThat(testCaseFromParcel.methodName).isEqualTo(METHOD_NAME);

    // Assertion for the inserted method annotation
    assertThat(testCaseFromParcel.methodAnnotations).isNotEmpty();
    assertThat(testCaseFromParcel.methodAnnotations.get(0).name)
        .isEqualTo(DummyAnnotation.class.getName());
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).fieldName)
        .isEqualTo("foo");
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).fieldValues)
        .containsExactly("bar");
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).valueType)
        .isEqualTo("String");

    assertThat(testCaseFromParcel.classAnnotations).isEmpty();
  }

  /**
   * Dummy {@link java.lang.annotation.Annotation} to test if the Java annotations are parsed and
   * converted correctly into {@link AnnotationInfo} and {@link AnnotationValue} parcelables.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  @interface DummyAnnotation {
    String foo();

    int[] numbers();
  }
}
