/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.web.matcher;

import androidx.test.espresso.EspressoException;

/**
 * An exception which indicates that for a given XPath there were multiple Elements found when only
 * 1 element was expected.
 */
public final class AmbiguousElementMatcherException extends RuntimeException
    implements EspressoException {

  public AmbiguousElementMatcherException(String xpath) {
    // TODO: Print out the DOM and highlight which elements were matched.
    super(
        String.format(
            "Multiple Element(s) found for xpath %s. Please narrow down " + "your search.", xpath));
  }
}
