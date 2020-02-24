/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.util.Log;
import androidx.test.internal.runner.junit3.AndroidJUnit3Builder;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

/** A class for loading JUnit3 and JUnit4 test classes given a set of potential class names. */
class TestLoader {

  private static final String LOG_TAG = "TestLoader";

  private final ClassLoader classLoader;
  private final RunnerBuilder runnerBuilder;

  private final Map<String, Runner> runnersMap = new LinkedHashMap<>();

  static TestLoader testLoader(
      ClassLoader classLoader, RunnerBuilder runnerBuilder, boolean scanningPath) {
    // If scanning then wrap the supplied RunnerBuilder with one that will ignore abstract
    // classes.
    if (scanningPath) {
      runnerBuilder = new ScanningRunnerBuilder(runnerBuilder);
    }

    if (null == classLoader) {
      classLoader = TestLoader.class.getClassLoader();
    }

    return new TestLoader(classLoader, runnerBuilder);
  }

  private TestLoader(ClassLoader classLoader, RunnerBuilder runnerBuilder) {
    this.classLoader = classLoader;
    this.runnerBuilder = runnerBuilder;
  }

  private void doCreateRunner(String className, boolean isScanningPath) {
    if (runnersMap.containsKey(className)) {
      // Class with the same name was already loaded, return
      return;
    }

    Runner runner;
    try {
      Class<?> loadedClass = Class.forName(className, false, classLoader);
      runner = runnerBuilder.safeRunnerForClass(loadedClass);
      if (null == runner) {
        logDebug(String.format("Skipping class %s: not a test", loadedClass.getName()));
      } else if (runner == AndroidJUnit3Builder.NOT_A_VALID_TEST) {
        logDebug(String.format("Skipping class %s: not a valid test", loadedClass.getName()));
        runner = null;
      }
      // Can get NoClassDefFoundError on Android L when a class extends a non-existent class.
    } catch (ClassNotFoundException | LinkageError e) {
      String errMsg = String.format("Could not find class: %s", className);
      Log.e(LOG_TAG, errMsg);
      Description description = Description.createSuiteDescription(className);
      Failure failure = new Failure(description, e);
      runner = null;
      if (!isScanningPath) {
        // If we're not scanning all paths it means that a user provided an explicit class
        // via the -e com.foo.ClassName runner argument. Therefore, treat it as a valid
        // test case and report failure accordingly.
        runner = new UnloadableClassRunner(description, failure);
      }
    }

    if (runner != null) {
      runnersMap.put(className, runner);
    }
  }

  /**
   * Get the {@link Collection) of {@link Runner runners}.
   */
  List<Runner> getRunnersFor(Collection<String> classNames, boolean isScanningPath) {
    for (String className : classNames) {
      doCreateRunner(className, isScanningPath);
    }
    return new ArrayList<>(runnersMap.values());
  }

  /**
   * Utility method for logging debug messages. Only actually logs a message if LOG_TAG is marked as
   * loggable to limit log spam during normal use.
   */
  private static void logDebug(String msg) {
    if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
      Log.d(LOG_TAG, msg);
    }
  }

  /**
   * Wrapper around a {@link RunnerBuilder} that will reject all abstract classes.
   *
   * <p>This is only used when loading classes found while scanning the class path.
   */
  private static class ScanningRunnerBuilder extends RunnerBuilder {

    private final RunnerBuilder runnerBuilder;

    ScanningRunnerBuilder(RunnerBuilder runnerBuilder) {
      this.runnerBuilder = runnerBuilder;
    }

    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
      // Ignore abstract classes. This could theoretically ignore test classes that should be
      // run, as some RunnerBuilders e.g. Suite do not strictly require the class to be
      // instantiable. However, they have always been ignored during scanning and changing
      // that would cause lots of problems.
      if (Modifier.isAbstract(testClass.getModifiers())) {
        logDebug(String.format("Skipping abstract class %s: not a test", testClass.getName()));
        return null;
      }

      return runnerBuilder.runnerForClass(testClass);
    }
  }

  @VisibleForTesting
  static class UnloadableClassRunner extends Runner {

    private final Description description;
    private final Failure failure;

    UnloadableClassRunner(Description description, Failure failure) {
      this.description = description;
      this.failure = failure;
    }

    @Override
    public Description getDescription() {
      return description;
    }

    @Override
    public void run(RunNotifier notifier) {
      notifier.fireTestStarted(description);
      notifier.fireTestFailure(failure);
      notifier.fireTestFinished(description);
    }
  }
}
