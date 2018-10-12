/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package androidx.test.internal.runner.junit4.statement;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/** <code>@UiThreadTest</code> aware implementation of {@link RunBefores}. */
public class RunBefores extends UiThreadStatement {
  private final Statement next;

  private final Object target;

  private final List<FrameworkMethod> befores;

  /**
   * Run all non-overridden {@code @Before} methods on this class and superclasses before running
   * {@code next}; if any throws an Exception, stop execution and pass the exception on.
   *
   * <p>{@code @Before} methods that also annotated with <code>@UiThreadTest</code> will be executed
   * on the UI Thread.
   *
   * @param next the original statement
   * @param befores methods annotated with {@code @Before}
   * @param target the test case instance
   */
  public RunBefores(
      FrameworkMethod method, Statement next, List<FrameworkMethod> befores, Object target) {
    super(next, shouldRunOnUiThread(method));
    this.next = next;
    this.befores = befores;
    this.target = target;
  }

  @Override
  public void evaluate() throws Throwable {
    final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
    for (final FrameworkMethod before : befores) {
      if (shouldRunOnUiThread(before)) {
        runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  before.invokeExplosively(target);
                } catch (Throwable throwable) {
                  exceptionRef.set(throwable);
                }
              }
            });

        // if any Exception thrown, stop execution and pass the exception on.
        Throwable throwable = exceptionRef.get();
        if (throwable != null) {
          throw throwable;
        }
      } else {
        before.invokeExplosively(target);
      }
    }

    next.evaluate();
  }
}
