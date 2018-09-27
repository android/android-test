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
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Collection of Filters that can be used to select/reject tests for execution. */
public enum Filters implements Function<BrokeredDevice, Predicate<InfoPb>> {
  SUPPRESS() {

    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      return new SuppressFilter();
    }
  },
  API_LEVEL() {
    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      return new ApiLevelFilter();
    }
  },
  DEVICE_TYPE() {
    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      return new DeviceTypeFilter();
    }
  },
  SIZE() {
    private final SizeFilter filter = new SizeFilter();

    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      checkState(filter.flagsParsed());
      return filter;
    }

    @Override
    protected void configure(String... args) {
      filter.parse(args);
    }
  },
  TEST_NAME() {
    private final TestNameFilter filter = new TestNameFilter();

    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      checkState(filter.flagsParsed());
      return filter;
    }

    @Override
    protected void configure(String... args) {
      filter.parse(args);
    }
  },
  ANNOTATION_NAME() {
    private final AnnotationNameFilter filter = new AnnotationNameFilter();

    @Override
    protected Function<BrokeredDevice, Predicate<InfoPb>> filterFunction() {
      checkState(filter.flagsParsed());
      return filter;
    }

    @Override
    protected void configure(String... args) {
      filter.parse(args);
    }
  };

  protected void configure(String... args) {}

  private static final Logger logger = Logger.getLogger(Filters.class.getName());
  private static final ImmutableSet<Filters> DEFAULT_FILTERS =
      ImmutableSet.of(SUPPRESS, API_LEVEL, DEVICE_TYPE);

  private static TestFilterSpec testFilterSpec;

  public static final TestFilter getTestFilter(String[] testArgs) {
    initFlags(testArgs);
    if (null == testFilterSpec) {
      TestFilterSpec filterSpec = new TestFilterSpec();
      JCommander jCommander = new JCommander(filterSpec);
      jCommander.setAcceptUnknownOptions(true);
      jCommander.setAllowParameterOverwriting(true);
      jCommander.parse(testArgs);
      testFilterSpec = filterSpec;
    }
    return testFilterSpec.testFilter;
  }

  @VisibleForTesting
  protected static void initFlags(String[] testArgs) {
    for (Filters filter : Filters.values()) {
      filter.configure(testArgs);
    }
  }

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice device) {
    checkNotNull(device);
    return filterFunction().apply(device);
  }

  protected abstract Function<BrokeredDevice, Predicate<InfoPb>> filterFunction();

  /**
   * Takes a group of active or inverted filters and exposes them as a single predicate to be
   * applied to a InfoPb.
   */
  public static class TestFilter {
    private final Set<Filters> active;
    private final Set<Filters> inverted;

    public TestFilter(Set<Filters> active, Set<Filters> inverted) {
      checkNotNull(active);
      checkNotNull(inverted);
      checkArgument(
          Collections.disjoint(active, inverted),
          "Intersection exists between active filters and inverted filters: %s.",
          Sets.intersection(active, inverted));
      this.inverted = ImmutableSet.copyOf(inverted);
      this.active =
          Sets.difference(Sets.union(DEFAULT_FILTERS, ImmutableSet.copyOf(active)), this.inverted);
    }

    Set<Filters> getActive() {
      return ImmutableSet.copyOf(active);
    }

    Set<Filters> getInverted() {
      return ImmutableSet.copyOf(inverted);
    }

    public Predicate<InfoPb> createTestFilterPredicateFor(BrokeredDevice device) {
      checkNotNull(device);
      Predicate<InfoPb> filterPredicate = alwaysTrue();
      for (Filters filter : active) {
        filterPredicate = and(filterPredicate, createPredicateFor(device, filter, false));
      }
      for (Filters filter : inverted) {
        filterPredicate = and(filterPredicate, createPredicateFor(device, filter, true));
      }
      return filterPredicate;
    }

    private Predicate<InfoPb> createPredicateFor(
        final BrokeredDevice device, final Filters filter, final boolean invert) {
      final Predicate<InfoPb> predicate = filter.apply(device);
      return info -> {
        checkNotNull(info);

        boolean result;
        if (invert) {
          result = not(predicate).apply(info);
        } else {
          result = predicate.apply(info);
        }

        if (!result) {
          logger.log(
              Level.INFO,
              String.format(
                  "Filtered: %s.%s#%s by %s %s",
                  info.getTestPackage(),
                  info.getTestClass(),
                  info.getTestMethod(),
                  invert ? "inverted" : "",
                  filter));
        }
        return result;
      };
    }
  }

  static class TestFilterSpec {
    private static final Splitter COMMA_SPLITTER =
        Splitter.on(',').omitEmptyStrings().trimResults();

    private static final String TEST_FILTER_SPEC_FLAG = "test_filter_spec";
    private static final String TEST_FILTER_SPEC_DESC =
        "A list of filters to activate or invert when selecting test cases. Example: "
            + "--test_filter_spec=-api_level,size would cause the tests to run to have to pass "
            + "through the suppression filter, the INVERSE of the api_level filter and the "
            + "size filter. By default the suppression, api_level, and device_type filters "
            + "are always activated however they can be inverted through this flag.";

    @Parameter(
      names = "--" + TEST_FILTER_SPEC_FLAG,
      description = TEST_FILTER_SPEC_DESC,
      converter = TestFilterSpecConverter.class
    )
    public TestFilter testFilter = new TestFilter(Collections.emptySet(), Collections.emptySet());

    protected static TestFilter fromString(String in) {
      Set<Filters> activeFilters = Sets.newHashSet();
      Set<Filters> invertedFilters = Sets.newHashSet();
      for (String spec : COMMA_SPLITTER.split(in)) {
        if (spec.startsWith("-")) {
          spec = spec.substring(1, spec.length());
          invertedFilters.add(Filters.valueOf(spec.toUpperCase()));
        } else {
          if (spec.startsWith("+")) { // undocumented convenience.
            spec = spec.substring(1, spec.length());
          }
          activeFilters.add(Filters.valueOf(spec.toUpperCase()));
        }
      }
      return new TestFilter(activeFilters, invertedFilters);
    }

    protected static String toString(TestFilter testFilter) {
      StringBuilder result = new StringBuilder();
      for (Filters filter : testFilter.active) {
        result.append(String.format("%s,", filter));
      }
      for (Filters filter : testFilter.inverted) {
        result.append(String.format("-%s,", filter));
      }
      int len = result.length();
      if (result.charAt(len - 1) == ',') {
        result.setLength(len - 1);
      }
      return result.toString();
    }
  }
}
