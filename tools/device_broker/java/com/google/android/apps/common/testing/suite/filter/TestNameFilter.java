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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.or;

import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A filter that creates a predicate which selects tests to run based on their name.
 *
 * <p>Users can specify a list of classes, packages, or test methods they want the predicate to
 * select.
 *
 * <p>These choices are OR'd together to construct final predicate.
 *
 * <p>NOTE: the android convention of shorting class names with a '.' (eg: .FooTest - implying
 * $APK_PACKAGE_NAME.FooTest) is not supported.
 */
class TestNameFilter extends FlagParsingFilter {

  private static final String TEST_CLASS_NAMES_FLAG = "test_class_names";
  private static final String TEST_CLASS_NAMES_DESC =
      "The fully qualified class names to run. --test_filter_spec must include "
          + "TEST_NAME for this to have effect";

  @Parameter(names = "--" + TEST_CLASS_NAMES_FLAG, description = TEST_CLASS_NAMES_DESC)
  private List<String> testClassNames = new ArrayList<>();

  private static final String TEST_METHOD_SHORT_NAMES_FLAG = "test_method_short_names";
  private static final String TEST_METHOD_SHORT_NAMES_DESC =
      "Run tests with the given method names (eg: testBar). --test_filter_spec must"
          + " include TEST_NAME for this to have effect";

  @Parameter(
    names = "--" + TEST_METHOD_SHORT_NAMES_FLAG,
    description = TEST_METHOD_SHORT_NAMES_DESC
  )
  private List<String> testMethodShortNames = new ArrayList<>();

  private static final String TEST_METHOD_FULL_NAMES_FLAG = "test_method_full_names";
  private static final String TEST_METHOD_FULL_NAMES_DESC =
      "Run tests with the given method names "
          + "(eg: com.google.Android.FooTest#testBar). --test_filter_spec must include "
          + "TEST_NAME for this to have effect";

  @Parameter(names = "--" + TEST_METHOD_FULL_NAMES_FLAG, description = TEST_METHOD_FULL_NAMES_DESC)
  private List<String> testMethodFullNames = new ArrayList<>();

  private static final String TEST_PACKAGE_NAMES_FLAG = "test_package_names";
  private static final String TEST_PACKAGE_NAMES_DESC =
      "Run tests in _OR_ beneath these _Java_ (not apk) package names. "
          + "--test_filter_spec must include TEST_NAME for this to have effect";

  @Parameter(names = "--" + TEST_PACKAGE_NAMES_FLAG, description = TEST_PACKAGE_NAMES_DESC)
  private List<String> testPackageNames = new ArrayList<>();

  TestNameFilter(
      List<String> classNames,
      List<String> shortMethods,
      List<String> fullMethods,
      List<String> packageNames) {
    this.testClassNames = checkNotNull(classNames);
    this.testMethodShortNames = checkNotNull(shortMethods);
    this.testMethodFullNames = checkNotNull(fullMethods);
    this.testPackageNames = checkNotNull(packageNames);
  }

  TestNameFilter() {
    this(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());
  }

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice unused) {
    checkState(
        !testClassNames.isEmpty()
            || !testMethodShortNames.isEmpty()
            || !testMethodFullNames.isEmpty()
            || !testPackageNames.isEmpty(),
        "Filtering on test names, but no names were specified!");

    return or(
        classNamesPredicate(),
        shortMethodsPredicate(),
        fullMethodsPredicate(),
        packageNamesPredicate());
  }

  private Predicate<InfoPb> classNamesPredicate() {
    return info -> {
      String fullyQuantifiedClassName =
          String.format("%s.%s", info.getTestPackage(), info.getTestClass());
      for (String className : testClassNames) {
        if (className.equals(fullyQuantifiedClassName)) {
          return true;
        }
      }
      return false;
    };
  }

  private Predicate<InfoPb> shortMethodsPredicate() {
    return info -> {
      for (String methodName : testMethodShortNames) {
        if (methodName.equals(info.getTestMethod())) {
          return true;
        }
      }
      return false;
    };
  }

  private Predicate<InfoPb> fullMethodsPredicate() {
    return info -> {
      String fullTestName =
          String.format(
              "%s.%s#%s", info.getTestPackage(), info.getTestClass(), info.getTestMethod());
      for (String fullName : testMethodFullNames) {
        if (fullName.equals(fullTestName)) {
          return true;
        }
      }
      return false;
    };
  }

  private Predicate<InfoPb> packageNamesPredicate() {
    return info -> {
      for (String packageName : testPackageNames) {
        if (info.getTestPackage().equals(packageName)) {
          return true;
        }
        if (info.getTestPackage().startsWith(packageName)
            && info.getTestPackage().charAt(packageName.length()) == '.') {
          return true;
        }
      }
      return false;
    };
  }
}
