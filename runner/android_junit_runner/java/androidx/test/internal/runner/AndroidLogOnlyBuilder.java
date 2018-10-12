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

import static androidx.test.internal.util.AndroidRunnerBuilderUtil.hasJUnit3TestMethod;
import static androidx.test.internal.util.AndroidRunnerBuilderUtil.hasSuiteMethod;
import static androidx.test.internal.util.AndroidRunnerBuilderUtil.isJUnit3Test;
import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.test.internal.runner.junit3.JUnit38ClassRunner;
import androidx.test.internal.runner.junit3.NonExecutingTestSuite;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * Runner builder that only returns non-executing Runner instances to run tests in dry-run mode.
 *
 * @see androidx.test.internal.runner.RunnerArgs.Builder#ARGUMENT_LOG_ONLY
 */
class AndroidLogOnlyBuilder extends RunnerBuilder {

  private final AndroidRunnerBuilder builder;

  private final AndroidRunnerParams runnerParams;

  private final boolean scanningPath;

  // The number of Runners created
  private int runnerCount = 0;

  /**
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   */
  AndroidLogOnlyBuilder(
      AndroidRunnerParams runnerParams,
      boolean scanningPath,
      List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
    this.runnerParams = checkNotNull(runnerParams, "runnerParams cannot be null!");
    this.scanningPath = scanningPath;

    // Create a builder for creating the executable Runner instances to wrap. Pass in this
    // builder as the suiteBuilder so that this will be called to create Runners for nested
    // classes, e.g. in Suite or Enclosed.
    builder =
        new AndroidRunnerBuilder(this, runnerParams, scanningPath, customRunnerBuilderClasses);
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    // Increment the number of runners created.
    ++runnerCount;

    // Build non executing runners for JUnit3 test classes
    if (isJUnit3Test(testClass)) {
      // If scanning the path then make sure that it has at least one test method before
      // trying to run it.
      if (scanningPath && !hasJUnit3TestMethod(testClass)) {
        return null;
      }

      return new JUnit38ClassRunner(new NonExecutingTestSuite(testClass));
    } else if (hasSuiteMethod(testClass)) {
      if (runnerParams.isIgnoreSuiteMethods()) {
        return null;
      }

      Test test = SuiteMethod.testFromSuiteMethod(testClass);
      if (!(test instanceof TestSuite)) {
        // this should not be possible
        throw new IllegalArgumentException(
            testClass.getName() + "#suite() did not return a TestSuite");
      }
      return new JUnit38ClassRunner(new NonExecutingTestSuite((TestSuite) test));
    } else {
      // Reset the count of the number of Runners created for the supplied testClass. Save
      // away the number created for the parent testClass.
      int oldRunnerCount = runnerCount;
      Runner runner = builder.runnerForClass(testClass);
      if (null == runner) {
        // If the runner could not be created then do not wrap it.
        return null;
      } else if (runner instanceof ErrorReportingRunner) {
        // Preserve behavior where a failure during construction results in an error,
        // even while in logOnly mode, by simply returning the runner rather than
        // wrapping it.
        return runner;
      } else if (runnerCount > oldRunnerCount) {
        // If constructing the testClass caused us to reenter here to build Runner
        // instances, e.g. for Suite or Enclosed, then this must not wrap runner in a
        // NonExecutingRunner as that would cause problems if any of the nested classes
        // were JUnit 3 ones.
        return runner;
      } else {
        return new NonExecutingRunner(runner);
      }
    }
  }
}
