/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.apps.common.testing.suite.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.or;

import com.google.android.apps.common.testing.proto.TestInfo.AnnotationPb;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationValuePb;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Set;

/** Helper functions for creating predicates on annotations in InfoPbs. */
public final class AnnotationPredicates {

  private AnnotationPredicates() {}

  static final Predicate<List<AnnotationValuePb>> PERMISSIVE = alwaysTrue();

  static Predicate<InfoPb> newMethodAnnotationPresentPredicate(
      String annotationClassName, Predicate<List<AnnotationValuePb>> valuePredicate) {
    return new AnnotationPresent(ElementType.METHOD, annotationClassName, valuePredicate);
  }

  static Predicate<InfoPb> newClassAnnotationPresentPredicate(
      String annotationClassName, Predicate<List<AnnotationValuePb>> valuePredicate) {
    return new AnnotationPresent(ElementType.TYPE, annotationClassName, valuePredicate);
  }

  static Predicate<InfoPb> newAnnotationPresentAnywherePredicate(
      String annotationClassName, Predicate<List<AnnotationValuePb>> valuePredicate) {
    return or(
        newMethodAnnotationPresentPredicate(annotationClassName, valuePredicate),
        newClassAnnotationPresentPredicate(annotationClassName, valuePredicate));
  }

  public static Predicate<InfoPb> newAnnotationPresentAnywherePredicate(
      String annotationClassName) {
    return newAnnotationPresentAnywherePredicate(annotationClassName, PERMISSIVE);
  }

  /**
   * Takes a predicate that was written to handle a single AnnotationValuePb with a specific field
   * name and converts it so that it can be applied to a list of annovaluepbs and will only be
   * called on its specific field.
   *
   * @param fieldName the field name the predicate should apply against.
   * @param valuePredicate the predicate to apply to the AnnotationValuePb with the above fileld
   *     name.
   */
  static Predicate<List<AnnotationValuePb>> applyValuePredicateToSpecificField(
      final String fieldName, final Predicate<AnnotationValuePb> valuePredicate) {
    checkNotNull(fieldName);
    checkNotNull(valuePredicate);
    return annoValues -> {
      for (AnnotationValuePb annoValue : annoValues) {
        if (annoValue.getFieldName().equals(fieldName)) {
          return valuePredicate.apply(annoValue);
        }
      }
      throw new IllegalStateException(
          String.format("Expected to find fieldname: %s in %s.", fieldName, annoValues));
    };
  }

  /**
   * Creates a predicate that returns true if the annotation's value is equal to the expected value.
   *
   * <p>If the field is an array field, the predicate will throw an exception.
   *
   * <p>Example: @FooBar(blah=42)
   *
   * <p>applyValuePredicateToSpecificField("blah", fieldValueEquals("42"))
   *
   * <p>Would return true.
   *
   * @param expectedValue the desired value.
   */
  static Predicate<AnnotationValuePb> fieldValueEquals(final String expectedValue) {
    checkNotNull(expectedValue);

    return new Predicate<AnnotationValuePb>() {
      @Override
      public boolean apply(AnnotationValuePb annoValue) {
        checkState(!annoValue.getIsArray(), "This predicate is not appropriate for arrays!");
        return expectedValue.equals(annoValue.getFieldValue(0));
      }
    };
  }

  /**
   * Creates a predicate that returns true if the annotation's value as an int is greater than the
   * given value.
   *
   * <p>If the field is an array field, the predicate will throw an exception.
   *
   * <p>Example: @FooBar(blah=42)
   *
   * <p>applyValuePredicateToSpecificField("blah", fieldValueGreaterThan(41))
   *
   * <p>would return true.
   *
   * @param value the desired value.
   */
  static Predicate<AnnotationValuePb> fieldIntValueGreaterThan(final int value) {
    return new Predicate<AnnotationValuePb>() {
      @Override
      public boolean apply(AnnotationValuePb annoValue) {
        checkState(!annoValue.getIsArray(), "This predicate is not appropriate for arrays!");
        String fieldValue = annoValue.getFieldValue(0);
        checkState(fieldValue != null, "Expected field value.");
        try {
          int fieldIntValue = Integer.parseInt(fieldValue);
          return fieldIntValue > value;
        } catch (NumberFormatException e) {
          throw new IllegalStateException(
              String.format("Expected integer for field value: %s.", fieldValue));
        }
      }
    };
  }

  /**
   * Creates a predicate that returns true if the annotation's value field contains a particular
   * value.
   *
   * <p>The predicate will throw an exception if the field is not an array type.
   *
   * <p>Example: @FooBar(blah=[21,11,23, 42])
   *
   * <p>applyValuePredicateToSpecificField("blah", fieldValueContainsElement("42"))
   *
   * <p>Would return true.
   *
   * @param expectedValue the desired value.
   */
  static Predicate<AnnotationValuePb> fieldValueContainsElement(final String expectedValue) {
    checkNotNull(expectedValue);

    return new Predicate<AnnotationValuePb>() {
      @Override
      public boolean apply(AnnotationValuePb annoValue) {
        checkState(annoValue.getIsArray(), "This predicate only appropriate for arrays!");
        return annoValue.getFieldValueList().contains(expectedValue);
      }
    };
  }

  private static class AnnotationPresent implements Predicate<InfoPb> {
    private static final Set<ElementType> VALID_LOCATIONS =
        Sets.newHashSet(ElementType.METHOD, ElementType.TYPE);

    private final ElementType checkLocation;
    private final String className;
    private final Predicate<List<AnnotationValuePb>> valuePredicate;

    AnnotationPresent(
        ElementType checkLocation,
        String className,
        Predicate<List<AnnotationValuePb>> valuePredicate) {
      checkArgument(
          VALID_LOCATIONS.contains(checkLocation),
          "%s is not valid - must be %s",
          checkLocation,
          VALID_LOCATIONS);
      this.checkLocation = checkLocation;
      this.className = checkNotNull(className);
      this.valuePredicate = checkNotNull(valuePredicate);
    }

    @Override
    public boolean apply(InfoPb info) {
      if (ElementType.METHOD == checkLocation) {
        return apply(info.getMethodAnnotationList());
      } else {
        return apply(info.getClassAnnotationList());
      }
    }

    private boolean apply(List<AnnotationPb> annos) {
      for (AnnotationPb anno : annos) {
        if (anno.getClassName().equals(className)) {
          if (valuePredicate.apply(anno.getAnnotationValueList())) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
