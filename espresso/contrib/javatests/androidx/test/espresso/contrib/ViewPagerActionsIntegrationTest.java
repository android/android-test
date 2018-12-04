/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import static androidx.test.espresso.contrib.ViewPagerActions.scrollLeft;
import static androidx.test.espresso.contrib.ViewPagerActions.scrollRight;
import static androidx.test.espresso.contrib.ViewPagerActions.scrollToFirst;
import static androidx.test.espresso.contrib.ViewPagerActions.scrollToLast;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.ViewPagerActivity;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link ViewPagerActions}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public final class ViewPagerActionsIntegrationTest {

  @Rule
  public ActivityScenarioRule<ViewPagerActivity> activityTestRule =
      new ActivityScenarioRule<>(ViewPagerActivity.class);

  @Test
  public void scrollRightThenLeft() {
    testScrollRightThenLeft(false);
  }

  @Test
  public void scrollRightThenLeft_smooth() {
    testScrollRightThenLeft(true);
  }

  private static void testScrollRightThenLeft(boolean smoothScroll) {
    onPager()
        .check(matches(isShowingPage(0)))
        .perform(scrollRight(smoothScroll))
        .check(matches(isShowingPage(1)))
        .perform(scrollLeft(smoothScroll))
        .check(matches(isShowingPage(0)));
  }

  @Test
  public void scrollToLastThenFirst() {
    testScrollToLastThenFirst(false);
  }

  @Test
  public void scrollToLastThenFirst_smooth() {
    testScrollToLastThenFirst(true);
  }

  private static void testScrollToLastThenFirst(boolean smoothScroll) {
    onPager()
        .check(matches(isShowingPage(0)))
        .perform(scrollToLast(smoothScroll))
        .check(matches(isShowingPage(2)))
        .perform(scrollToFirst(smoothScroll))
        .check(matches(isShowingPage(0)));
  }

  @Test
  public void scrollToPage() {
    testScrollToPage(false);
  }

  @Test
  public void scrollToPage_smooth() {
    testScrollToPage(true);
  }

  private static void testScrollToPage(boolean smoothScroll) {
    onPager()
        .check(matches(isShowingPage(0)))
        .perform(ViewPagerActions.scrollToPage(2, smoothScroll))
        .check(matches(isShowingPage(2)))
        .perform(ViewPagerActions.scrollToPage(1, smoothScroll))
        .check(matches(isShowingPage(1)));
  }

  private static ViewInteraction onPager() {
    return onView(withId(R.id.pager_layout));
  }

  private static Matcher<? super View> isShowingPage(int index) {
    return ViewMatchers.hasDescendant(ViewMatchers.withText("Position #" + index));
  }
}
