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

import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.applyValuePredicateToSpecificField;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.fieldValueContainsElement;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newAnnotationPresentAnywherePredicate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationPb;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationValuePb;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;
import java.util.Map;

/**
 * Creates a predicate which returns false if a test class or method is annotated with either:
 *
 * <ul>
 *   <li>a com.google.android.apps.common.testing.testrunner.annotations.SdkSuppress annotation with
 *       the device's api level in the versions field.
 *   <li>a androidx.test.filters.SdkSuppress annotation with a minSdkVersion field higher
 *       than the device's api level.
 * </ul>
 */
class ApiLevelFilter implements Function<BrokeredDevice, Predicate<InfoPb>> {
  private static final String API_LEVEL_KEY = "ro.build.version.sdk";
  private static final String GOOGLE_COMMON_TESTING_SDK_SUPPRESS_CLASS =
      "com.google.android.apps.common.testing.testrunner.annotations.SdkSuppress";
  private static final String ANDROIDX_TEST_SDK_SUPPRESS_CLASS =
      "androidx.test.filters.SdkSuppress";
  private static final String ANDROID_SUPPORT_TEST_SDK_SUPPRESS_CLASS =
      "android.support.test.filters.SdkSuppress";

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice device) {
    checkNotNull(device);
    Map<String, String> props = device.getDeviceBootProperties();
    String apiLevelStr = props.get(API_LEVEL_KEY);
    checkNotNull(
        apiLevelStr, "Can't determine api level (key: %s) (props: %s)", API_LEVEL_KEY, props);

    int apiLevel;
    try {
      apiLevel = Integer.parseInt(apiLevelStr);
    } catch (NumberFormatException nfe) {
      throw new IllegalStateException(
          String.format(
              "API Level is not an integer (%s). (Key used: %s, props: %s)",
              apiLevelStr, API_LEVEL_KEY, props));
    }

    return and(
        // doesn't have google common.testing SdkSuppress w/ versions that contains our api level
        not(
            newAnnotationPresentAnywherePredicate(
                GOOGLE_COMMON_TESTING_SDK_SUPPRESS_CLASS,
                applyValuePredicateToSpecificField(
                    "versions", fieldValueContainsElement(apiLevelStr)))),
        // doesn't have android.support.test SdkSuppress w/ minSdkVersion > our api level
        new ATSLSuppressPredicate(apiLevel));
  }

  private static class ATSLSuppressPredicate implements Predicate<InfoPb> {
    private static final String MIN_SDK_VERSION = "minSdkVersion";
    private static final String MAX_SDK_VERSION = "maxSdkVersion";

    private final int apiLevel;

    ATSLSuppressPredicate(int apiLevel) {
      this.apiLevel = apiLevel;
    }

    @Override
    public boolean apply(InfoPb testMethod) {
      return
      // I'd argue that the method annotation supersedes anything the class annotation
      // says, but legacy behaviour says that this is an AND.
      applyToAnno(classAnnotation(testMethod)) && applyToAnno(methodAnnotation(testMethod));
    }

    private boolean applyToAnno(AnnotationPb anno) {
      if (null == anno) {
        // doesn't have the ATSL predicate, accept it.
        return true;
      }

      // Note - since minSdkVersion / maxSdkVersion have default definitions in the
      // attribute's java src, it will _ALWAYS_ be defined.
      int min = Integer.MIN_VALUE;
      int max = Integer.MAX_VALUE;
      for (AnnotationValuePb value : anno.getAnnotationValueList()) {
        if (MIN_SDK_VERSION.equals(value.getFieldName())) {
          min = Integer.parseInt(value.getFieldValue(0));
        } else if (MAX_SDK_VERSION.equals(value.getFieldName())) {
          max = Integer.parseInt(value.getFieldValue(0));
        }
      }

      // has the ATSL predicate, only accept it if we're not in the ATSL
      // (inclusive) range.
      return Range.closed(min, max).contains(apiLevel);
    }

    private AnnotationPb classAnnotation(InfoPb testMethod) {
      for (AnnotationPb anno : testMethod.getMethodAnnotationList()) {
        if (anno.getClassName().equals(ANDROID_SUPPORT_TEST_SDK_SUPPRESS_CLASS)) {
          return anno;
        } else if (anno.getClassName().equals(ANDROIDX_TEST_SDK_SUPPRESS_CLASS)) {
          return anno;
        }
      }
      return null;
    }

    private AnnotationPb methodAnnotation(InfoPb testMethod) {
      for (AnnotationPb anno : testMethod.getClassAnnotationList()) {
        if (anno.getClassName().equals(ANDROID_SUPPORT_TEST_SDK_SUPPRESS_CLASS)) {
          return anno;
        } else if (anno.getClassName().equals(ANDROIDX_TEST_SDK_SUPPRESS_CLASS)) {
          return anno;
        }
      }
      return null;
    }
  }
}
