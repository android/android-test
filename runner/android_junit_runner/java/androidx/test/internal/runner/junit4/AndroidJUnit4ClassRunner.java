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
package androidx.test.internal.runner.junit4;

import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.junit4.statement.RunAfters;
import androidx.test.internal.runner.junit4.statement.RunBefores;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/** A specialized {@link BlockJUnit4ClassRunner} that can handle timeouts */
public class AndroidJUnit4ClassRunner extends BlockJUnit4ClassRunner {

  private final AndroidRunnerParams androidRunnerParams;

  public AndroidJUnit4ClassRunner(Class<?> klass, AndroidRunnerParams runnerParams)
      throws InitializationError {
    super(klass);
    androidRunnerParams = runnerParams;
  }

  public AndroidJUnit4ClassRunner(Class<?> klass) throws InitializationError {
    this(klass, createRunnerParams());
  }

  private static AndroidRunnerParams createRunnerParams() {
    RunnerArgs runnerArgs =
        new RunnerArgs.Builder().fromBundle(getInstrumentation(), getArguments()).build();
    return new AndroidRunnerParams(
        getInstrumentation(), getArguments(), runnerArgs.testTimeout, false);
  }

  /** Returns a {@link Statement} that invokes {@code method} on {@code test} */
  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    if (UiThreadStatement.shouldRunOnUiThread(method)) {
      return new UiThreadStatement(super.methodInvoker(method, test), true);
    }
    return super.methodInvoker(method, test);
  }

  @Override
  protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
    List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
    return befores.isEmpty() ? statement : new RunBefores(method, statement, befores, target);
  }

  @Override
  protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
    List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
    return afters.isEmpty() ? statement : new RunAfters(method, statement, afters, target);
  }

  /**
   * Default to <a href="http://junit.org/javadoc/latest/org/junit/Test.html#timeout()"><code>
   * org.junit.Test#timeout()</code></a> level timeout if set. Otherwise, set the timeout that was
   * passed to the instrumentation via argument.
   */
  @Override
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    // test level timeout i.e @Test(timeout = 123)
    long timeout = getTimeout(method.getAnnotation(Test.class));

    // use runner arg timeout if test level timeout is not present
    if (timeout <= 0 && androidRunnerParams.getPerTestTimeout() > 0) {
      timeout = androidRunnerParams.getPerTestTimeout();
    }

    if (timeout <= 0) {
      // no timeout was set
      return next;
    }

    // Cannot switch to use builder as that is not supported in JUnit 4.10 which is what is
    // available in AOSP.
    return new FailOnTimeout(next, timeout);
  }

  private long getTimeout(Test annotation) {
    if (annotation == null) {
      return 0;
    }
    return annotation.timeout();
  }
}
