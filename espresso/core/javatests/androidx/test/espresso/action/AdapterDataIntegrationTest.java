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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.LongListActivity;
import androidx.test.ui.app.R;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for operating on data displayed in an adapter. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AdapterDataIntegrationTest {

  @Rule
  public ActivityScenarioRule<LongListActivity> rule =
      new ActivityScenarioRule<>(LongListActivity.class);

  @Test
  public void clickAroundList() {
    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 99"))))
        .perform(click());
    onView(withId(R.id.selection_row_value)).check(matches(withText("99")));

    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 1"))))
        .perform(click());

    onView(withId(R.id.selection_row_value)).check(matches(withText("1")));

    onData(is(instanceOf(Map.class))).atPosition(20).perform(click());

    onView(withId(R.id.selection_row_value)).check(matches(withText("20")));

    // lets operate on a specific child of a row...
    onData(allOf(is(instanceOf(Map.class)), hasEntry(is(LongListActivity.STR), is("item: 50"))))
        .onChildView(withId(R.id.item_size))
        .perform(click())
        .check(matches(withText(String.valueOf("item: 50".length()))));

    onView(withId(R.id.selection_row_value)).check(matches(withText("50")));
  }

  @Test
  public void selectItemWithSibling() {
    onView(allOf(withText("7"), hasSibling(withText("item: 0")))).perform(click());
    onView(withId(R.id.selection_row_value)).check(matches(withText("0")));
  }
}
