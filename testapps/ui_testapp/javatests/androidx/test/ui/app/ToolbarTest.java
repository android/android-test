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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;

import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Demonstrates Espresso with Toolbar. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ToolbarTest {

  @Before
  public void setUp() throws Exception {
    // Espresso will not launch our activity for us, we must launch it via ActivityScenario.launch.
    ActivityScenario.launch(ToolbarActivity.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClickActionbarItem() {
    onView(withId(R.id.action_lock))
        .perform(click());

    onView(withId(R.id.toolbar_result))
        .check(matches(withText("Lock")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testActionbarOverflow() {
    // Open the overflow menu from action bar. We need custom matcher, because
    // Espresso.openActionBarOverflowOrOptionsMenu matches with both toolbar OverflowButtons.
    onView(allOf(OVERFLOW_BUTTON_MATCHER, isDescendantOfA(withId(R.id.toolbar))))
        .perform(click());

    onView(withText("Key"))
        .perform(click());

    onView(withId(R.id.toolbar_result))
        .check(matches(withText("Key")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClickToolbarItem() {
    onView(withId(R.id.action_save)).
        perform(click());

    onView(withId(R.id.toolbar_result))
        .check(matches(withText("Save")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testToolbarOverflow() {
    // Open the overflow menu from tool bar. We need custom matcher, because
    // Espresso.openActionBarOverflowOrOptionsMenu matches with both toolbar OverflowButtons.
    onView(allOf(OVERFLOW_BUTTON_MATCHER, isDescendantOfA(withId(R.id.toolbar_over_text))))
        .perform(click());

    onView(withText("World"))
        .perform(click());

    onView(withId(R.id.toolbar_result))
        .check(matches(withText("World")));
  }

  @SuppressWarnings("unchecked")
  private static final Matcher<View> OVERFLOW_BUTTON_MATCHER = anyOf(
      allOf(isDisplayed(), withContentDescription("More options")),
      allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton"))));
}
