/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.runner.junit4;

import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.runner.AndroidJUnit4;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/** A specialized {@link AnnotatedBuilder} that can Android runner specific features */
public class AndroidAnnotatedBuilder extends AnnotatedBuilder {
  private static final String LOG_TAG = "AndroidAnnotatedBuilder";

  private final AndroidRunnerParams androidRunnerParams;

  public AndroidAnnotatedBuilder(RunnerBuilder suiteBuilder, AndroidRunnerParams runnerParams) {
    super(suiteBuilder);
    androidRunnerParams = runnerParams;
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Exception {
    try {
      RunWith annotation = testClass.getAnnotation(RunWith.class);
      // check if its an Android specific runner otherwise default to AnnotatedBuilder
      if (annotation != null && AndroidJUnit4.class.equals(annotation.value())) {
        Class<? extends Runner> runnerClass = annotation.value();
        try {
          // try to build an AndroidJUnit4 runner
          Runner runner = buildAndroidRunner(runnerClass, testClass);
          if (runner != null) {
            return runner;
          }
        } catch (NoSuchMethodException e) {
          // let the super class handle the error for us and throw an InitializationError
          // exception.
          return super.buildRunner(runnerClass, testClass);
        }
      }
    } catch (Throwable e) {
      // log error message including stack trace before throwing to help with debugging.
      Log.e(LOG_TAG, "Error constructing runner", e);
      throw e;
    }
    return super.runnerForClass(testClass);
  }

  @VisibleForTesting
  public Runner buildAndroidRunner(Class<? extends Runner> runnerClass, Class<?> testClass)
      throws Exception {
    return runnerClass
        .getConstructor(Class.class, AndroidRunnerParams.class)
        .newInstance(testClass, androidRunnerParams);
  }
}
