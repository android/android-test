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

package androidx.test.internal.runner.junit3;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.junit.runner.Describable;
import org.junit.runner.Description;

/**
 * A {@link TestSuite} that discards references to included tests when execution is complete. Done
 * so tests can be garbage collected and memory freed.
 */
@Ignore
public class NonLeakyTestSuite extends TestSuite {
  public NonLeakyTestSuite(Class<?> testClass) {
    super(testClass);
  }

  @Override
  public void addTest(Test test) {
    super.addTest(new NonLeakyTest(test));
  }

  private static class NonLeakyTest implements Test, Describable {
    private Test delegate;
    private final Description desc;

    NonLeakyTest(Test delegate) {
      this.delegate = delegate;
      // cache description so it's available after execution
      this.desc = JUnit38ClassRunner.makeDescription(this.delegate);
    }

    @Override
    public int countTestCases() {
      if (delegate != null) {
        return delegate.countTestCases();
      } else {
        return 0;
      }
    }

    @Override
    public void run(TestResult result) {
      delegate.run(result);
      delegate = null;
    }

    @Override
    public Description getDescription() {
      return desc;
    }

    @Override
    public String toString() {
      if (delegate != null) {
        return delegate.toString();
      } else {
        return desc.toString();
      }
    }
  }
}
