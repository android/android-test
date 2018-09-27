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

package androidx.test.espresso.matcher;

import android.text.Layout;
import android.view.View;
import android.widget.TextView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matches to detect typical layout issues. */
public final class LayoutMatchers {

  private LayoutMatchers() {}

  /**
   * Matches TextView elements having ellipsized text. If text is too long to fit into a TextView,
   * it can be either ellipsized ('Too long' shown as 'Too l…' or '… long') or cut off ('Too long'
   * shown as 'Too l'). Though acceptable in some cases, usually indicates bad user experience.
   */
  public static Matcher<View> hasEllipsizedText() {
    return new TypeSafeMatcher<View>(TextView.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("has ellipsized text");
      }

      @Override
      public boolean matchesSafely(View tv) {
        Layout layout = ((TextView) tv).getLayout();
        if (layout != null) {
          int lines = layout.getLineCount();
          return lines > 0 && layout.getEllipsisCount(lines - 1) > 0;
        }
        return false;
      }
    };
  }

  /** Matches TextView elements having multiline text. */
  public static Matcher<View> hasMultilineText() {
    return new TypeSafeMatcher<View>(TextView.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("has more than one line of text");
      }

      @Override
      public boolean matchesSafely(View tv) {
        return ((TextView) tv).getLineCount() > 1;
      }
    };
  }
}
