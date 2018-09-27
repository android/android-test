/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.multiprocess.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Sample test to showcase Idling Resource in a multi-process environment.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IdlingActivityTest {

  private static final String STRING_TO_BE_TYPED = "Hello Multiprocess Espresso!";
  private static final String IDLING_PROC_NAME = "androidx.test.multiprocess.app:idling";

  @Rule
  public ActivityTestRule<IdlingActivity> mActivityRule = new ActivityTestRule<>(
      IdlingActivity.class);

  @Test
  public void verifyChangingTextAsynchronously() {
    // Type text and then press the button.
    onView(withId(R.id.editTextUserInput))
        .perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
    onView(withId(R.id.changeTextBtn)).perform(click());

    // The above will be a long running task where an IdlingResource is required in order for
    // Espresso to synchronize against the interaction. The IdlingResource
    // registration/unregistration responsibility is now shifted to the Activity (or process) under
    // test which enables the ability to handle IdlingResources cross process.

    // Check that the text was changed.
    onView(withId(R.id.textToBeChanged)).check(matches(withText(STRING_TO_BE_TYPED)));
  }

  @Test
  public void verifyRunningInIdlingProcess() {
    onView(withId(R.id.textIdlingProcessName)).check(matches(withText(IDLING_PROC_NAME)));
  }
}
