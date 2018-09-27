/*
 * Copyright (C) 2015 The Android Open Source Project
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
package androidx.test.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Util methods for {@link androidx.test.internal.runner.AndroidRunnerBuilder} */
public class AndroidRunnerBuilderUtil {

  /**
   * Checks if a particular test class is a JUnit3 test
   *
   * @param testClass test class to check
   * @return true if the test class is a JUnit3 test
   */
  public static boolean isJUnit3Test(Class<?> testClass) {
    return TestCase.class.isAssignableFrom(testClass);
  }

  /**
   * Checks if a particular test class is a JUnit3 test suite
   *
   * @param testClass test class to check
   * @return true if the test class is a JUnit3 test suite
   */
  public static boolean isJUnit3TestSuite(Class<?> testClass) {
    return TestSuite.class.isAssignableFrom(testClass);
  }

  /**
   * Checks if a JUnit3 test class has a suite method
   *
   * @param testClass test class to check
   * @return true if the test class has a suite method
   */
  public static boolean hasSuiteMethod(Class<?> testClass) {
    try {
      testClass.getMethod("suite");
    } catch (NoSuchMethodException e) {
      return false;
    }
    return true;
  }

  public static boolean hasJUnit3TestMethod(Class<?> loadedClass) {
    for (Method testMethod : loadedClass.getMethods()) {
      if (isPublicTestMethod(testMethod)) {
        return true;
      }
    }
    return false;
  }

  /** copied from junit.framework.TestSuite */
  private static boolean isPublicTestMethod(Method m) {
    return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
  }

  /** copied from junit.framework.TestSuite */
  private static boolean isTestMethod(Method m) {
    return m.getParameterTypes().length == 0
        && m.getName().startsWith("test")
        && m.getReturnType().equals(Void.TYPE);
  }
}
