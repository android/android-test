/*
 * Copyright (C) 2017 The Android Open Source Project
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
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.NoMatchingRootException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link androidx.test.espresso.base.RootViewPicker}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class RootViewPickerTest {

  @Rule
  public ActivityTestRule<DelayedDialogActivity> activityTestRule =
      new ActivityTestRule<>(DelayedDialogActivity.class);

  @Test
  public void waitForDialogRootToAppear() {
    activityTestRule.getActivity().setDelayInMillis(1000 /* 1 sec */);
    waitForRootToAppear();
  }

  @Test
  public void waitForDialogRootToAppear_withTimeout() {
    activityTestRule.getActivity().setDelayInMillis(10000 /* 10 sec */);
    try {
      waitForRootToAppear();
    } catch (NoMatchingRootException nmre) {
      // expected
    }
  }

  private void waitForRootToAppear() {
    onView(withId(R.id.delayed_dialog_btn)).perform(click());
    onView(withText(R.string.dialog_delayed_btn_text_espresso)).inRoot(isDialog()).perform(click());
    onView(withId(R.id.delayed_dialog_selection))
        .check(
            matches(
                withText(
                    activityTestRule
                        .getActivity()
                        .getString(R.string.dialog_delayed_btn_text_espresso))));
  }
}
