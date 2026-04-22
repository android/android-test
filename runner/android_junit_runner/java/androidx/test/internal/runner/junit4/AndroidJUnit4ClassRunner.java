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
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.junit4.statement.RunAfters;
import androidx.test.internal.runner.junit4.statement.RunBefores;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/** A specialized {@link BlockJUnit4ClassRunner} that can handle timeouts */
public class AndroidJUnit4ClassRunner extends BlockJUnit4ClassRunner {

  private final long perTestTimeout;

  /**
   * @deprecated use {@link AndroidJUnit4ClassRunner(Class)} instead.
   */
  @Deprecated
  public AndroidJUnit4ClassRunner(Class<?> klass, AndroidRunnerParams runnerParams)
      throws InitializationError {
    this(klass, runnerParams.getPerTestTimeout());
  }

  public AndroidJUnit4ClassRunner(Class<?> klass, long perTestTimeout) throws InitializationError {
    super(klass);
    this.perTestTimeout = perTestTimeout;
  }

  public AndroidJUnit4ClassRunner(Class<?> klass) throws InitializationError {
    this(klass, RunnerArgs.parseTestTimeout(getArguments()));
  }

  private static final ThreadLocal<CountDownLatch> currentTestStartedLatch = new ThreadLocal<>();
  private static final ThreadLocal<CountDownLatch> currentTestFinishedLatch = new ThreadLocal<>();

  /** Returns a {@link Statement} that invokes {@code method} on {@code test} */
  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    final Statement invoker;
    if (UiThreadStatement.shouldRunOnUiThread(method)) {
      invoker = new UiThreadStatement(super.methodInvoker(method, test), true);
    } else {
      invoker = super.methodInvoker(method, test);
    }
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        CountDownLatch startLatch = currentTestStartedLatch.get();
        if (startLatch != null) {
          startLatch.countDown();
        }
        try {
          invoker.evaluate();
        } finally {
          CountDownLatch finishLatch = currentTestFinishedLatch.get();
          if (finishLatch != null) {
            finishLatch.countDown();
          }
        }
      }
    };
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

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    Object test;
    try {
      test =
          new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
              return createTest();
            }
          }.run();
    } catch (Throwable e) {
      return new Fail(e);
    }

    Statement statement = methodInvoker(method, test);
    statement = possiblyExpectingExceptions(method, test, statement);
    statement = withBefores(method, test, statement);
    statement = withAfters(method, test, statement);
    statement = withPotentialTimeout(method, test, statement);
    try {
      java.lang.reflect.Method withRulesMethod =
          BlockJUnit4ClassRunner.class.getDeclaredMethod(
              "withRules", FrameworkMethod.class, Object.class, Statement.class);
      withRulesMethod.setAccessible(true);
      statement = (Statement) withRulesMethod.invoke(this, method, test, statement);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return statement;
  }

  /**
   * Default to {@link org.junit.Test#timeout()} level timeout if set. Otherwise, set the timeout
   * that was passed to the instrumentation via argument.
   */
  @Override
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    long timeout = getTimeout(method.getAnnotation(Test.class));
    if (timeout <= 0 && perTestTimeout > 0) {
      timeout = perTestTimeout;
    }
    final long finalTimeout = timeout;

    if (finalTimeout <= 0 || UiThreadStatement.shouldRunOnUiThread(method)) {
      return next;
    }

    return new Statement() {
      @Override
      @SuppressWarnings("Interruption") // We want to interrupt the thread to stop the test.
      public void evaluate() throws Throwable {
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final CountDownLatch testStartedLatch = new CountDownLatch(1);
        final CountDownLatch testFinishedLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(1);

        Thread thread =
            new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    currentTestStartedLatch.set(testStartedLatch);
                    currentTestFinishedLatch.set(testFinishedLatch);
                    try {
                      next.evaluate();
                    } catch (Throwable t) {
                      failure.set(t);
                    } finally {
                      testStartedLatch.countDown();
                      testFinishedLatch.countDown();
                      doneLatch.countDown();
                      currentTestStartedLatch.remove();
                      currentTestFinishedLatch.remove();
                    }
                  }
                },
                "Time-limited test");
        thread.setDaemon(true);
        thread.start();

        testStartedLatch.await();
        boolean finishedInTime = testFinishedLatch.await(finalTimeout, MILLISECONDS);

        if (!finishedInTime) {
          thread.interrupt();
          throw new TestTimedOutException(finalTimeout, MILLISECONDS);
        }

        doneLatch.await();
        if (failure.get() != null) {
          throw failure.get();
        }
      }
    };
  }

  private long getTimeout(Test annotation) {
    if (annotation == null) {
      return 0;
    }
    return annotation.timeout();
  }
}
