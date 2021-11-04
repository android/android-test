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
import java.lang.reflect.Modifier;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * A class for loading JUnit3 and JUnit4 test classes given a set of potential class names.
 *
 * <p>This is a lenient loader intended for class path scanning cases, where if a class cannot be
 * loaded or is not a test it will be ignored.
 */
class ScanningTestLoader extends TestLoader {

  private static final String LOG_TAG = "ScanningTestLoader";

  private final ClassLoader classLoader;
  private final RunnerBuilder runnerBuilder;

  ScanningTestLoader(ClassLoader classLoader, RunnerBuilder runnerBuilder) {
    this.classLoader = classLoader;
    this.runnerBuilder = runnerBuilder;
  }

  @Override
  protected Runner doCreateRunner(String className) {
    try {
      Class<?> loadedClass = Class.forName(className, false, classLoader);
      if (Modifier.isAbstract(loadedClass.getModifiers())) {
        logDebug("Skipping abstract class %s: not a test", loadedClass.getName());
        return null;
      }
      Runner runner = runnerBuilder.runnerForClass(loadedClass);
      if (runner instanceof EmptyTestRunner) {
        logDebug("Skipping class %s: class with no test methods", loadedClass.getName());
        return null;
      }
      return runner;
    } catch (Throwable e) {
      Log.w(LOG_TAG, String.format("Could not load class: %s", className), e);
      return null;
    }
  }

  /**
   * Utility method for logging debug messages. Only actually logs a message if LOG_TAG is marked as
   * loggable to limit log spam during normal use.
   */
  private static void logDebug(String msg, Object... objects) {
    if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
      Log.d(LOG_TAG, String.format(msg, objects));
    }
  }
}
