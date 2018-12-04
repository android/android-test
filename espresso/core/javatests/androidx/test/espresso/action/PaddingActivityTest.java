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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.PaddingActivity;
import androidx.test.ui.app.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests padding within a scrollview. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class PaddingActivityTest {

  @Rule
  public ActivityScenarioRule<PaddingActivity> rule =
      new ActivityScenarioRule<>(PaddingActivity.class);

  @Test
  public void textViewWithHugePadding() {
    onView(withId(R.id.view_with_padding)).perform(scrollTo(), typeText("Hello World"));
  }
}
