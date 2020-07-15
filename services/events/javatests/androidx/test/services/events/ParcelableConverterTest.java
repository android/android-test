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

package androidx.test.services.events;

import static androidx.test.services.events.ParcelableConverter.getArrayElements;
import static androidx.test.services.events.ParcelableConverter.getTestCaseFromDescription;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

/** Unit tests for {@link ParcelableConverter}. */
@RunWith(AndroidJUnit4.class)
public class ParcelableConverterTest {

  private static final Named<AnnotationInfo> ANNOTATION = (obj, name) -> name.equals(obj.name);
  private static final Named<AnnotationValue> VALUE = (obj, name) -> name.equals(obj.fieldName);

  private AnnotationInfo ignore;
  private AnnotationInfo dummy;
  private Annotation[] javaAnnotations;

  @Before
  public void before() throws NoSuchMethodException {
    javaAnnotations =
        getClass().getDeclaredMethod("methodWithAnnotations").getDeclaredAnnotations();

    List<AnnotationInfo> result = ParcelableConverter.getAnnotationsFromArray(javaAnnotations);

    assertThat(result).hasSize(2);

    ignore = getElement(result, Ignore.class.getName(), ANNOTATION);
    dummy = getElement(result, DummyAnnotation.class.getName(), ANNOTATION);
  }

  @Test
  public void getTestCaseFromDescription_returnsTestCase() throws TestEventException {
    Description description = Description.createTestDescription("Class", "method", javaAnnotations);
    TestCaseInfo testCase = getTestCaseFromDescription(description);
    assertThat(testCase.className).isEqualTo("Class");
    assertThat(testCase.methodName).isEqualTo("method");
    assertThat(testCase.methodAnnotations.get(0).name)
        .isEqualTo(javaAnnotations[0].annotationType().getName());
  }

  @Test
  public void getTestCaseFromDescription_invalidDescription_throws() {
    assertThrows(
        TestEventException.class, () -> getTestCaseFromDescription(Description.TEST_MECHANISM));
  }

  @Ignore("This method is used as test data")
  @DummyAnnotation(
      foo = "hello world",
      numbers = {1, 2, 3})
  @SuppressWarnings("unused")
  private static void methodWithAnnotations() {}

  @Test
  public void getAnnotationsFromArray_annotationWithStringValue() {
    assertThat(ignore.name).isEqualTo(Ignore.class.getName());
    assertThat(ignore.values.get(0).fieldName).isEqualTo("value");
    assertThat(ignore.values.get(0).valueType).isEqualTo("String");
    assertThat(ignore.values.get(0).fieldValues.get(0))
        .isEqualTo("This method is used as test data");
  }

  @Test
  public void getAnnotationsFromArray_annotationWithArrayValue() {
    assertThat(dummy.name).isEqualTo(DummyAnnotation.class.getName());

    AnnotationValue numbers = getElement(dummy.values, "numbers", VALUE);
    assertThat(numbers.valueType).isEqualTo("int");
    assertThat(numbers.fieldValues).containsExactly("1", "2", "3");

    AnnotationValue foo = getElement(dummy.values, "foo", VALUE);
    assertThat(foo.valueType).isEqualTo("String");
    assertThat(foo.fieldValues).containsExactly("hello world");
  }

  @Test
  public void getArrayElements_null() {
    assertThat(getArrayElements(null)).containsExactly("<null>");
  }

  @Test
  public void getArrayElements_emptyString() {
    assertThat(getArrayElements("")).containsExactly("");
  }

  @Test
  public void getArrayElements_string() {
    assertThat(getArrayElements("hello world")).containsExactly("hello world");
  }

  @Test
  public void getArrayElements_int() {
    assertThat(getArrayElements(1)).containsExactly("1");
  }

  @Test
  public void getArrayElements_intArray() {
    assertThat(getArrayElements(new int[] {1, 2, 3})).containsExactly("1", "2", "3");
  }

  @Test
  public void getArrayElements_intList() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    assertThat(getArrayElements(list)).containsExactly("1", "2", "3");
  }

  private interface Named<T> {
    boolean hasName(T element, String name);
  }

  /** Utility to find an element with a specific name in a list. */
  private static <T> T getElement(List<T> list, String name, Named<T> check) {
    for (T element : list) {
      if (check.hasName(element, name)) {
        return element;
      }
    }
    throw new IllegalStateException(
        "No element found with name " + name + " in list [" + list + "]");
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
