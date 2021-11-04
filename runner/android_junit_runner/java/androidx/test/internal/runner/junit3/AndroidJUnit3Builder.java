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
import androidx.test.internal.runner.EmptyTestRunner;
import androidx.test.internal.util.AndroidRunnerParams;
import junit.framework.TestCase;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * A {@link RunnerBuilder} that will build customized runners needed for specialized Android {@link
 * TestCase}s.
 */
public class AndroidJUnit3Builder extends JUnit3Builder {

  private static final String TAG = "AndroidJUnit3Builder";


  private final AndroidRunnerParams androidRunnerParams;

  /** @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters */
  public AndroidJUnit3Builder(AndroidRunnerParams runnerParams) {
    androidRunnerParams = runnerParams;
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    try {
      if (isJUnit3Test(testClass)) {
        if (!hasJUnit3TestMethod(testClass)) {
          // ideally we would just let JUnit38ClassRunner handle this case, but for historical
          // reasons there was special handling when classpath scanning for this case
          return new EmptyTestRunner(testClass);
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
