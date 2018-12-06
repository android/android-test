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

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.LayoutAssertions.noOverlaps;
import static androidx.test.espresso.assertion.PositionAssertions.isAbove;
import static androidx.test.espresso.assertion.PositionAssertions.isLeftAlignedWith;
import static androidx.test.espresso.assertion.PositionAssertions.isLeftOf;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static androidx.test.espresso.matcher.LayoutMatchers.hasEllipsizedText;
import static androidx.test.espresso.matcher.LayoutMatchers.hasMultilineText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Sample accessibility and Layout PositionAssertions tests. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LayoutTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(LayoutIssuesActivity.class);
  }

  /**
   * Put all generic layout tests in a dedicated private method. Generic tests do not depend on
   * application state and can be run in any time.
   */
  private void assertNoLayoutBreakages() {
    // Elements should not overlap
    onView(isRoot()).check(noOverlaps());

    // Buttons should not be ellipsized, though may wrap
    onView(isRoot()).check(selectedDescendantsMatch(
        isAssignableFrom(Button.class), not(hasEllipsizedText())));

    // Add other suitable generic checks, e.g. Accessibility, below
  }

  @Test
  public void testLayout() {
    // Test initial layout state
    assertNoLayoutBreakages();
    // Fine-grained test: assert that Wrap button is not wrapped
    // at this point.
    onView(withId(R.id.wrap)).check(matches(not(hasMultilineText())));
    // Perform actions

    onView(withId(R.id.wrap)).perform(click());

    // Now Wrap button should be wrapped
    onView(withId(R.id.wrap)).check(matches(hasMultilineText()));

    // Long text should be ellipsized
    onView(withId(R.id.ellipsized)).check(matches(hasEllipsizedText()));

    // Test final layout state
    assertNoLayoutBreakages();
  }

  @Test
  public void testRelativePositions() {
    // left text should be left of right button although they share 1 pixel at the boundary.
    onView(withId(R.id.left_text)).check(isLeftOf(withId(R.id.right_button)));

    // Switch length button should be above Wrap button
    onView(withId(R.id.length)).check(isAbove(withId(R.id.wrap)));

    // Switch length button and Wrap button should be aligned to the left.
     onView(withId(R.id.length)).check(isLeftAlignedWith(withId(R.id.wrap)));

  }
}
