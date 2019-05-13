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

package androidx.test.espresso.contrib;

import androidx.core.view.GravityCompat;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.espresso.matcher.BoundedMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/** Hamcrest matchers for a {@link DrawerLayout}. */
public final class DrawerMatchers {

  private DrawerMatchers() {
    // forbid instantiation
  }

  /**
   * Returns a matcher that verifies that the drawer with the specified gravity is open. Matches
   * only when the drawer is fully open. Use {@link #isClosed(int)} instead of {@code not(isOpen())}
   * when you wish to check that the drawer is fully closed.
   */
  public static Matcher<View> isOpen(final int gravity) {
    return new BoundedMatcher<View, DrawerLayout>(DrawerLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("is drawer open");
      }

      @Override
      public boolean matchesSafely(DrawerLayout drawer) {
        return drawer.isDrawerOpen(gravity);
      }
    };
  }

  /**
   * Returns a matcher that verifies that the drawer (with gravity START) is open. Matches only when
   * the drawer is fully open. Use {@link #isClosed()} instead of {@code not(isOpen())} when you
   * wish to check that the drawer is fully closed.
   */
  public static Matcher<View> isOpen() {
    return isOpen(GravityCompat.START);
  }

  /**
   * Returns a matcher that verifies that the drawer with the specified gravity is closed. Matches
   * only when the drawer is fully closed. Use {@link #isOpen(int)} instead of {@code
   * not(isClosed()))} when you wish to check that the drawer is fully open.
   */
  public static Matcher<View> isClosed(final int gravity) {
    return new BoundedMatcher<View, DrawerLayout>(DrawerLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("is drawer closed");
      }

      @Override
      public boolean matchesSafely(DrawerLayout drawer) {
        return !drawer.isDrawerVisible(gravity);
      }
    };
  }

  /**
   * Returns a matcher that verifies that the drawer (with gravity START) is closed. Matches only
   * when the drawer is fully closed. Use {@link #isOpen()} instead of {@code not(isClosed()))} when
   * you wish to check that the drawer is fully open.
   */
  public static Matcher<View> isClosed() {
    return isClosed(GravityCompat.START);
  }
}
