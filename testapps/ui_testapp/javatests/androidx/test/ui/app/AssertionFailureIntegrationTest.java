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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for Espresso test failures. */
@RunWith(AndroidJUnit4.class)
public final class AssertionFailureIntegrationTest {

  @Rule
  public final ActivityScenarioRule<SimpleActivity> activityScenarioRule =
      new ActivityScenarioRule<>(SimpleActivity.class);

  @Test
  public void check_succeeded() {
    onView(withId(R.id.button_simple)).perform(click());
    onView(withId(R.id.text_simple)).check(matches(withText("Hello Espresso!")));
  }

  @Test
  public void perform_noMatchingViewException() {
    onView(withId(R.id.button_cancel)).perform(click());
  }

  @Test
  public void check_assertionFailedWithCauseError() {
    onView(withId(R.id.button_simple)).perform(click());
    onView(withId(R.id.text_simple)).check(matches(withText("Wrong greetings!")));
  }
}
