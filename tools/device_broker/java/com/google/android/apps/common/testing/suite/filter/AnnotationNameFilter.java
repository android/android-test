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

import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.PERMISSIVE;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newAnnotationPresentAnywherePredicate;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newClassAnnotationPresentPredicate;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newMethodAnnotationPresentPredicate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.or;

import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a Predicate which returns true if and only if one of the desired annotation class name
 * appears on the test case.
 */
class AnnotationNameFilter extends FlagParsingFilter {
  private static final String ALLOWED_CLASS_NAMES_FLAG = "allowed_class_names";
  private static final String ALLOWED_CLASS_NAMES_DESC =
      "Search for these annotation class names anywhere on a test.";

  @Parameter(names = "--" + ALLOWED_CLASS_NAMES_FLAG, description = ALLOWED_CLASS_NAMES_DESC)
  private List<String> allowedClassNames = new ArrayList<>();

  private static final String ALLOWED_CLASS_NAME_ON_METHODS_FLAG = "allowed_class_name_on_methods";
  private static final String ALLOWED_CLASS_NAME_ON_METHODS_DESC =
      "Search for these annotation class names on a test method.";

  @Parameter(
    names = "--" + ALLOWED_CLASS_NAME_ON_METHODS_FLAG,
    description = ALLOWED_CLASS_NAME_ON_METHODS_DESC
  )
  private List<String> allowedClassNamesOnMethods = new ArrayList<>();

  private static final String ALLOWED_CLASS_NAME_ON_TYPES_FLAG = "allowed_class_name_on_types";
  private static final String ALLOWED_CLASS_NAME_ON_TYPES_DESC =
      "Search for these annotation class names on a test type.";

  @Parameter(
    names = "--" + ALLOWED_CLASS_NAME_ON_TYPES_FLAG,
    description = ALLOWED_CLASS_NAME_ON_TYPES_DESC
  )
  private List<String> allowedClassNamesOnTypes = new ArrayList<>();

  AnnotationNameFilter() {
    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  @VisibleForTesting
  AnnotationNameFilter(
      List<String> classNamesAnywhere,
      List<String> classNamesOnMethods,
      List<String> classNamesOnTypes) {
    this.allowedClassNames = checkNotNull(classNamesAnywhere);
    this.allowedClassNamesOnMethods = checkNotNull(classNamesOnMethods);
    this.allowedClassNamesOnTypes = checkNotNull(classNamesOnTypes);
  }

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice unused) {
    checkState(
        !allowedClassNames.isEmpty()
            || !allowedClassNamesOnMethods.isEmpty()
            || !allowedClassNamesOnTypes.isEmpty(),
        "Filtering on annotation names, but no annotation names specified!");
    List<Predicate<InfoPb>> predicates = Lists.newArrayList();
    for (String className : allowedClassNames) {
      predicates.add(newAnnotationPresentAnywherePredicate(className, PERMISSIVE));
    }
    for (String className : allowedClassNamesOnMethods) {
      predicates.add(newMethodAnnotationPresentPredicate(className, PERMISSIVE));
    }
    for (String className : allowedClassNamesOnTypes) {
      predicates.add(newClassAnnotationPresentPredicate(className, PERMISSIVE));
    }
    return or((Predicate<InfoPb>[]) predicates.toArray(new Predicate[0]));
  }
}
