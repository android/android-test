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

import static androidx.test.services.events.ParcelableConverter.getAnnotationsFromArray;
import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the parcelable {@link TestCase}. We write and read from the parcel to test every
 * thing done correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestCaseParcelableTest {

  private final String className = "DummyTestClass";
  private final String methodName = "DummyTestMethod";

  @Test
  public void testCaseToParcelableTest_basicClassNameAndMethodNameGiven() {

    TestCase testCase = new TestCase(className, methodName, new ArrayList<>(), new ArrayList<>());
    Parcel parcel = Parcel.obtain();
    testCase.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestCase testCaseFromParcel = TestCase.CREATOR.createFromParcel(parcel);

    assertThat(testCaseFromParcel.className).isEqualTo(className);
    assertThat(testCaseFromParcel.methodName).isEqualTo(methodName);
    assertThat(testCaseFromParcel.methodAnnotations).isEmpty();
    assertThat(testCaseFromParcel.classAnnotations).isEmpty();
  }

  @DummyAnnotation(
      foo = "bar",
      numbers = {1, 2})
  public void testMethodToGetAnnotations() {}

  @Test
  public void testCaseToParcelableTest_withMethodAnnotations() throws NoSuchMethodException {
    Annotation[] annotations =
        TestCaseParcelableTest.class
            .getDeclaredMethod("testMethodToGetAnnotations")
            .getDeclaredAnnotations();

    TestCase testCase =
        new TestCase(
            className, methodName, getAnnotationsFromArray(annotations), new ArrayList<>());
    Parcel parcel = Parcel.obtain();
    testCase.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestCase testCaseFromParcel = TestCase.CREATOR.createFromParcel(parcel);

    assertThat(testCaseFromParcel.className).isEqualTo(className);
    assertThat(testCaseFromParcel.methodName).isEqualTo(methodName);

    // Assertion for the inserted method annotation
    assertThat(testCaseFromParcel.methodAnnotations).isNotEmpty();
    assertThat(testCaseFromParcel.methodAnnotations.get(0).name)
        .isEqualTo("androidx.test.services.events.DummyAnnotation");
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).fieldName)
        .isEqualTo("foo");
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).fieldValues)
        .containsExactly("bar");
    assertThat(testCaseFromParcel.methodAnnotations.get(0).values.get(0).valueType)
        .isEqualTo("String");

    assertThat(testCaseFromParcel.classAnnotations).isEmpty();
  }
}
