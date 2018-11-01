/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule;

import android.util.Log;
import androidx.test.annotation.UiThreadTest;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This rule allows the test method annotated with {@link UiThreadTest} to execute on the
 * application's main thread (or UI thread).
 *
 * <p>Note, methods annotated with <a
 * href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a> and <a
 * href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a> will also
 * be executed on the UI thread.
 *
 * @see androidx.test.annotation.UiThreadTest if you need to switch in and out of the UI
 *     thread within your method.
 * @deprecated use {@link UiThreadTest} directly without this rule. {@link UiThreadTest} is now
 *     supported as part of the core Android test runner to provide the ability to run methods
 *     annotated with <code>@Before</code> and <code>@After</code> on the UI thread regardless of
 *     what <code>@Test</code> is annotated with.
 */
@Deprecated
public class UiThreadTestRule implements TestRule {
  private static final String TAG = "UiThreadTestRule";

  @Override
  public Statement apply(final Statement base, Description description) {
    if (base instanceof FailOnTimeout
        || (base instanceof UiThreadStatement && !((UiThreadStatement) base).isRunOnUiThread())) {
      // In upstream junit code Rules Statements are handled last. Since we now handle
      // @UiThreadTest as part of the core Android runner, there is a chance that
      // UiThreadStatement was already applied on the current statement.
      // This is mainly for compatibility reasons to deprecated this rule.
      return base;
    }
    return new UiThreadStatement(base, shouldRunOnUiThread(description));
  }

  protected boolean shouldRunOnUiThread(Description description) {
    if (description.getAnnotation(android.test.UiThreadTest.class) != null) {
      Log.w(
          TAG,
          "Deprecated android.test.UiThreadTest annotation is used! please switch "
              + "to using androidx.test.annotation.UiThreadTest instead.");
      return true;
    }
    return description.getAnnotation(UiThreadTest.class) != null;
  }

  /**
   * Helper method for running part of a method on the UI thread.
   *
   * <p>Note: In most cases it is simpler to annotate the test method with {@link UiThreadTest}.
   *
   * <p>Use this method if you need to switch in and out of the UI thread within your method.
   *
   * @param runnable runnable containing test code in the {@link Runnable#run()} method
   * @see androidx.test.annotation.UiThreadTest
   */
  public void runOnUiThread(final Runnable runnable) throws Throwable {
    UiThreadStatement.runOnUiThread(runnable);
  }
}
