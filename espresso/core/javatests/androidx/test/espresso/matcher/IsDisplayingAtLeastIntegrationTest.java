/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.test.espresso.matcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.MatcherTestUtils.getDescription;
import static androidx.test.espresso.matcher.MatcherTestUtils.getMismatchDescription;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.test.core.app.testing.UiActivity;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link ViewMatchers#isDisplayingAtLeast(int)}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IsDisplayingAtLeastIntegrationTest {

  @Rule
  public ActivityScenarioRule<UiActivity> activityScenarioRule =
      new ActivityScenarioRule<>(UiActivity.class);

  @Test
  public void invalidPercentageRange() {
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(-1));
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(101));
  }

  @Test
  public void fullyDisplayed() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              View child = new View(activity);
              childHolder[0] = child;
              activity.setContentView(child, new ViewGroup.LayoutParams(100, 100));
            });

    onView(is(childHolder[0])).check(matches(isDisplayingAtLeast(100)));
  }

  @Test
  public void fullyDisplayed_withScale() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              FrameLayout parent = new FrameLayout(activity);
              parent.setScaleX(0.5f);
              View child = new View(activity);
              childHolder[0] = child;
              child.setScaleY(0.5f);
              parent.addView(child, new FrameLayout.LayoutParams(100, 100));
              activity.setContentView(
                  parent,
                  new ViewGroup.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            });

    onView(is(childHolder[0])).check(matches(isDisplayingAtLeast(100)));
  }

  @Test
  public void partiallyDisplayed() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              FrameLayout parent = new FrameLayout(activity);
              View child = new View(activity);
              childHolder[0] = child;
              parent.addView(child, new FrameLayout.LayoutParams(100, 100));
              activity.setContentView(parent, new ViewGroup.LayoutParams(50, 50));
            });

    // Set the view to be 100x100: 10,000 pixels, parent 50x50: 2,500 pixels (25% visible)
    onView(is(childHolder[0])).check(matches(isDisplayingAtLeast(20)));
    onView(is(childHolder[0])).check(matches(not(isDisplayingAtLeast(30))));
  }

  @Test
  public void partiallyDisplayed_withScale() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              FrameLayout parent = new FrameLayout(activity);
              parent.setScaleY(-0.9f);
              View child = new View(activity);
              childHolder[0] = child;
              child.setScaleX(0.6f);
              parent.addView(child, new FrameLayout.LayoutParams(100, 100));
              activity.setContentView(parent, new ViewGroup.LayoutParams(60, 60));
            });

    // Scaled child: 60x90 = 5,400 pixels (horizontal range [20, 80]),
    // parent 60x60 clips child to 40x54 = 2,160 pixels (40% visible).
    onView(is(childHolder[0])).check(matches(isDisplayingAtLeast(39)));
    onView(is(childHolder[0])).check(matches(not(isDisplayingAtLeast(41))));
  }

  @Test
  public void gone() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              View child = new View(activity);
              childHolder[0] = child;
              child.setVisibility(View.GONE);
              activity.setContentView(child, new ViewGroup.LayoutParams(100, 100));
            });

    onView(is(childHolder[0])).check(matches(not(isDisplayingAtLeast(5))));
  }

  @Test
  public void description() {
    assertThat(
        getDescription(isDisplayingAtLeast(15)),
        is(
            "("
                + getDescription(withEffectiveVisibility(Visibility.VISIBLE))
                + " and view.getGlobalVisibleRect() covers at least <15> percent of the view's"
                + " area)"));
  }

  @Test
  public void mismatchDescription_wrongVisibility() {
    View view = new View(getApplicationContext());
    view.setVisibility(View.GONE);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is(getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view)));
  }

  @Test
  public void mismatchDescription_notVisible() {
    View view = new View(getApplicationContext());
    view.setVisibility(View.VISIBLE);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is("view was <0> percent visible to the user"));
  }

  @Test
  public void mismatchDescription_lowVisibility() {
    View[] childHolder = new View[1];
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              // Set the area of the view to 100x100 = 10,000, parent to 50x50 = 2,500: 25% visible
              FrameLayout parent = new FrameLayout(activity);
              View child = new View(activity);
              childHolder[0] = child;
              parent.addView(child, new FrameLayout.LayoutParams(100, 100));
              activity.setContentView(parent, new ViewGroup.LayoutParams(50, 50));
            });

    onView(is(childHolder[0]))
        .check(
            (view, noViewFoundException) ->
                assertThat(
                    getMismatchDescription(isDisplayingAtLeast(35), view),
                    is("view was <25> percent visible to the user")));
  }
}
