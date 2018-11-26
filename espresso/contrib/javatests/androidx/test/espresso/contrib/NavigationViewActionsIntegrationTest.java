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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.close;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.contrib.NavigationViewActions.navigateTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.core.view.GravityCompat;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.ui.app.NavigationViewActivity;
import androidx.test.ui.app.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link NavigationViewActions}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class NavigationViewActionsIntegrationTest {

  private static final int NUM_NAVIGATION_ITEMS = 9;

  @Rule
  public ActivityTestRule<NavigationViewActivity> activityTestRule =
      new ActivityTestRule<NavigationViewActivity>(NavigationViewActivity.class);

  @Test
  public void clickOnAllNavigationItems_DisplaysCorrespondingItemText() {
    for (int i = 1; i <= NUM_NAVIGATION_ITEMS; i++) {
      onView(withId(R.id.drawer_layout)).perform(open(GravityCompat.START));

      int navItemViewId =
          InstrumentationRegistry.getTargetContext()
              .getResources()
              .getIdentifier(
                  "navigation_view_menu_item" + i,
                  "id",
                  InstrumentationRegistry.getTargetContext().getPackageName());

      onView(withId(R.id.nav_view)).perform(navigateTo(navItemViewId));

      int navItemStringId =
          InstrumentationRegistry.getTargetContext()
              .getResources()
              .getIdentifier(
                  "navigation_view_item" + i,
                  "string",
                  InstrumentationRegistry.getTargetContext().getPackageName());

      String expectedItemText =
          InstrumentationRegistry.getTargetContext().getString(navItemStringId);
      onView(withId(R.id.selected_nav_item_text_view)).check(matches(withText(expectedItemText)));

      // Force closing of drawer
      onView(withId(R.id.drawer_layout)).perform(close());
    }
  }
}
