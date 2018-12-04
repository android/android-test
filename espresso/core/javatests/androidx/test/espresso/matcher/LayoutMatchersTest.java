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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.LayoutIssuesActivity;
import androidx.test.ui.app.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link LayoutMatchers}. Layout matchers depend on layout pass, thus tests
 * below depend on Espresso ability to wait for activity updates transparently to a user.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LayoutMatchersTest {

  @Rule
  public ActivityScenarioRule<LayoutIssuesActivity> rule =
      new ActivityScenarioRule<>(LayoutIssuesActivity.class);

  @Test
  public void ellipsizedTextMatcher() {
    onView(withId(R.id.ellipsized)).check(matches(LayoutMatchers.hasEllipsizedText()));
    onView(withId(R.id.wrap)).check(matches(not(LayoutMatchers.hasEllipsizedText())));
  }

  @Test
  public void multilineTextMatcher() {
    onView(withId(R.id.ellipsized)).check(matches(not(LayoutMatchers.hasMultilineText())));
    onView(withId(R.id.wrap))
        .perform(click())
        .perform(click())
        .check(matches(LayoutMatchers.hasMultilineText()));
  }
}
