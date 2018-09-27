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
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newClassAnnotationPresentPredicate;
import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newMethodAnnotationPresentPredicate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.broker.HostTestSize;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * Creates a Predicate which controls test size.
 *
 * <p>Specifically if a test method is annotated with a test size annotation and it is not in the
 * user supplied list of test sizes, the predicate will return false.
 *
 * <p>However if the test method is NOT annotated with a size annotation and the test class is, then
 * that size annotation is used and must be in the user supplied list of acceptable test sizes.
 *
 * <p>Additionally if neither the method nor the class is annotated with a test size, than it
 * assumed to be a medium size test.
 *
 * <p>And finally, if Size.UNKNOWN is used, the predicate will return true for tests who are missing
 * size annotations on both their method and class levels.
 *
 * <p>User supplied test sizes are OR'd together.
 */
class SizeFilter extends FlagParsingFilter {

  private static final String ANDROID_TEST_SIZE_FLAG = "android_test_size";
  private static final String ANDROID_TEST_SIZE_DESC = "A list of test sizes that we should run";

  @Parameter(
    names = "--" + ANDROID_TEST_SIZE_FLAG,
    description = ANDROID_TEST_SIZE_DESC,
    converter = SizeFilterConverter.class
  )
  private List<HostTestSize> hostTestSizes = Lists.newArrayList();

  private static final ImmutableMap<HostTestSize, Predicate<InfoPb>> sizeToMethodPredicate =
      ImmutableMap.<HostTestSize, Predicate<InfoPb>>builder()
          .put(
              HostTestSize.ENORMOUS, // ENORMOUS is not an android concept, map to large.
              or(
                  newMethodAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.LargeTest", PERMISSIVE),
                  newMethodAnnotationPresentPredicate(
                      "androidx.test.filters.LargeTest", PERMISSIVE)))
          .put(
              HostTestSize.LARGE,
              or(
                  newMethodAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.LargeTest", PERMISSIVE),
                  newMethodAnnotationPresentPredicate(
                      "androidx.test.filters.LargeTest", PERMISSIVE)))
          .put(
              HostTestSize.MEDIUM,
              or(
                  newMethodAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.MediumTest", PERMISSIVE),
                  newMethodAnnotationPresentPredicate(
                      "androidx.test.filters.MediumTest", PERMISSIVE)))
          .put(
              HostTestSize.SMALL,
              or(
                  newMethodAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.SmallTest", PERMISSIVE),
                  newMethodAnnotationPresentPredicate(
                      "androidx.test.filters.SmallTest", PERMISSIVE)))
          .build();

  private static final ImmutableMap<HostTestSize, Predicate<InfoPb>> sizeToClassPredicate =
      ImmutableMap.<HostTestSize, Predicate<InfoPb>>builder()
          .put(
              HostTestSize.ENORMOUS, // Not an android concept, map it to large.
              or(
                  newClassAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.LargeTest", PERMISSIVE),
                  newClassAnnotationPresentPredicate(
                      "androidx.test.filters.LargeTest", PERMISSIVE)))
          .put(
              HostTestSize.LARGE,
              or(
                  newClassAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.LargeTest", PERMISSIVE),
                  newClassAnnotationPresentPredicate(
                      "androidx.test.filters.LargeTest", PERMISSIVE)))
          .put(
              HostTestSize.MEDIUM,
              or(
                  newClassAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.MediumTest", PERMISSIVE),
                  newClassAnnotationPresentPredicate(
                      "androidx.test.filters.MediumTest", PERMISSIVE)))
          .put(
              HostTestSize.SMALL,
              or(
                  newClassAnnotationPresentPredicate(
                      "android.test.suitebuilder.annotation.SmallTest", PERMISSIVE),
                  newClassAnnotationPresentPredicate(
                      "androidx.test.filters.SmallTest", PERMISSIVE)))
          .build();

  private static final Predicate<InfoPb> NO_SIZE_ON_METHOD_PREDICATE =
      and(
          not(sizeToMethodPredicate.get(HostTestSize.ENORMOUS)),
          not(sizeToMethodPredicate.get(HostTestSize.LARGE)),
          not(sizeToMethodPredicate.get(HostTestSize.MEDIUM)),
          not(sizeToMethodPredicate.get(HostTestSize.SMALL)));

  private static final Predicate<InfoPb> NO_SIZE_ON_CLASS_PREDICATE =
      and(
          not(sizeToClassPredicate.get(HostTestSize.ENORMOUS)),
          not(sizeToClassPredicate.get(HostTestSize.LARGE)),
          not(sizeToClassPredicate.get(HostTestSize.MEDIUM)),
          not(sizeToClassPredicate.get(HostTestSize.SMALL)));

  public SizeFilter() {
    this(Collections.emptyList());
  }

  @VisibleForTesting
  public SizeFilter(List<HostTestSize> requestedTestSizes) {
    this.hostTestSizes = checkNotNull(requestedTestSizes);
  }

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice unused) {
    checkState(
        !hostTestSizes.isEmpty(), "Filtering on test size names, but no size was specified!");
    List<Predicate<InfoPb>> predicates = Lists.newArrayList();

    for (HostTestSize size : hostTestSizes) {
      if (HostTestSize.UNKNOWN == size || HostTestSize.MEDIUM == size) {
        Predicate<InfoPb> pred = and(NO_SIZE_ON_METHOD_PREDICATE, NO_SIZE_ON_CLASS_PREDICATE);
        predicates.add(pred);
      }
      if (HostTestSize.UNKNOWN != size) {
        Predicate<InfoPb> pred =
            or(
                sizeToMethodPredicate.get(size),
                and(NO_SIZE_ON_METHOD_PREDICATE, sizeToClassPredicate.get(size)));
        predicates.add(pred);
      }
    }
    return or((Predicate<InfoPb>[]) predicates.toArray(new Predicate[0]));
  }
}
