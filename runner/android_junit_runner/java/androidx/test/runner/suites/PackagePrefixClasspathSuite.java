/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.runner.suites;

import static androidx.test.internal.runner.ClassPathScanner.getDefaultClasspaths;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.internal.runner.ClassPathScanner;
import androidx.test.internal.runner.ClassPathScanner.InclusivePackageNamesFilter;
import androidx.test.internal.runner.ErrorReportingRunner;
import androidx.test.internal.runner.TestLoader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * An alternative to {@link AndroidClasspathSuite} that ignores classes whose java package is not
 * within the current class's java package.
 *
 * <p>Example usage: * *
 *
 * <pre>
 * package com.example.foo;
 *
 * {@literal @}RunWith(PackagePrefixClasspathSuite.class)
 * public class AllTests {
 * }
 * </pre>
 *
 * <p>This class was intended for large applications using {@link AndroidClasspathSuite} results in
 * LinearAlloc errors (b/36936369) and performance overhead when scanning the entire classpath.
 *
 * <p>This API is currently experimental and subject to change in future releases.
 */
@ExperimentalTestApi
public final class PackagePrefixClasspathSuite extends Suite {

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @hide
   */
  @RestrictTo(Scope.LIBRARY)
  public PackagePrefixClasspathSuite(Class<?> klass, RunnerBuilder builder)
      throws InitializationError {
    super(klass, getRunnersForClasses(klass, builder));
  }

  private static List<Runner> getRunnersForClasses(Class<?> klass, RunnerBuilder builder) {
    try {
      Collection<String> classNames =
          new ClassPathScanner(getDefaultClasspaths(getInstrumentation()))
              .getClassPathEntries(
                  new InclusivePackageNamesFilter(Arrays.asList(klass.getPackage().getName())));
      classNames.remove(klass.getName());
      return TestLoader.Factory.create(null, builder, true).getRunnersFor(classNames);
    } catch (IOException e) {
      return Arrays.asList(
          new ErrorReportingRunner(
              getInstrumentation().getContext().getPackageName(),
              new RuntimeException(
                  "Failed to perform classpath scanning to determine tests to run", e)));
    }
  }
}
