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

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

/** Creates a Predicate which returns true if and only if a test is not suppressed. */
class SuppressFilter implements Function<BrokeredDevice, Predicate<InfoPb>> {

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice unused) {
    return not(
        or(
            AnnotationPredicates.newAnnotationPresentAnywherePredicate(
                "android.test.suitebuilder.annotation.Suppress"),
            AnnotationPredicates.newAnnotationPresentAnywherePredicate(
                "androidx.test.filters.Suppress"),
            AnnotationPredicates.newAnnotationPresentAnywherePredicate(
                "android.support.test.filters.Suppress"),
            AnnotationPredicates.newAnnotationPresentAnywherePredicate("org.junit.Ignore"),
            AnnotationPredicates.newAnnotationPresentAnywherePredicate(
                "com.google.android.apps.common.testing.testrunner.annotations.Ignore")));
  }
}
