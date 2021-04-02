/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.internal.events.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.runner.Description;

/** A collection of methods for parsing and analyzing {@link Description} objects. */
final class JUnitDescriptionParser {
  private JUnitDescriptionParser() {}
  /**
   * Gets all individual Test Case {@link Description}s from a single {@link Description}.
   *
   * <p>JUnit {@link Description}s are organized in a tree. A Runner is situated at the top of the
   * tree, containing (maybe) Test Suites, which contain (maybe) Test Cases. For our use case we
   * don't really care about Test Suites, we only care about Test Cases and, occasionally, Runners.
   *
   * <p>A {@link Description} representing a single Test Case is safe to parse here as it will only
   * find itself and return a list containing itself.
   *
   * @param origin the {@link Description} that will be treated as the top-most node in our tree.
   */
  public static List<Description> getAllTestCaseDescriptions(Description origin) {
    List<Description> testCases = new ArrayList<>();
    Deque<Description> walk = new ArrayDeque<>();
    walk.add(origin);
    while (!walk.isEmpty()) {
      Description current = walk.pop();
      walk.addAll(current.getChildren());
      if (current.isTest()) {
        testCases.add(current);
      }
    }

    return testCases;
  }
}
