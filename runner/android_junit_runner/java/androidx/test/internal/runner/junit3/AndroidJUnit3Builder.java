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

package androidx.test.internal.runner.junit3;

import static androidx.test.internal.util.AndroidRunnerBuilderUtil.hasJUnit3TestMethod;
import static androidx.test.internal.util.AndroidRunnerBuilderUtil.isJUnit3Test;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import junit.framework.TestCase;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

/**
 * A {@link RunnerBuilder} that will build customized runners needed for specialized Android {@link
 * TestCase}s.
 */
public class AndroidJUnit3Builder extends JUnit3Builder {

  private static final String TAG = "AndroidJUnit3Builder";

  /**
   * A special runner, used while scanning to indicate that the class is a JUnit 3 test but is not a
   * valid test because it has no methods.
   */
  public static final Runner NOT_A_VALID_TEST =
      new Runner() {
        @Override
        public Description getDescription() {
          return Description.EMPTY;
        }

        @Override
        public void run(RunNotifier notifier) {}
      };

  private final AndroidRunnerParams androidRunnerParams;
  private final boolean scanningPath;

  /**
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   */
  public AndroidJUnit3Builder(AndroidRunnerParams runnerParams, boolean scanningPath) {
    androidRunnerParams = runnerParams;
    this.scanningPath = scanningPath;
  }

  /**
   * @deprecated Provided temporarily for backwards compatibility. Use {@link
   *     AndroidJUnit3Builder#AndroidJUnit3Builder(AndroidRunnerParams, boolean) instead}.
   */
  @Deprecated
  public AndroidJUnit3Builder(AndroidRunnerParams runnerParams) {
    this(runnerParams, false);
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    try {
      if (isJUnit3Test(testClass)) {
        // If scanning the path then make sure that it has at least one test method before
        // trying to run it.
        if (scanningPath && !hasJUnit3TestMethod(testClass)) {
          // Return a runner to prevent any other RunnerBuilder classes from
          // trying to check this class.
          return NOT_A_VALID_TEST;
        }
        return new JUnit38ClassRunner(new AndroidTestSuite(testClass, androidRunnerParams));
      }
    } catch (Throwable e) {
      // log error message including stack trace before throwing to help with debugging.
      Log.e(TAG, "Error constructing runner", e);
      throw e;
    }
    return null;
  }
}
