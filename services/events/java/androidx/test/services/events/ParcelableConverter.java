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

import static java.util.Collections.emptyList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import androidx.test.services.events.internal.StackTrimmer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Utility to convert JUnit {@link Description} and related test data classes to parcelables for
 * sending to a remote service.
 */
public final class ParcelableConverter {

  private static final String TAG = "ParcelableConverter";

  private ParcelableConverter() {}

  /** Converts a JUnit {@link Description} to a {@link TestCaseInfo} parcelable. */
  @NonNull
  public static TestCaseInfo getTestCaseFromDescription(@NonNull Description description)
      throws TestEventException {
    if (!isValidJUnitDescription(description)) {
      throw new TestEventException("Unexpected description instance: " + description);
    }
    List<AnnotationInfo> methodAnnotations =
        getAnnotationsFromCollection(description.getAnnotations());
    List<AnnotationInfo> classAnnotations =
        description.getTestClass() != null
            ? getAnnotationsFromArray(description.getTestClass().getAnnotations())
            : emptyList();
    // Note that getMethodName() may return null as in the case of TestRunStartedEvent.
    return new TestCaseInfo(
        description.getClassName(),
        description.getMethodName() != null ? description.getMethodName() : "",
        methodAnnotations,
        classAnnotations);
  }

  /** Checks if the specified JUnit {@link Description} contains a valid test case name. */
  public static boolean isValidJUnitDescription(@NonNull Description description) {
    return !description.equals(Description.EMPTY)
        && !description.equals(Description.TEST_MECHANISM);
  }

  /**
   * Converts an array of Java {@link Annotation}s to a list of {@link AnnotationInfo} parcelables.
   */
  @NonNull
  public static List<AnnotationInfo> getAnnotationsFromArray(@NonNull Annotation[] annotations) {
    List<AnnotationInfo> result = new ArrayList<>(annotations.length);
    for (Annotation annotation : annotations) {
      result.add(getAnnotation(annotation));
    }
    return result;
  }

  /**
   * Converts a {@link Collection} of Java {@link Annotation}s to a list of {@link AnnotationInfo}
   * parcelables.
   */
  @NonNull
  public static List<AnnotationInfo> getAnnotationsFromCollection(
      @NonNull Collection<Annotation> annotations) {
    List<AnnotationInfo> result = new ArrayList<>(annotations.size());
    for (Annotation annotation : annotations) {
      result.add(getAnnotation(annotation));
    }
    return result;
  }

  /** Converts a JUnit {@link Failure} to a {@link FailureInfo} parcelable. */
  @NonNull
  public static FailureInfo getFailure(@NonNull Failure junitFailure) throws TestEventException {
    return new FailureInfo(
        junitFailure.getMessage(),
        junitFailure.getTestHeader(),
        StackTrimmer.getTrimmedStackTrace(junitFailure),
        getTestCaseFromDescription(junitFailure.getDescription()));
  }

  /**
   * Converts a list of JUnit {@link Failure} objects to a list of {@link FailureInfo} parcelable
   * objects.
   */
  @NonNull
  public static List<FailureInfo> getFailuresFromList(@NonNull List<Failure> failures)
      throws TestEventException {
    List<FailureInfo> result = new ArrayList<>();
    for (Failure failure : failures) {
      result.add(getFailure(failure));
    }
    return result;
  }

  /**
   * Convert a Java {@link Annotation} to a parcelable {@link AnnotationInfo}.
   *
   * @return a parcelable {@link AnnotationInfo}
   */
  @NonNull
  public static AnnotationInfo getAnnotation(@NonNull Annotation javaAnnotation) {
    List<AnnotationValue> annotationValues = new ArrayList<>();

    // Since java annotations fields are represented as methods we iterate on the object's methods.
    for (Method method : javaAnnotation.annotationType().getDeclaredMethods()) {
      AnnotationValue annotationValue = getAnnotationValue(javaAnnotation, method);
      annotationValues.add(annotationValue);
    }

    return new AnnotationInfo(javaAnnotation.annotationType().getName(), annotationValues);
  }

  /**
   * Gets the Java annotation field value and type and returns it as an {@link AnnotationValue}
   * parcelable.
   *
   * @param javaAnnotation the {@link Annotation} to get a field value from
   * @param annotationField the field of a {@link Annotation} to get the type and values from
   * @return an {@link AnnotationValue} - a {@link android.os.Parcelable} class containing the
   *     field's value and type as strings
   */
  @NonNull
  private static AnnotationValue getAnnotationValue(
      @NonNull Annotation javaAnnotation, @NonNull Method annotationField) {
    String annotationFieldName = annotationField.getName();
    List<String> annotationFieldValues;
    String valueType = "NULL";

    try {
      Object fieldValues = annotationField.invoke(javaAnnotation, (Object[]) null);
      valueType = getFieldValuesType(fieldValues);

      // If the annotation value is an array, then convert each one to a String and
      // add it to the list.
      annotationFieldValues = getArrayElements(fieldValues);
    } catch (Exception e) {
      Log.e(
          TAG,
          "Unable to get annotation values for field '"
              + annotationFieldName
              + "': ["
              + javaAnnotation
              + "]",
          e);
      annotationFieldValues = new ArrayList<>();
    }
    return new AnnotationValue(annotationFieldName, annotationFieldValues, valueType);
  }

  @NonNull
  private static String getFieldValuesType(Object fieldValues) {
    // Removes "[" and "]" from the valueType. E.g String[] -> String
    return fieldValues.getClass().getSimpleName().replace("[", "").replace("]", "");
  }

  /** Tries to convert an object's value(s) to a List of Strings. */
  @NonNull
  static List<String> getArrayElements(@Nullable Object obj) {
    List<String> result = new ArrayList<>();
    if (obj == null) {
      result.add("<null>");
    } else if (obj.getClass().isArray()) {
      for (int n = 0; n < Array.getLength(obj); n++) {
        result.add(Array.get(obj, n).toString());
      }
    } else if (obj instanceof Iterable<?>) {
      for (Object element : ((Iterable<?>) obj)) {
        result.add(element.toString());
      }
    } else {
      result.add(obj.toString());
    }
    return result;
  }
}
