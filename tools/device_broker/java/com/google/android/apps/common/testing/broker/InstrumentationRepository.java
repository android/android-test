/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Provider;

/**
 * Used by {@link AdbController} to detect the package(s) containing the JUnit test runner and the
 * test cases. The first supported instrumentation Android package found will be used.
 *
 * <p>Usually the test runner and the test cases exist in the same package, but it is also possible
 * to have the test runner and tests in separate APKs when the {@code
 * --bootstrap_instrumentation_package} flag is used to specify which test runner to use. In such a
 * case, it may be useful to also specify the {@code --additional_test_packages} flag.
 *
 * <p>If the {@code --additional_test_packages} flag is used to to specify additional test packages,
 * they will also be scanned for tests. However, if this flag is empty, we will try to automatically
 * detect the test package (the first package found that contains any supported instrumentation
 * other than the specified bootstrap package).
 */
class InstrumentationRepository {
  /**
   * Default instrumentations to skip when searching for the test package in {@link
   * #getTestInstrumentation()}. Any values provided in the {@code ignoreTestPackages}
   * instrumentation flag will be added to this list.
   */
  private static final ImmutableList<String> IGNORE_INSTRUMENTATION_PACKAGES =
      ImmutableList.of("com.android.emulator.smoketests");

  private static final Logger logger = Logger.getLogger(InstrumentationRepository.class.getName());

  private final Provider<List<Instrumentation>> instrumentationsProvider;
  private final List<String> additionalTestPackages;
  private final String bootstrapInstrumentationPackage;
  private final List<String> ignoreTestPackages;

  private Instrumentation testRunner;
  private String defaultTestPackage;

  private InstrumentationRepository(Builder builder) {
    this.instrumentationsProvider = builder.instrumentationsProvider;
    this.additionalTestPackages = builder.additionalTestPackages;
    this.ignoreTestPackages = builder.ignoreTestPackages;
    this.bootstrapInstrumentationPackage = builder.bootstrapInstrumentationPackage;
  }

  /** @return Instrumentation containing the test runner */
  public Instrumentation getTestInstrumentation() {
    findInstrumentations();
    return testRunner;
  }

  /** @return The package name(s) containing the tests. */
  public List<String> getAdditionalTestPackages() {
    if (additionalTestPackages.size() > 0
        || Strings.isNullOrEmpty(bootstrapInstrumentationPackage)) {
      return additionalTestPackages;
    }
    // If bootstrapInstrumentationPackage is used, but no additionalTestPackages were specified,
    // we have to automatically detect the default test package name.
    findInstrumentations();
    if (!Strings.isNullOrEmpty(defaultTestPackage)) {
      return ImmutableList.of(defaultTestPackage);
    }
    return ImmutableList.of();
  }

  private void findInstrumentations() {
    if (testRunner != null) {
      return;
    }

    List<Instrumentation> availableInstrumentations = instrumentationsProvider.get();

    Set<String> ignoredPackages = new HashSet<>(ignoreTestPackages);
    ignoredPackages.addAll(IGNORE_INSTRUMENTATION_PACKAGES);

    // Don't ignore the bootstrap package when looking for the test runner.
    // (We'd only want to ignore it when looking for tests in TestInfoRepository.)
    if (!Strings.isNullOrEmpty(bootstrapInstrumentationPackage)) {
      ignoredPackages.remove(bootstrapInstrumentationPackage);
    }

    // Filter the list to only contain supported instrumentations.
    List<Instrumentation> filteredInstrumentations = Lists.newArrayList();
    for (Instrumentation instrumentation : availableInstrumentations) {
      if (ignoredPackages.contains(instrumentation.getAndroidPackage())) {
        logger.info(
            String.format(
                "Ignoring instrumentation class: %s/%s",
                instrumentation.getAndroidPackage(), instrumentation.getInstrumentationClass()));
      } else if (AdbController.SUPPORTED_INSTRUMENTATION_NAMES.contains(
              instrumentation.getFullInstrumentationClass())
          ||
          // TODO(b/150524968): remove support for custom runner classes
          AdbController.SUPPORTED_INSTRUMENTATION_NAMES.contains(
              instrumentation.getInstrumentationClass())) {
        filteredInstrumentations.add(instrumentation);

        // Assume the first supported non-bootstrap instrumentation found also contains the tests.
        if (Strings.isNullOrEmpty(defaultTestPackage)
            && !instrumentation.getAndroidPackage().equals(bootstrapInstrumentationPackage)) {
          defaultTestPackage = instrumentation.getAndroidPackage();
          logger.info("Found test package: " + defaultTestPackage);
        }
      }
    }

    if (!Strings.isNullOrEmpty(bootstrapInstrumentationPackage)) {
      testRunner = getBootstrapInstrumentation(availableInstrumentations, filteredInstrumentations);
    } else {
      testRunner = getFirstInstrumentation(filteredInstrumentations);
    }
  }

