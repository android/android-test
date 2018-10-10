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

package androidx.test.espresso.contrib;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.close;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.DrawerActivity;
import androidx.test.ui.app.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link DrawerActions}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DrawerActionsIntegrationTest {

  static final String EXTRA_DATA = "androidx.test.ui.app.DATA";
  private ActivityScenario<DrawerActivity> activityScenario;

  @Before
  public void setUp() throws Exception {
    activityScenario = launch(DrawerActivity.class);
  }

  @Test
  public void testOpenAndCloseDrawer() {
    onView(withId(R.id.drawer_layout))
        .check(matches(isClosed())) // Drawer should not be open to start.
        .perform(open())
        .check(matches(isOpen())) // The drawer should now be open.
        .perform(close())
        .check(matches(isClosed())); // Drawer should be closed again.
  }

  @Test
  public void testOpenAndCloseDrawer_idempotent() {
    onView(withId(R.id.drawer_layout))
        .check(matches(isClosed())) // Drawer should not be open to start.
        .perform(open()) // Open drawer repeatedly.
        .perform(open())
        .perform(open())
        .check(matches(isOpen())) // The drawer should be open.
        .perform(close()) // Close drawer repeatedly.
        .perform(close())
        .perform(close())
        .check(matches(isClosed())); // Drawer should be closed.
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testOpenDrawer_clickItem() {
    onView(withId(R.id.drawer_layout)).perform(open());

    // Click an item in the drawer.
    int rowIndex = 2;
    String rowContents = DrawerActivity.DRAWER_CONTENTS[rowIndex];
    onData(allOf(is(instanceOf(String.class)), is(rowContents))).perform(click());

    // clicking the item should close the drawer.
    onView(withId(R.id.drawer_layout)).check(matches(isClosed()));

    // The text view will now display "You picked: Pickle"
    onView(withId(R.id.drawer_text_view)).check(matches(withText("You picked: " + rowContents)));
  }

  // Don't use swipeLeft() to close an open drawer. Since we unregister the IdlingDrawerListener
  // after the drawer is open, a left swipe to close the drawer is not synchronized and will result
  // in a flaky test. Therefore this test is commented out.
  //
  //  public void testOpenDrawer_closeBySwipeLeft() {
  //    onView(withId(R.id.drawer_layout))
  //        .perform(open())
  //        .perform(swipeLeft()) // Swipe left to close drawer.
  //        .check(matches(isClosed()) // Drawer should be closed.
  //  }

  @Test
  public void testOpenDrawer_startNewActivity() {
    onView(withId(R.id.drawer_layout)).perform(open());

    Intent displayActivity =
        new Intent()
            .setClassName(
                getInstrumentation().getTargetContext(),
                "androidx.test.ui.app.DisplayActivity");
    displayActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    displayActivity.putExtra(EXTRA_DATA, "Have a cup of Espresso.");
    getInstrumentation().startActivitySync(displayActivity);

    onView(withId(R.id.display_data)).check(matches(withText("Have a cup of Espresso.")));

    // Going back to the previous activity
    pressBack();

    // The drawer should still be open.
    onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
  }

  @Test
  public void testOpenDrawer_rotateScreen() {
    onView(withId(R.id.drawer_layout)).perform(open());

    // Rotate to landscape.
    setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    // The drawer should still be open.
    onView(withId(R.id.drawer_layout)).check(matches(isOpen()));

    // Rotate to portrait.
    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // The drawer should still be open.
    onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
  }

  private void setOrientation(final int orientation) {
    activityScenario.onActivity(activity -> activity.setRequestedOrientation(orientation));
  }
}
