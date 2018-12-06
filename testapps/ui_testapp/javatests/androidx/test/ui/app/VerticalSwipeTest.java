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
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Demonstrates use of {@link ViewActions#swipeUp()} and {@link ViewActions#swipeDown()}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class VerticalSwipeTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(VerticalViewPagerActivity.class);
  }

  @Test
  public void testSwipingThroughViews() {
    // Should be on position 0 to start with.
    onView(withText("Position #0")).check(matches(isDisplayed()));

    onView(withId(R.id.vertical_pager)).perform(swipeUp());
    onView(withText("Position #1")).check(matches(isDisplayed()));

    onView(withId(R.id.vertical_pager)).perform(swipeUp());
    onView(withText("Position #2")).check(matches(isDisplayed()));
  }

  @Test
  public void testSwipingBackAndForward() {
    // Should be on position 0 to start with.
    onView(withText("Position #0")).check(matches(isDisplayed()));

    onView(withId(R.id.vertical_pager)).perform(swipeUp());
    onView(withText("Position #1")).check(matches(isDisplayed()));

    onView(withId(R.id.vertical_pager)).perform(swipeDown());
    onView(withText("Position #0")).check(matches(isDisplayed()));
  }
}