  private Instrumentation getFirstInstrumentation(List<Instrumentation> filteredInstrumentations) {
    if (filteredInstrumentations.isEmpty()) {
      throw new RuntimeException(
          "Instrumentation package not found: " + AdbController.SUPPORTED_INSTRUMENTATION_NAMES);
    }

    if (filteredInstrumentations.size() > 1) {
      logger.warning(
          "Multiple instrumentations found: " + Joiner.on(", ").join(filteredInstrumentations));
    }

    Instrumentation result = filteredInstrumentations.get(0);
    logger.info("Using " + result.getFullName());
    return result;
  }

  /**
   * By default the package containing the tests is also the package containing the instrumentation
   * class we need to use, but in some cases a test target may specify a different instrumentation
   * class to "bootstrap" the environment, e.g. to set up a custom class loader.
   *
   * <p>The {@code bootstrapInstrumentationPackage} flag value can either be just the Android
   * package name to search for a test runner, or the full Android package and class name separated
   * with "/". When the {@code bootstrapInstrumentationPackage} flag is specified, you will probably
   * also want to specify the {@code additionalTestPackages} flag to ensure that the test cases can
   * be found by {@link TestInfoRepository#listTests(Instrumentation, List)}.
   *
   * @param availableInstrumentations All instrumentations found
   * @param filteredInstrumentations Supported instrumentations
   * @return the Instrumentation containing the test runner
   */
  private Instrumentation getBootstrapInstrumentation(
      List<Instrumentation> availableInstrumentations,
      List<Instrumentation> filteredInstrumentations) {
    Instrumentation result = null;
    if (bootstrapInstrumentationPackage.contains("/")) {
      // The flag specified the full package and class names so use both for comparison.
      // Since the user was specific with the test runner name, scan all available
      // instrumentations.
      for (Instrumentation instrumentation : availableInstrumentations) {
        if (bootstrapInstrumentationPackage.equals(instrumentation.getFullName())) {
          result = instrumentation;
          break;
        }
      }
    } else {
      // The flag only specified the package name, so only compare the package name part.
      for (Instrumentation instrumentation : filteredInstrumentations) {
        if (bootstrapInstrumentationPackage.equals(instrumentation.getAndroidPackage())) {
          result = instrumentation;
          break;
        }
      }
    }
    if (result != null) {
      logger.info("Using bootstrap instrumentation class: " + result.getFullName());
      return result;
    }
    throw new RuntimeException(
        "Couldn't find instrumentation class in package: " + bootstrapInstrumentationPackage);
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Provider<List<Instrumentation>> instrumentationsProvider =
        Providers.of(Collections.emptyList());
    private List<String> additionalTestPackages = Collections.emptyList();
    private List<String> ignoreTestPackages = Collections.emptyList();
    private String bootstrapInstrumentationPackage;

    Builder() {}

    Builder withInstrumentationsProvider(Provider<List<Instrumentation>> instrumentationsProvider) {
      this.instrumentationsProvider = instrumentationsProvider;
      return this;
    }

    Builder withAdditionalTestPackages(List<String> additionalTestPackages) {
      this.additionalTestPackages = additionalTestPackages;
      return this;
    }

    Builder withIgnoreTestPackages(List<String> ignoreTestPackages) {
      this.ignoreTestPackages = ignoreTestPackages;
      return this;
    }

    Builder withBootstrapInstrumentationPackage(String bootstrapInstrumentationPackage) {
      this.bootstrapInstrumentationPackage = bootstrapInstrumentationPackage;
      return this;
    }

    InstrumentationRepository build() {
      return new InstrumentationRepository(this);
    }
  }
}
