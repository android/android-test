/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.internal.util;

import android.app.Instrumentation;
import android.os.Bundle;

/** Helper class to store frequently passed test parameters between the different classes */
public class AndroidRunnerParams {
  private final Instrumentation instrumentation;
  private final Bundle bundle;
  private final boolean skipExecution;
  private final long perTestTimeout;
  private final boolean ignoreSuiteMethods;

  /**
   * @param instrumentation the {@link Instrumentation} to inject into any tests that require it
   * @param bundle the {@link Bundle} of command line args to inject into any tests that require it
   * @param skipExecution whether or not to skip actual test execution
   * @param perTestTimeout milliseconds timeout value applied to each test where 0 means no timeout
   * @param ignoreSuiteMethods whether or not JUnit3 suite() methods should be executed.
   * @deprecated Use {@link #AndroidRunnerParams(Instrumentation, Bundle, long, boolean)} instead.
   */
  @Deprecated
  public AndroidRunnerParams(
      Instrumentation instrumentation,
      Bundle bundle,
      boolean skipExecution,
      long perTestTimeout,
      boolean ignoreSuiteMethods) {
    this.instrumentation = instrumentation;
    this.bundle = bundle;
    this.skipExecution = skipExecution;
    this.perTestTimeout = perTestTimeout;
    this.ignoreSuiteMethods = ignoreSuiteMethods;
  }

  /**
   * @param instrumentation the {@link Instrumentation} to inject into any tests that require it
   * @param bundle the {@link Bundle} of command line args to inject into any tests that require it
   * @param perTestTimeout milliseconds timeout value applied to each test where 0 means no timeout
   * @param ignoreSuiteMethods whether or not JUnit3 suite() methods should be executed.
   */
  public AndroidRunnerParams(
      Instrumentation instrumentation,
      Bundle bundle,
      long perTestTimeout,
      boolean ignoreSuiteMethods) {
    this.instrumentation = instrumentation;
    this.bundle = bundle;
    this.skipExecution = false;
    this.perTestTimeout = perTestTimeout;
    this.ignoreSuiteMethods = ignoreSuiteMethods;
  }

  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

  public Bundle getBundle() {
    return bundle;
  }

  /**
   * @deprecated {@link org.junit.runner.Runner} implementations no longer need to support skipping
   *     execution of tests.
   */
  @Deprecated
  public boolean isSkipExecution() {
    return skipExecution;
  }

  public long getPerTestTimeout() {
    return perTestTimeout;
  }

  public boolean isIgnoreSuiteMethods() {
    return ignoreSuiteMethods;
  }
}
