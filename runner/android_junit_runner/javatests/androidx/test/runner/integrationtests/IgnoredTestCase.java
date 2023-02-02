/*
 * Copyright (C) 2023 The Android Open Source Project
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
package androidx.test.runner.integrationtests;

import junit.framework.TestCase;
import org.junit.Ignore;

public class IgnoredTestCase extends TestCase {

  public void testNotIgnored() {}

  /**
   * UTP filters out both JUnit3 and JUnit4 tests using annotations. Strange but changing this
   * behavior will introduce regressions.
   */
  @Ignore
  public void testIgnored() {
    fail("should be ignored");
  }
}
