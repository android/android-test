/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.accessibility;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.accessibility.AccessibilityChecks.accessibilityAssertion;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.LargeViewActivity;
import androidx.test.ui.app.R;
import com.google.android.apps.common.testing.accessibility.framework.integrations.AccessibilityViewCheckException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** {@link androidx.test.espresso.accessibility.AccessibilityChecks} integration tests. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccessibilityChecksIntegrationTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(LargeViewActivity.class);
    // ViewAction to set the height, width to be too small, which will cause an a11y exception
    onView(withId(R.id.large_view))
        .perform(
            new ViewAction() {
              @Override
              public String getDescription() {
                return "Set view size to 1px by 1px";
              }

              @Override
              public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
              }

              @Override
              public void perform(UiController uiController, View view) {
                view.setLayoutParams(new LayoutParams(1, 1));
              }
            });
  }

  @Test
  public void testRunAccessibilityChecks_viewWithOneError() {
    try {
      onView(withId(R.id.large_view)).check(accessibilityAssertion());
    } catch (AccessibilityViewCheckException e) {
      assertEquals(1, e.getResults().size());
      return;
    }
    fail("Should have thrown an AccessibilityViewCheckException for a small touch target.");
  }

  @Test
  public void testRunAccessibilityChecks_viewWithTwoErrors() {
    // ViewAction to give view a URLSpan with an empty URL, the second accessibility error
    onView(withId(R.id.large_view))
        .perform(
            new ViewAction() {
              @Override
              public String getDescription() {
                return "Set text to include URLSpan with null url";
              }

              @Override
              public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
              }

              @Override
              public void perform(UiController uiController, View view) {
                SpannableString spannableString = new SpannableString("Some text");
                spannableString.setSpan(new URLSpan(""), 0, 1, Spanned.SPAN_COMPOSING);
                ((TextView) view).setText(spannableString);
              }
            });
    try {
      onView(withId(R.id.large_view)).check(accessibilityAssertion());
    } catch (AccessibilityViewCheckException e) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android O and above provides support for URL/ClickableSpans for accessibility
        assertEquals(1, e.getResults().size());
      } else {
        assertEquals(2, e.getResults().size());
      }
      return;
    }
    fail("Should have thrown an AccessibilityViewCheckException for a small touch target.");
  }

  @Test
  public void testCheckWithNonNullMatchingViewException_throwsNoMatchingViewException() {
    try {
      onView(withText("There is no view with this text!")).check(accessibilityAssertion());
      fail("Should have thrown a NoMatchingViewException");
    } catch (NoMatchingViewException expected) {
    }
  }
}
