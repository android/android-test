/*
 * Copyright (C) 2014 The Android Open Source Project
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
 */

package androidx.test.espresso;

import android.view.View;

/**
 * Responsible for performing assertions on a View element.<br>
 *
 * <p>This is considered part of the test framework public API - developers are free to write their
 * own assertions as long as they meet the following requirements:
 *
 * <ul>
 *   <li>Do not mutate the passed in view.
 *   <li>Throw junit.framework.AssertionError when the view assertion does not hold.
 *   <li>Implementation runs on the UI thread - so it should not do any blocking operations
 *   <li>Downcasting the view to a specific type is allowed, provided there is a test that view is
 *       an instance of that type before downcasting. If not, an AssertionError should be thrown.
 *   <li>It is encouraged to access non-mutating methods on the view to perform assertion.
 * </ul>
 *
 * <br>
 *
 * <p>Strongly consider using a existing ViewAssertion via the ViewAssertions utility class before
 * writing your own assertion.
 */
public interface ViewAssertion {

  /**
   * Checks the state of the given view (if such a view is present).
   *
   * @param view the view, if one was found during the view interaction or null if it was not (which
   *     may be an acceptable option for an assertion)
   * @param noViewFoundException an exception detailing why the view could not be found or null if
   *     the view was found
   */
  void check(View view, NoMatchingViewException noViewFoundException);
}
