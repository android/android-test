/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.pressMenuKey;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.os.Build;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.accessibility.AccessibilityChecks;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.google.android.apps.common.testing.accessibility.framework.integrations.AccessibilityViewCheckException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Demonstrates use of {@link AccessibilityChecks}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccessibilityChecksTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(LargeViewActivity.class);

    // ViewAction to set the height, width to be too small, which will cause an a11y exception
    onView(withId(R.id.large_view)).perform(new ViewAction() {
        @Override
        public String getDescription() {
          return "Set height, width to 0";
        }

        @Override
        public Matcher<View> getConstraints() {
          return isAssignableFrom(View.class);
        }

        @Override
        public void perform(UiController uiController, View view) {
          // set size to be tiny to cause an accessibility error
          view.setLayoutParams(new LayoutParams(1, 1));
        }
      });
  }

  /**
   * Demonstrates how to run Accessibility Checks by default on all actions contained in {@link
   * ViewActions}
   */
  @Test
  public void testRunAccessibilityChecks_usingPressMenuKey() {
    // will not run accessibility checks (off by default), so no error will be thrown
    onView(withId(R.id.large_view)).perform(pressMenuKey());

    // accessibility checks will be run, so there will be an error thrown for a small touch target
    AccessibilityChecks.enable();
    try {
      onView(withId(R.id.large_view)).perform(actionWithAssertions(pressMenuKey()));
      fail("Should have thrown an exception for a small touch target");
    } catch (AccessibilityViewCheckException e) {
      assertEquals(1, e.getResults().size());
    }
  }

  /**
   * Demonstrates how to run global assertions, and in particular Accessibility Checks, before
   * custom actions.
   */
  @Test
  public void testRunAccessibilityChecks_usingCustomAction() {
    // could be any custom action, this one does nothing
    ViewAction customAction = new ViewAction() {
        @Override
        public String getDescription() {
          return "nothing";
        }

        @Override
        public Matcher<View> getConstraints() {
          return isAssignableFrom(View.class);
        }

        @Override
        public void perform(UiController uiController, View view) {
          // this action does nothing
        }
      };

    // will not run accessibility checks (off by default), so no error will be thrown
    onView(withId(R.id.large_view)).perform(actionWithAssertions(customAction));

    // accessibility checks will be run, so there will be an error thrown for a small touch target
    AccessibilityChecks.enable();
    try {
      onView(withId(R.id.large_view)).perform(actionWithAssertions(customAction));
      fail("Should have thrown an exception for a small touch target");
    } catch (AccessibilityViewCheckException e) {
      assertEquals(1, e.getResults().size());
    }
  }

  /**
   * Demonstrates how to run Accessibility Checks with a custom configuration, in this case with a
   * suppressing result matcher to suppress the error.
   */
  @Test
  public void testRunAccessibilityChecks_usingCustomConfiguration() {
    AccessibilityChecks.enable()
    // set the suppressing result matcher, which could be used to suppress known bugs
        .setSuppressingResultMatcher(matchesViews(withId(R.id.large_view)))
    // set the checks to check the whole screen, not just the view being acted upon
        .setRunChecksFromRootView(true);
    try {
      onView(withId(R.id.large_view)).perform(actionWithAssertions(pressMenuKey()));
    } catch (AccessibilityViewCheckException e) {
      // There are framework bugs for APIs 16 and below that cause extra errors.
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
        fail(String.format("Exception should have been suppressed. %1$s", e.toString()));
      }
    }
  }

  @Test
  public void enableIsIdempotent() {
    AccessibilityChecks.enable();
    AccessibilityChecks.enable();
  }
}
