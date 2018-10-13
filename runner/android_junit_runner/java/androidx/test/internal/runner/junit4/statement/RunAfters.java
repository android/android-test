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
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/** <code>@UiThreadTest</code> aware implementation of {@link RunAfters}. */
public class RunAfters extends UiThreadStatement {
  private final Statement next;

  private final Object target;

  private final List<FrameworkMethod> afters;

  /**
   * Run all non-overridden {@code @After} methods on this class and superclasses before running
   * {@code next}; all After methods are always executed: exceptions thrown by previous steps are
   * combined, if necessary, with exceptions from After methods into a {@link
   * MultipleFailureException}.
   *
   * <p>{@code @After} methods that also annotated with <code>@UiThreadTest</code> will be executed
   * on the UI Thread.
   *
   * @param next the original statement
   * @param afters methods annotated with {@code @After}
   * @param target the test case instance
   */
  public RunAfters(
      FrameworkMethod method, Statement next, List<FrameworkMethod> afters, Object target) {
    super(next, shouldRunOnUiThread(method));
    this.next = next;
    this.afters = afters;
    this.target = target;
  }

  @Override
  public void evaluate() throws Throwable {
    final List<Throwable> errors = new CopyOnWriteArrayList<>();

    try {
      next.evaluate();
    } catch (Throwable e) {
      errors.add(e);
    } finally {
      for (final FrameworkMethod each : afters) {
        if (shouldRunOnUiThread(each)) {
          runOnUiThread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    each.invokeExplosively(target);
                  } catch (Throwable throwable) {
                    errors.add(throwable);
                  }
                }
              });
        } else {
          try {
            each.invokeExplosively(target);
          } catch (Throwable e) {
            errors.add(e);
          }
        }
      }
    }
    MultipleFailureException.assertEmpty(errors);
  }
}
