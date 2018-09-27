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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.closeDrawer;
import static androidx.test.espresso.contrib.DrawerActions.openDrawer;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.test.ActivityInstrumentationTestCase2;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;

/**
 * Demonstrates use of {@link DrawerActions}.
 */
@LargeTest
public class DrawerActionsTest extends ActivityInstrumentationTestCase2<DrawerActivity> {

  public DrawerActionsTest() {
    super(DrawerActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  @SdkSuppress(minSdkVersion = 20) // b/26957451
  public void testOpenAndCloseDrawer() {
    // Drawer should not be open to start.
    onView(withId(R.id.drawer_layout)).check(matches(isClosed()));

    openDrawer(R.id.drawer_layout);

    // The drawer should now be open.
    onView(withId(R.id.drawer_layout)).check(matches(isOpen()));

    closeDrawer(R.id.drawer_layout);

    // Drawer should be closed again.
    onView(withId(R.id.drawer_layout)).check(matches(isClosed()));
  }

  @SdkSuppress(minSdkVersion = 20) // b/26957451
  public void testDrawerOpenAndClick() {
    openDrawer(R.id.drawer_layout);

    onView(withId(R.id.drawer_layout)).check(matches(isOpen()));

    // Click an item in the drawer. We use onData because the drawer is backed by a ListView, and
    // the item may not necessarily be visible in the view hierarchy.
    int rowIndex = 2;
    String rowContents = DrawerActivity.DRAWER_CONTENTS[rowIndex];
    onData(allOf(is(instanceOf(String.class)), is(rowContents))).perform(click());

    // clicking the item should close the drawer.
    onView(withId(R.id.drawer_layout)).check(matches(isClosed()));

    // The text view will now display "You picked: Pickle"
    onView(withId(R.id.drawer_text_view)).check(matches(withText("You picked: " + rowContents)));
  }
}
