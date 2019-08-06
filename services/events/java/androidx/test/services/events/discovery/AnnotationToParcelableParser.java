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
package androidx.test.services.events.discovery;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser to parse {@link java.lang.annotation.Annotation} to a parcelable {@link Annotation} class.
 */
public class AnnotationToParcelableParser {

  private static final String TAG = "AnnotationToParcelableParser";

  private java.lang.annotation.Annotation javaAnnotation;

  void setJavaAnnotation(java.lang.annotation.Annotation javaAnnotation) {
    this.javaAnnotation = javaAnnotation;
  }

  /**
   * Gets the annotation field value and type and returns it as an {@link AnnotationValue}.
   *
   * @param annotationField the field of an [java.lang.Annotation] to get the type and values from.
   * @return an {@link AnnotationValue} - Parcelable data class with annotation information.
   */
  private AnnotationValue getAnnotationValueAndType(Method annotationField) {

    String annotationFieldName = annotationField.getName();
    List<String> annotationFieldValues = new ArrayList<>();
    String valueType = "NULL";

    try {
      Object fieldValues = annotationField.invoke(javaAnnotation, (Object[]) null);

      // Removes "[" and "]" from the valueType. E.g String[] -> String
      valueType = fieldValues.getClass().getSimpleName().replaceAll("\\[", "").replace("\\]", "");

      // If the annotation value is an array, then convert each one to a [String] and
      // add it to the list.
      if (fieldValues.getClass().isArray()) {
        for (Object o : (Object[]) fieldValues) {
          annotationFieldValues.add(o.toString());
        }
      } else {
        annotationFieldValues.add(fieldValues.toString());
      }

    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      Log.e(TAG, "Unable to get annotation values.");
    }

    return new AnnotationValue(annotationFieldName, annotationFieldValues, valueType);
  }

  /**
   * Parse the javaAnnotation {@link java.lang.annotation.Annotation} to {@link Annotation}.
   *
   * @return a parcelable [Annotation].
   */
  public Annotation parse() {

    List<AnnotationValue> annotationValues = new ArrayList<>();

    // Since java annotations fields are represented as methods we iterate on the object's methods.
    for (Method method : javaAnnotation.annotationType().getDeclaredMethods()) {
      AnnotationValue annotationValue = getAnnotationValueAndType(method);
      annotationValues.add(annotationValue);
    }

    return new Annotation(javaAnnotation.annotationType().getCanonicalName(), annotationValues);
  }
}
