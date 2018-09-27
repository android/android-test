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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import androidx.test.filters.SdkSuppress;
import org.hamcrest.Matchers;

@LargeTest
public class TransitionActivityMainTest
    extends ActivityInstrumentationTestCase2<TransitionActivityMain> {

  @SuppressWarnings("deprecation")
  public TransitionActivityMainTest() {
        // This constructor was deprecated - but we want to support lower API levels.
    super("androidx.test.ui.app", TransitionActivityMain.class);
  }
  @Override
  public void setUp() throws Exception {
    super.setUp();
        // Espresso will not launch our activity for us, we must launch it via getActivity().
    getActivity();
  }

  // This test only applies to Lollipop+
  @SdkSuppress(minSdkVersion = 21)
  public void testTransition() throws InterruptedException {
    onView(withId(R.id.grid)).check(matches(isDisplayed()));
    onData(Matchers.anything()).atPosition(0).perform(click());
    onView(withText(containsString("Flying in the Light")))
        .check(matches(isDisplayed()));
    pressBack();
    onView(withId(R.id.grid)).check(matches(isDisplayed()));
  }

  @SdkSuppress(minSdkVersion = 21)
  public void testInterruptedBackDoesntExit() {
    // Set a flag in the activity to intercept the back button.
    ((TransitionActivityMain) getActivity()).setExitOnBackPressed(false);
    pressBack();
    pressBack();
    pressBack();
    // Nothing should happen, activity doesn't exit.

    onView(withId(R.id.grid)).check(matches(isDisplayed()));
  }
}
