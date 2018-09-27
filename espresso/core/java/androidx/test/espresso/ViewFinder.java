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

/** Uses matchers to locate particular views within the view hierarchy. */
public interface ViewFinder {

  /**
   * Immediately locates a single view within the provided view hierarchy.
   *
   * <p>If multiple views match, or if no views match the appropriate exception is thrown.
   *
   * @return A singular view which matches the matcher we were constructed with.
   * @throws AmbiguousViewMatcherException when multiple views match
   * @throws NoMatchingViewException when no views match.
   */
  public View getView() throws AmbiguousViewMatcherException, NoMatchingViewException;
}
