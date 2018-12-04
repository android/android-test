/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.rule;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

@RunWith(AndroidJUnit4.class)
@LargeTest
public final class DisableOnAndroidDebugTest {
  @Rule public final ExpectedException expectedException = ExpectedException.none();

  private static class FailOnExecution implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private static class ToggleableDisableOnAndroidDebug extends DisableOnAndroidDebug {
    private final boolean isDebugging;

    ToggleableDisableOnAndroidDebug(TestRule delegate, boolean isDebugging) {
      super(delegate);
      this.isDebugging = isDebugging;
    }

    @Override
    public boolean isDebugging() {
      return isDebugging;
    }
  }

  @Test
  public void disablesWhenDebugging() throws Throwable {
    TestRule rule = new ToggleableDisableOnAndroidDebug(new FailOnExecution(), true);
    final AtomicInteger value = new AtomicInteger();
    Statement incrementValue =
        new Statement() {
          @Override
          public void evaluate() throws Throwable {
            value.incrementAndGet();
          }
        };
    rule.apply(incrementValue, null).evaluate();
    assertEquals(1, value.get());
  }

  @Test
  public void enabledWhenNotDebugging() throws Throwable {
    expectedException.expect(UnsupportedOperationException.class);

    TestRule rule = new ToggleableDisableOnAndroidDebug(new FailOnExecution(), false);
    final AtomicInteger value = new AtomicInteger();
    Statement incrementValue =
        new Statement() {
          @Override
          public void evaluate() throws Throwable {
            value.incrementAndGet();
          }
        };
    rule.apply(incrementValue, null).evaluate();
  }
}
