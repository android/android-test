/*
 * Copyright (C) 2021 The Android Open Source Project
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases that generates {@link androidx.test.espresso.NoMatchingViewException} and {@link
 * androidx.test.espresso.AmbiguousViewMatcherException}. The tests fails and must be run with a
 * test_result_assertion_plugin that validate expected failure, see BUILD for details.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DuplicateViewActivityTest {

  @Rule
  public ActivityScenarioRule<DuplicateViewActivity> activityScenarioRule =
      new ActivityScenarioRule<>(DuplicateViewActivity.class);

  @Test
  public void testNoMatchingViewException() {
    // The requested withId() is not in the layout.
    // This fails with NoMatchingViewException.
    onView(withId(android.R.id.list)).check(matches(withText("Hello world!")));
  }

  @Test
  public void testAmbiguousViewMatcherException() {
    // There are 2 views with the requested string.
    // This fails with AmbiguousViewMatcherException.
    onView(withText("Message"))
        .check(matches(withText("Ambiguous match due to views with same text.")));
  }
}
