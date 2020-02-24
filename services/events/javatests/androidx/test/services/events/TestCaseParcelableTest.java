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
import static org.junit.Assert.fail;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Class to test parcelable {@link TestCase}. We write and read from the parcel to test every thing
 * done correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestCaseParcelableTest {

  private final String className = "DummyTestClass";
  private final String methodName = "DummyTestMethod";
  private List<Annotation> annotations;

  @Test
  public void testCaseToParcelableTest_basicClassNameAndMethodNameGiven() {

    TestCase testCase = new TestCase(className, methodName, new ArrayList<>(), new ArrayList<>());
    Parcel parcel = Parcel.obtain();
    testCase.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestCase testCaseFromParcel = TestCase.CREATOR.createFromParcel(parcel);

    assertThat(testCaseFromParcel.getClassName()).isEqualTo(className);
    assertThat(testCaseFromParcel.getMethodName()).isEqualTo(methodName);
    assertThat(testCaseFromParcel.getMethodAnnotations()).isEmpty();
    assertThat(testCaseFromParcel.getClassAnnotations()).isEmpty();
  }

  @DummyAnnotation(foo = "bar")
  private void dummyMethodToGetAnnotations() {}

  @Test
  public void testCaseToParcelableTest_withMethodAnnotations() {

    try {
      annotations =
          Arrays.asList(
              TestCaseParcelableTest.class
                  .getDeclaredMethod("dummyMethodToGetAnnotations")
                  .getDeclaredAnnotations());

    } catch (NoSuchMethodException e) {
      fail(e.toString());
    }

    TestCase testCase = new TestCase(className, methodName, annotations, new ArrayList<>());
    Parcel parcel = Parcel.obtain();
    testCase.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TestCase testCaseFromParcel = TestCase.CREATOR.createFromParcel(parcel);

    assertThat(testCaseFromParcel.getClassName()).isEqualTo(className);
    assertThat(testCaseFromParcel.getMethodName()).isEqualTo(methodName);

    // Assertion for the inserted method annotation
    assertThat(testCaseFromParcel.getMethodAnnotations()).isNotEmpty();
    assertThat(testCaseFromParcel.getMethodAnnotations().get(0).getName())
        .isEqualTo("androidx.test.services.events.DummyAnnotation");
    assertThat(testCaseFromParcel.getMethodAnnotations().get(0).getValues().get(0).getFieldName())
        .isEqualTo("foo");
    assertThat(testCaseFromParcel.getMethodAnnotations().get(0).getValues().get(0).getFieldValues())
        .containsExactly("bar");
    assertThat(testCaseFromParcel.getMethodAnnotations().get(0).getValues().get(0).getValueType())
        .isEqualTo("String");

    assertThat(testCaseFromParcel.getClassAnnotations()).isEmpty();
  }
}
