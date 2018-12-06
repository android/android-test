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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.PositionAssertions.isBelow;
import static androidx.test.espresso.assertion.PositionAssertions.isLeftOf;
import static androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Highlights basic {@link androidx.test.espresso.Espresso#onView(org.hamcrest.Matcher)}
 * functionality.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicTest {

  @Before
  public void setUp() throws Exception {
    // Espresso will not launch our activity for us, we must launch it via ActivityScenario.launch.
    ActivityScenario.launch(SimpleActivity.class);
  }

  @Test
  public void testSimpleClickAndCheckText() {
    onView(withId(R.id.button_simple))
        .perform(click());

    onView(withId(R.id.text_simple))
        .check(matches(withText("Hello Espresso!")));
  }

  @Test
  public void testTypingAndPressBack() {
    onView(withId(R.id.sendtext_simple))
        .check(matches(withHint(R.string.send_hint)));

    onView(withId(R.id.sendtext_simple))
        .perform(typeText("Have a cup of Espresso."));

    onView(withId(R.id.send_simple))
        .perform(click());

    // Clicking launches a new activity that shows the text entered above. You don't need to do
    // anything special to handle the activity transitions. Espresso takes care of waiting for the
    // new activity to be resumed and its view hierarchy to be laid out.
    onView(withId(R.id.display_data))
        .check(matches(withText(("Have a cup of Espresso."))));

    // Going back to the previous activity - lets make sure our text was perserved.
    pressBack();

    onView(withId(R.id.sendtext_simple))
        .check(matches(withText(containsString("Espresso"))));

    onView(withId(R.id.sendtext_simple))
        .check(matches(withHint(R.string.send_hint)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClickOnSpinnerItemAmericano() {
    // Open the spinner.
    onView(withId(R.id.spinner_simple))
      .perform(click());
    // Spinner creates a List View with its contents - this can be very long and the element not
    // contributed to the ViewHierarchy - by using onData we force our desired element into the
    // view hierarchy.
    onData(allOf(is(instanceOf(String.class)), is("Americano")))
      .perform(click());

    onView(withId(R.id.spinnertext_simple))
      .check(matches(withText(containsString("Americano"))));
  }

  @Test
  public void testRelativePositionOfViews() {
    onView(withId(R.id.button_simple))
        .check(isBelow(withId(R.id.spinnertext_simple)));

    onView(withId(R.id.sendtext_simple))
         .check(isLeftOf(withId(R.id.send_simple)));

    onView(withId(R.id.sendtext_simple))
         .check(isTopAlignedWith(withId(R.id.send_simple)));
  }
}
