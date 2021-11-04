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

package androidx.test.internal.runner;

import android.util.Log;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * A class for loading JUnit3 and JUnit4 test classes given a set of class names.
 *
 * <p>This variant of TestLoader will strictly report any errors loading the class.
 */
class DirectTestLoader extends TestLoader {

  private static final String LOG_TAG = "DirectTestLoader";

  private final ClassLoader classLoader;
  private final RunnerBuilder runnerBuilder;

  DirectTestLoader(ClassLoader classLoader, RunnerBuilder runnerBuilder) {
    this.classLoader = classLoader;
    this.runnerBuilder = runnerBuilder;
  }

  @Override
  protected Runner doCreateRunner(String className) {
    // this method should always return a non-null runner.
    try {
      Class<?> loadedClass = Class.forName(className, false, classLoader);
      return runnerBuilder.safeRunnerForClass(loadedClass);
      // Can get NoClassDefFoundError on Android L when a class extends a non-existent class.
    } catch (ClassNotFoundException | LinkageError e) {
      String msg = String.format("Failed loading specified test class '%s'", className);
      Log.e(LOG_TAG, msg, e);
      return new ErrorReportingRunner(className, new RuntimeException(msg, e));
    }
  }
}
