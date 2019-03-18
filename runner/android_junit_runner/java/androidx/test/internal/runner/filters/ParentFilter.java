/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.internal.runner.filters;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/** Helper parent class for {@link Filter} that allows suites to run if any child matches. */
public abstract class ParentFilter extends Filter {
  /** {@inheritDoc} */
  @Override
  public boolean shouldRun(Description description) {
    if (description.isTest()) {
      return evaluateTest(description);
    }
    // this is a suite, explicitly check if any children should run
    for (Description each : description.getChildren()) {
      if (shouldRun(each)) {
        return true;
      }
    }
    // no children to run, filter this out
    return false;
  }

  /**
   * Determine if given test description matches filter.
   *
   * @param description the {@link Description} describing the test
   * @return <code>true</code> if matched
   */
  protected abstract boolean evaluateTest(Description description);
}
