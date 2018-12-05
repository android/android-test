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
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

import androidx.test.espresso.PerformException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** {@link TypeTextAction} integration tests. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class TypeTextActionIntegrationTest {

  @Rule public ExpectedException expectedException = none();

  @Rule
  public ActivityScenarioRule<SendActivity> rule = new ActivityScenarioRule<>(SendActivity.class);

  @Test
  public void typeTextActionPerform() {
    onView(withId(is(R.id.send_data_to_call_edit_text))).perform(typeText("Hello!"));
  }

  @Test
  public void typeTextActionPerformWithEnter() {
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("Hello World!\n"));
    onView(allOf(withId(R.id.enter_data_response_text), withText("Hello World!")))
        .check(matches(isDisplayed()));
  }

  @Test
  public void typeTextInFocusedView() {
    onView(withId(is(R.id.send_data_to_call_edit_text)))
        .perform(typeText("Hello World How Are You Today? I have alot of text to type."));
    onView(withId(is(R.id.send_data_to_call_edit_text)))
        .perform(typeTextIntoFocusedView("Jolly good!"));
    onView(withId(is(R.id.send_data_to_call_edit_text)))
        .check(
            matches(
                withText(
                    "Hello World How Are You Today? I have alot of text to type.Jolly good!")));
  }

  /**
   * Test only passes if run in isolation. Unless Gradle supports a single instrumentation per test
   * this test is ignored"
   */
  @Test
  public void typeTextInFocusedView_constraintBreakage() {
    onView(withId(is(R.id.send_data_to_call_edit_text)))
        .perform(typeText("Hello World How Are You Today? I have alot of text to type."));
    expectedException.expect(PerformException.class);
    onView(withId(is(R.id.edit_text_message)))
        .perform(scrollTo(), typeTextIntoFocusedView("Jolly good!"));
  }

  @Test
  public void typeTextInDelegatedEditText() {
    String toType = "honeybadger doesn't care";
    onView(allOf(withParent(withId(R.id.delegating_edit_text)), withId(R.id.delegate_edit_text)))
        .perform(scrollTo(), typeText(toType), pressImeActionButton());
    onView(withId(R.id.edit_text_message))
        .perform(scrollTo())
        .check(matches(withText(containsString(toType))));
  }

  /**
   * Test only passes if run in isolation. Unless Gradle supports a single instrumentation per test
   * this test is ignored"
   */
  @Test
  public void testTypeText_NonEnglish() {
    expectedException.expect(RuntimeException.class);
    String toType = "在一个月之内的话";
    onView(allOf(withParent(withId(R.id.delegating_edit_text)), withId(R.id.delegate_edit_text)))
        .perform(scrollTo(), typeText(toType));
  }
}
