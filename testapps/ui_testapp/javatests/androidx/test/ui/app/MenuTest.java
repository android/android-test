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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressMenuKey;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Ensures view root ordering works properly. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MenuTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(MenuActivity.class);
  }

  // popup menus are post honeycomb.
  @Test
  @SdkSuppress(minSdkVersion = 11)
  public void testPopupMenu() {
    onView(withText(R.string.popup_item_1_text)).check(doesNotExist());
    onView(withId(R.id.popup_button)).perform(click());
    onView(withText(R.string.popup_item_1_text)).check(matches(isDisplayed())).perform(click());

    onView(withId(R.id.text_menu_result)).check(matches(withText(R.string.popup_item_1_text)));
  }

  @Test
  public void testContextMenu() {
    onView(withText(R.string.context_item_2_text)).check(doesNotExist());
    onView(withId(R.id.text_context_menu)).perform(longClick());
    onView(withText(R.string.context_item_2_text)).check(matches(isDisplayed())).perform(click());

    onView(withId(R.id.text_menu_result)).check(matches(withText(R.string.context_item_2_text)));
  }

  @Test
  public void testOptionMenu() {
    onView(withText(R.string.options_item_3_text)).check(doesNotExist());
    onView(isRoot()).perform(pressMenuKey());
    onView(withText(R.string.options_item_3_text)).check(matches(isDisplayed())).perform(click());

    onView(withId(R.id.text_menu_result)).check(matches(withText(R.string.options_item_3_text)));
  }
}
