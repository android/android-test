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

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import android.app.Activity;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ActivityResultTest {
  @Rule public ActivityTestRule<PickActivity> rule = new ActivityTestRule<>(PickActivity.class);

  @Test
  public void getResultOkFromPickActivity() {
    onView(withText(R.string.button_pick_number)).perform(click());
    assertThat(rule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
    assertThat(
        rule.getActivityResult(),
        hasResultData(IntentMatchers.hasExtraWithKey(PickActivity.EXTRA_PICKED_NUMBER)));
  }

  @Test
  public void getResultCancelFromPickActivity() {
    onView(withText(android.R.string.cancel)).perform(click());
    assertThat(rule.getActivityResult(), hasResultCode(Activity.RESULT_CANCELED));
    assertThat(rule.getActivityResult().getResultData(), is(nullValue()));
  }

  @Test
  public void verifyAppInBackground() throws InterruptedException {
    // the ActivityTestRule grantees the activity is started before the test

    // press back to go back to home screen, ie app in background
    pressBackUnconditionally();

    // Since we have the instance of the original activity under test, relaunch it.
    rule.launchActivity(null);

    onView(withText(R.string.button_pick_number)).perform(click());
  }

  @Test
  public void getResultAfterManuallyFinishingTheActivity() {
    onView(withText(R.string.button_set_number)).perform(click());
    rule.finishActivity();
    assertThat(rule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
    assertThat(
        rule.getActivityResult(),
        hasResultData(IntentMatchers.hasExtraWithKey(PickActivity.EXTRA_SET_NUMBER)));
  }

  @Test
  public void getResultBeforeFinishing() {
    try {
      rule.getActivityResult();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // expected IllegalStateException exception to be thrown
    }
  }
}
