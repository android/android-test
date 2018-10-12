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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.CursorMatchers.withRowInt;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.fail;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CursorAdapterTest {

  @Before
  public void setUp() throws Exception {
    // Espresso will not launch our activity for us, we must launch it via
    // ActivityScenario.launch
    ActivityScenario.launch(CursorAdapterActivity.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClickOnFirstCursorItem() {
    final String expectedRowString = "item: 0";

    // Match and click on view with a row int item length of 7 and the row String "item: 0".
    // Strict column checks are turned off because this is a MergeCursor.
    onData(
        allOf(
            withRowInt(CursorAdapterListFragment.COLUMN_LEN, 7)
                .withStrictColumnChecks(false),
            withRowString(CursorAdapterListFragment.COLUMN_STR, expectedRowString)
                .withStrictColumnChecks(false)))
        .perform(click());

    // Check if the correct test is displayed in the Ui.
    onView(withId(R.id.selected_item_value)).check(matches(withText(expectedRowString)));
  }

  @Test
  public void testClickOnFirstAndLastItemWithLength7() {
    final String expectedFirstItemString = "item: 0";
    onData(withRowInt(CursorAdapterListFragment.COLUMN_LEN, 7).withStrictColumnChecks(false))
        .atPosition(0)
        .perform(click());
    onView(withId(R.id.selected_item_value)).check(matches(withText(expectedFirstItemString)));

    final String expectedLastItemString = "item: 9";
    onData(withRowInt(CursorAdapterListFragment.COLUMN_LEN, 7).withStrictColumnChecks(false))
        .atPosition(9)
        .perform(click());

    onView(withId(R.id.selected_item_value)).check(matches(withText(expectedLastItemString)));
  }

  @Test
  public void testClickOnDifferentColumnName() {
    // You can also refer to colums by name. Note, names in each row don't have to be the same.
    onData(withRowInt("surprise!", 1).withStrictColumnChecks(false)).perform(click());
    onView(withId(R.id.selected_item_value)).check(matches(withText("item: 20")));
  }

  @Test
  public void testClickOnColumnNameNotFound() {
    try {
      onData(withRowInt("not_there", 1).withStrictColumnChecks(false)).perform(click());
      fail("Should have thrown PerformException");
    } catch (PerformException expected) {}
  }

}
