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

package androidx.test.espresso.action;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.ViewAction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SwipeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for swiping actions. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SwipeActionIntegrationTest {
  @Rule public ActivityTestRule<SwipeActivity> rule = new ActivityTestRule<>(SwipeActivity.class);

  /** Tests that a small view can be swiped in both directions. */
  @Test
  public void swipeOverSmallView() {
    onView(withId(R.id.small_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeRight())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeRight())
        .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that a view can be swiped outwards from its edge. */
  @Test
  public void swipeOutwardFromEdge() {
    onView(withId(R.id.small_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeLeftFromLeftEdge())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeLeftFromLeftEdge())
        .check(matches(hasDescendant(withText("Position #2"))));
  }

  /** Like {@link ViewActions#swipeLeft()}, but starts at the left edge of the view. */
  private static ViewAction swipeLeftFromLeftEdge() {
    CoordinatesProvider startPoint = GeneralLocation.CENTER_LEFT;
    CoordinatesProvider endPoint = GeneralLocation.translate(startPoint, -1.0f, 0);
    return new GeneralSwipeAction(Swipe.FAST, startPoint, endPoint, Press.FINGER);
  }

  /** Tests that trying to swipe beyond the start of a view pager has no effect. */
  @Test
  public void swipingRightHasNoEffectWhenAtStart() {
    onView(withId(R.id.small_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeRight())
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeRight())
        .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe beyond the end of a view pager has no effect. */
  @Test
  public void swipingLeftHasNoEffectWhenAtEnd() {
    onView(withId(R.id.small_pager))
        .perform(swipeLeft())
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #2"))));
  }

  /** Tests that swiping across a partially overlapped view works correctly. */
  @Test
  public void swipeOverPartiallyOverlappedView() {
    onView(withId(R.id.overlapped_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeLeft())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeRight())
        .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe a view that doesn't respond to swipes has no effect. */
  @Test
  public void swipeOverUnswipableView() {
    onView(withId(R.id.text_simple))
        .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))))
        .perform(swipeLeft())
        .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))))
        .perform(swipeRight())
        .check(matches(allOf(isDisplayed(), withText(R.string.text_simple))));
  }

  /** Tests that a vertical paper can be swiped up and down */
  @Test
  public void swipeOverVerticalPager() {
    onView(withId(R.id.vertical_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeUp())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeUp())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeDown())
        .check(matches(hasDescendant(withText("Position #1"))))
        .perform(swipeDown())
        .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe beyond the start of a vertical view pager has no effect. */
  @Test
  public void swipingDownHasNoEffectWhenAtStart() {
    onView(withId(R.id.vertical_pager))
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeDown())
        .check(matches(hasDescendant(withText("Position #0"))))
        .perform(swipeDown())
        .check(matches(hasDescendant(withText("Position #0"))));
  }

  /** Tests that trying to swipe beyond the end of a vertical view pager has no effect. */
  @Test
  public void swipingUpHasNoEffectWhenAtEnd() {
    onView(withId(R.id.vertical_pager))
        .perform(swipeUp())
        .perform(swipeUp())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeUp())
        .check(matches(hasDescendant(withText("Position #2"))))
        .perform(swipeUp())
        .check(matches(hasDescendant(withText("Position #2"))));
  }
}
