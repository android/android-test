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
import androidx.test.internal.runner.ClassPathScanner;
import androidx.test.internal.runner.ErrorReportingRunner;
import androidx.test.internal.runner.TestLoader;
import androidx.test.runner.suites.AndroidClasspathSuite.RunnerSuite;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Test suite that finds all JUnit3 and JUnit4 test classes in the instrumentation context apk.
 *
 * <p>This is intended to replicate the legacy AndroidJUnitRunner behavior of discovering and
 * running all tests when no '-e class' option is specified. e.g. the following AJUR invocations are
 * equivalent
 *
 * <p>'adb shell am instrument mypkg/androidx.test.runner.AndroidJUnitRunner' 'adb shell am
 * instrument -e class androidx.test.runner.suites.AndroidClasspathSuite
 * mypkg/androidx.test.runner.AndroidJUnitRunner'
 */
@RunWith(RunnerSuite.class)
public final class AndroidClasspathSuite {

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @hide
   */
  @RestrictTo(Scope.LIBRARY)
  public AndroidClasspathSuite() {}

  /**
   * Internal suite class that performs the work of class path scanning.
   *
   * @hide
   */
  @RestrictTo(Scope.LIBRARY)
  public static class RunnerSuite extends Suite {

    /**
     * Only called reflectively. Do not use programmatically.
     *
     * @hide
     */
    @RestrictTo(Scope.LIBRARY)
    public RunnerSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
      super(klass, getRunnersForClasses(builder));
    }

    private static List<Runner> getRunnersForClasses(RunnerBuilder builder) {
      try {
        Collection<String> classNames =
            new ClassPathScanner(getDefaultClasspaths(getInstrumentation())).getClassPathEntries();
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
}
