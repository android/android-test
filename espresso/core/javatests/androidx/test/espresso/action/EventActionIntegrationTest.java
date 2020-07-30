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
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.view.View;
import android.view.ViewConfiguration;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ui.app.GestureActivity;
import androidx.test.ui.app.R;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** UI tests for ClickAction, LongClickAction and DoubleClickAction. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EventActionIntegrationTest {

  @Rule
  public ActivityTestRule<GestureActivity> rule = new ActivityTestRule<>(GestureActivity.class);

  private GestureActivity activity;

  @Before
  public void setUp() throws Exception {
    activity = rule.getActivity();
  }

  @Test
  public void clickTesting() {
    onView(withText(is(activity.getString(R.string.text_click))))
        .check(matches(not(isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(click());
    onView(withId(is(R.id.text_click))).check(matches(isDisplayed()));
    onView(withText(is(activity.getString(R.string.text_click)))).check(matches(isDisplayed()));
  }

  // fails on SDK 30 due to the key dispatching timeout of 5 seconds. see b/162542125
  @SdkSuppress(maxSdkVersion = 29)
  @Test
  public void badClickTesting() {
    onView(withText(is(activity.getString(R.string.text_click))))
        .check(matches(not(isDisplayed())));
    GestureActivity activity = this.activity;
    activity.setTouchDelay(ViewConfiguration.getLongPressTimeout() + 200);

    onView(withId(is(R.id.gesture_area)))
        .perform(
            click(
                new ViewAction() {
                  @Override
                  public String getDescription() {
                    return "Handle tap->longclick.";
                  }

                  @Override
                  public Matcher<View> getConstraints() {
                    return isAssignableFrom(View.class);
                  }

                  @Override
                  public void perform(UiController uiController, View view) {
                    activity.setTouchDelay(0);
                  }
                }));

    onView(withId(is(R.id.text_click))).check(matches(isDisplayed()));
    onView(withText(is(this.activity.getString(R.string.text_click))))
        .check(matches(isDisplayed()));
  }

  @SdkSuppress(minSdkVersion = 15)
  @Test
  public void longClickTesting() {
    onView(withText(is(activity.getString(R.string.text_long_click))))
        .check(matches(not(isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(longClick());
    onView(withId(is(R.id.text_long_click))).check(matches(isDisplayed()));
    onView(withText(is(activity.getString(R.string.text_long_click))))
        .check(matches(isDisplayed()));
  }

  @SdkSuppress(minSdkVersion = 15)
  @Test
  public void doubleClickTesting() {
    onView(withText(is(activity.getString(R.string.text_double_click))))
        .check(matches(not(ViewMatchers.isDisplayed())));
    onView(withId(is(R.id.gesture_area))).perform(doubleClick());
    onView(withId(is(R.id.text_double_click))).check(matches(isDisplayed()));
    onView(withText(is("Double Click"))).check(matches(isDisplayed()));
    onView(withText(is(activity.getString(R.string.text_double_click))))
        .check(matches(isDisplayed()));
  }
}
