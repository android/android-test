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

package androidx.test.espresso.assertion;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.rules.ExpectedException.none;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.LayoutIssuesActivity;
import androidx.test.ui.app.R;
import junit.framework.AssertionFailedError;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Integration tests for {@link LayoutAssertions}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LayoutAssertionsTest {

  @Rule
  public ActivityScenarioRule<LayoutIssuesActivity> rule =
      new ActivityScenarioRule<>(LayoutIssuesActivity.class);

  @Rule public ExpectedException expectedException = none();

  @Test
  public void assertNoEllipsizedText() {
    expectedException.expect(AssertionFailedError.class);
    onView(isRoot()).check(LayoutAssertions.noEllipsizedText());
  }

  @Test
  public void assertNoMultilineButtons() {
    onView(withId(R.id.wrap)).perform(click());
    expectedException.expect(AssertionFailedError.class);
    onView(isRoot()).check(LayoutAssertions.noMultilineButtons());
  }

  @Test
  public void assertNoOverlaps() {
    onView(withId(R.id.length)).perform(click());
    expectedException.expect(AssertionFailedError.class);
    onView(isRoot()).check(LayoutAssertions.noOverlaps());
  }

  @Test
  public void assertNoOverlapsViewNotFound() {
    onView(withId(R.id.length)).perform(click());
    expectedException.expect(NoMatchingViewException.class);
    onView(withText("nonexistent")).check(LayoutAssertions.noOverlaps());
  }
}
