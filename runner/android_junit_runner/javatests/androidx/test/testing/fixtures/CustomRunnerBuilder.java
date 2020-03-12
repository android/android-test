/*
 * Copyright (C) 2017 The Android Open Source Project
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
package androidx.test.testing.fixtures;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

/**
 * A custom runner builder used for testing support for specifying custom {@link RunnerBuilder}
 * classes through the runner args.
 */
public class CustomRunnerBuilder extends RunnerBuilder {

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    if (Runnable.class.isAssignableFrom(testClass)) {
      final Runnable runnable = (Runnable) testClass.getDeclaredConstructor().newInstance();
      final Description description = Description.createTestDescription(testClass, "run");
      return new Runner() {
        @Override
        public Description getDescription() {
          return description;
        }

        @Override
        public void run(RunNotifier notifier) {
          EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
          eachNotifier.fireTestStarted();
          try {
            runnable.run();
          } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
          } catch (Throwable e) {
            eachNotifier.addFailure(e);
          } finally {
            eachNotifier.fireTestFinished();
          }
        }
      };
    } else {
      return null;
    }
  }
}
