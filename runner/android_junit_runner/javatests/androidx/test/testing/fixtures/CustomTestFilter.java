/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.test.testing.fixtures;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * A custom filter used for testing support for specifying custom {@link Filter} classes through the
 * runner args.
 */
public class CustomTestFilter extends Filter {

  @Override
  public boolean shouldRun(Description description) {
    return !description.isTest() || "testOther".equals(description.getMethodName());
  }

  @Override
  public String describe() {
    return "custom filter";
  }
}
