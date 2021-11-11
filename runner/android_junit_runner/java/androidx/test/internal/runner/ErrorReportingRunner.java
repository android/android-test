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

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * A {@link Runner} to report a critical initialization failure with a test class that prevents any
 * methods from being executed.
 *
 * <p>Patterned after org.junit.internal.runners.ErrorReportingRunner, which sadly is not public.
 */
public class ErrorReportingRunner extends Runner {
  private final String className;
  private final Throwable cause;

  public ErrorReportingRunner(String className, Throwable e) {
    this.className = className;
    this.cause = e;
  }

  @Override
  public Description getDescription() {
    Description description = Description.createSuiteDescription(className);
    description.addChild(describeCause());
    return description;
  }

  private Description describeCause() {
    // insert a child with 'initializationError' as display name for consistency with JUnit
    return Description.createTestDescription(className, "initializationError");
  }

  @Override
  public void run(RunNotifier notifier) {
    Description description = describeCause();
    notifier.fireTestStarted(description);
    notifier.fireTestFailure(new Failure(description, cause));
    notifier.fireTestFinished(description);
  }
}
