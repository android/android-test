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
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasImeAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.rules.ExpectedException.none;

import android.view.inputmethod.EditorInfo;
import androidx.test.espresso.PerformException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.Suppress;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Tests for {@link EditorAction}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditorActionIntegrationTest {

  @Rule
  public ActivityScenarioRule<SendActivity> rule = new ActivityScenarioRule<>(SendActivity.class);

  @Rule public ExpectedException expectedException = none();

  @Test
  public void pressImeActionButtonOnSearchBox() {
    String searchFor = "rainbows and unicorns";
    onView(withId(R.id.search_box)).perform(scrollTo(), ViewActions.typeText(searchFor));
    onView(withId(R.id.search_box))
        .check(matches(hasImeAction(EditorInfo.IME_ACTION_SEARCH)))
        .perform(pressImeActionButton());
    onView(withId(R.id.search_result)).perform(scrollTo());
    onView(withId(R.id.search_result))
        .check(matches(allOf(isDisplayed(), withText(containsString(searchFor)))));
  }

  /**
   * Test only passes if run in isolation. Unless Gradle supports a single instrumentation per test
   * this test is ignored"
   */
  @Suppress
  @Test
  public void pressImeActionButtonOnNonEditorWidget() {
    expectedException.expect(PerformException.class);
    expectedException.expectCause(
        new CustomTypeSafeMatcher<Throwable>("instance of IllegalStateException") {
          @Override
          protected boolean matchesSafely(Throwable throwable) {
            return throwable instanceof IllegalStateException;
          }
        });
    onView(withId(R.id.send_button)).perform(pressImeActionButton());
  }

  @Test
  public void pressSearchOnDefaultEditText() {
    onView(withId(R.id.enter_data_edit_text)).perform(pressImeActionButton());
  }
}
