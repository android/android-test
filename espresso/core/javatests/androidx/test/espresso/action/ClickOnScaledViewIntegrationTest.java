/*
 * Copyright (C) 2016 The Android Open Source Project
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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.ui.app.R;
import androidx.test.ui.app.ScaledViewActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ClickOnScaledViewIntegrationTest {

  @Rule
  public final ActivityScenarioRule<ScaledViewActivity> activityRule =
      new ActivityScenarioRule<>(ScaledViewActivity.class);

  @Test
  @SdkSuppress(minSdkVersion = 12)
  public void clickUnscaledView() {
    onView(withId(R.id.scaled_view)).perform(click());
  }

  @Test
  @SdkSuppress(minSdkVersion = 12)
  public void clickToScaleAndClickAgain() {
    onView(withId(R.id.scaled_view)).perform(click());
    onView(withId(R.id.scaled_view)).perform(click());
  }

  // placeholder test to avoid the 'no tests found' error on api 10
  // TODO: yuck, find a better solution
  @Test
  @SdkSuppress(maxSdkVersion = 10)
  public void emptyTest() {}
}
