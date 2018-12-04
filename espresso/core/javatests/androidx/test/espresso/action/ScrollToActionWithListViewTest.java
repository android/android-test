/*
 * Copyright (C) 2016 The Android Open Source Project
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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.VeryLongListViewActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for ScrollToAction Within ListView with long header and footer. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScrollToActionWithListViewTest {

  @Rule public ActivityScenarioRule rule = new ActivityScenarioRule(VeryLongListViewActivity.class);

  @Test
  public void shouldScrollToBottomHeaderTextOfListView() {
    onView(withId(is(R.id.bottom_header_text)))
        .check(matches(not(isDisplayed())))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
  }

  @Test
  public void shouldScrollToBottomFooterTextOfListView() {
    onData(is(instanceOf(String.class))).atPosition(6).perform(scrollTo());
    onView(withId(is(R.id.bottom_footer_text)))
        .check(matches(not(isDisplayed())))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
  }
}
