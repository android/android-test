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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.view.InputDevice;
import android.view.MotionEvent;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.ui.app.LargeViewActivity;
import androidx.test.ui.app.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link GeneralClickAction} on a large view. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ClickActionIntegrationTest {

  @Rule
  public ActivityScenarioRule<LargeViewActivity> rule =
      new ActivityScenarioRule<>(LargeViewActivity.class);

  @Test
  public void clickActionTesting() {
    onView(withId(R.id.large_view)).check(matches(withText("large view")));
    onView(withId(R.id.large_view)).perform(click());
    onView(withId(R.id.large_view)).check(matches(withText("Ouch!!!")));
  }

  // Right-click support does not exist pre API_14
  @SdkSuppress(minSdkVersion = 14)
  @Test
  public void rightClickTest() {
    onView(withId(R.id.large_view))
        .perform(click(InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY));
    onView(withText(R.string.context_item_1_text)).check(matches(isDisplayed()));
    onView(withText(R.string.context_item_2_text)).check(matches(isDisplayed()));
    onView(withText(R.string.context_item_3_text)).check(matches(isDisplayed()));
  }

  @Test
  @SdkSuppress(maxSdkVersion = 13)
  public void rightClickTest_unsupportedApiLevel() {
    boolean exceptionThrown = false;
    try {
      onView(withId(R.id.large_view)).perform(click(0, 0));
    } catch (UnsupportedOperationException e) {
      exceptionThrown = true;
    } finally {
      assertTrue(exceptionThrown);
    }
  }
}
