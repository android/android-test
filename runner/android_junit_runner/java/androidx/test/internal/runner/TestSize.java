/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.internal.runner;

import androidx.annotation.VisibleForTesting;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.runner.Description;

/**
 * This class represents a test size qualifier which can be used to filter out tests from a test
 * suite.
 *
 * <p>It quietly handles runner size filter annotations and old platform size annotations.
 */
public final class TestSize {

  /** @see androidx.test.filters.SmallTest */
  public static final TestSize SMALL =
      new TestSize(
          "small",
          androidx.test.filters.SmallTest.class,
          "android.test.suitebuilder.annotation.SmallTest",
          200 /* in ms */);

  /** @see androidx.test.filters.MediumTest */
  public static final TestSize MEDIUM =
      new TestSize(
          "medium",
          androidx.test.filters.MediumTest.class,
          "android.test.suitebuilder.annotation.MediumTest",
          1000 /* in ms */);

  /** @see androidx.test.filters.LargeTest */
  public static final TestSize LARGE =
      new TestSize(
          "large",
          androidx.test.filters.LargeTest.class,
          "android.test.suitebuilder.annotation.LargeTest",
          Float.MAX_VALUE /* no threshold */);

  /**
   * A "null object" that is returned in case no test size matches, to avoid a null return value.
   */
  public static final TestSize NONE = new TestSize("", null, null, 0);

  private static final Set<TestSize> ALL_SIZES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(SMALL, MEDIUM, LARGE)));

  private final String sizeQualifierName;
  private final Class<? extends Annotation> platformAnnotationClass;
  private final Class<? extends Annotation> runnerFilterAnnotationClass;

  /**
   * This value the maximum allowed runtime (in ms) for a test included in the test size suite. It
   * is used to make an educated guess at which size bucket a test belongs to.
   */
  private final float testSizeRunTimeThreshold;

  @VisibleForTesting
  public TestSize(
      String sizeQualifierName,
      Class<? extends Annotation> runnerFilterAnnotationClass,
      String legacyPlatformAnnotationClassName,
      float testSizeRuntimeThreshold) {
    this.sizeQualifierName = sizeQualifierName;
    this.platformAnnotationClass = loadPlatformAnnotationClass(legacyPlatformAnnotationClassName);
    this.runnerFilterAnnotationClass = runnerFilterAnnotationClass;
    testSizeRunTimeThreshold = testSizeRuntimeThreshold;
  }

  private static Class<? extends Annotation> loadPlatformAnnotationClass(
      String legacyPlatformAnnotationClassName) {
    if (legacyPlatformAnnotationClassName == null) {
      return null;
    }
    try {
      return (Class<? extends Annotation>) Class.forName(legacyPlatformAnnotationClassName);
    } catch (ClassNotFoundException e) {
      // ignore - not present on boot classpath
      return null;
    }
  }

  /** @return the test size name */
  public String getSizeQualifierName() {
    return sizeQualifierName;
  }

  /**
   * @return true if the test method in the {@link Description} is annotated with the test size
   *     annotation class.
   */
  public boolean testMethodIsAnnotatedWithTestSize(Description description) {
    if (description.getAnnotation(runnerFilterAnnotationClass) != null
        || description.getAnnotation(platformAnnotationClass) != null) {
      // If the test method is annotated with a test size annotation include it
      return true;
    }
    // Otherwise exclude it
    return false;
  }

  /**
   * @return true if the test class in the {@link Description} is annotated with the test size
   *     annotation.
   */
  public boolean testClassIsAnnotatedWithTestSize(Description description) {
    final Class<?> testClass = description.getTestClass();
    if (null == testClass) {
      return false;
    }

    if (hasAnnotation(testClass, runnerFilterAnnotationClass)
        || hasAnnotation(testClass, platformAnnotationClass)) {
      // If the test class is annotated with a test size annotation include it.
      return true;
    }
    return false;
  }

  private static boolean hasAnnotation(
      Class<?> testClass, Class<? extends Annotation> annotationClass) {
    return annotationClass != null && testClass.isAnnotationPresent(annotationClass);
  }

  /** @return the suite run time threshold for a given test size. */
  public float getRunTimeThreshold() {
    return testSizeRunTimeThreshold;
  }

  /**
   * Maps a runtime to a test size.
   *
   * @param testRuntime the runtime of the test
   * @return the test size which was mapped to the runtime
   */
  public static TestSize getTestSizeForRunTime(float testRuntime) {
    if (runTimeSmallerThanThreshold(testRuntime, SMALL.getRunTimeThreshold())) {
      return SMALL;
    } else if (runTimeSmallerThanThreshold(testRuntime, MEDIUM.getRunTimeThreshold())) {
      return MEDIUM;
    }
    return LARGE;
  }

  /**
   * @param annotationClass the test size annotation class
   * @return true if the the test size annotation is valid
   */
  public static boolean isAnyTestSize(Class<? extends Annotation> annotationClass) {
    for (TestSize testSize : ALL_SIZES) {
      if (testSize.getRunnerAnnotation() == annotationClass
          || testSize.getFrameworkAnnotation() == annotationClass) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a test size instance from a test size string. This method will return {@link
   * TestSize#NONE} if the test size is unknown.
   */
  public static TestSize fromString(final String testSize) {
    TestSize testSizeFromString = NONE;
    for (TestSize testSizeValue : ALL_SIZES) {
      if (testSizeValue.getSizeQualifierName().equals(testSize)) {
        testSizeFromString = testSizeValue;
      }
    }
    return testSizeFromString;
  }

  /**
   * Creates a test size instance from a {@link Description}. This method will return {@link
   * TestSize#NONE} if the description does not contain any test size information.
   */
  public static TestSize fromDescription(Description description) {
    TestSize testSize = NONE;
    // Match on method level first
    for (TestSize testMethodSizeValue : ALL_SIZES) {
      if (testMethodSizeValue.testMethodIsAnnotatedWithTestSize(description)) {
        testSize = testMethodSizeValue;
        break;
      }
    }
    // If size annotation not matched on method level look at class level
    if (NONE.equals(testSize)) {
      for (TestSize testClassSizeValue : ALL_SIZES) {
        if (testClassSizeValue.testClassIsAnnotatedWithTestSize(description)) {
          testSize = testClassSizeValue;
          break;
        }
      }
    }
    return testSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestSize testSize = (TestSize) o;

    return sizeQualifierName.equals(testSize.sizeQualifierName);
  }

  @Override
  public int hashCode() {
    return sizeQualifierName.hashCode();
  }

  private static boolean runTimeSmallerThanThreshold(float testRuntime, float runtimeThreshold) {
    return Float.compare(testRuntime, runtimeThreshold) < 0;
  }

  private Class<? extends Annotation> getFrameworkAnnotation() {
    return platformAnnotationClass;
  }

  private Class<? extends Annotation> getRunnerAnnotation() {
    return runnerFilterAnnotationClass;
  }
}
