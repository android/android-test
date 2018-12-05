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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.FlakyTest;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.Suppress;
import androidx.test.ui.app.MainActivity;
import androidx.test.ui.app.R;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Integration tests for {@link KeyEventAction}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class KeyEventActionIntegrationTest {

  @Rule public ExpectedException expectedException = none();

  /**
   * Test only passes if run in isolation. Unless Gradle supports a single instrumentation per test
   * this test is ignored"
   */
  @Suppress
  @Test
  public void clickBackOnRootAction() {
    ActivityScenario.launch(MainActivity.class);
    expectedException.expect(NoActivityResumedException.class);
    pressBack();
  }

  @Test
  public void clickBackOnNonRootActivityLatte() {
    ActivityScenario.launch(MainActivity.class);
    onData(allOf(instanceOf(Map.class), hasValue("LargeViewActivity"))).perform(click());
    pressBack();

    // Make sure we are back.
    onData(allOf(instanceOf(Map.class), hasValue("LargeViewActivity")))
        .check(matches(isDisplayed()));
  }

  @Test
  public void clickBackOnNonRootActionNoLatte() {
    ActivityScenario.launch(MainActivity.class);
    onData(allOf(instanceOf(Map.class), hasValue("LargeViewActivity"))).perform(click());
    onView(isRoot()).perform(ViewActions.pressBack());

    // Make sure we are back.
    onData(allOf(instanceOf(Map.class), hasValue("LargeViewActivity")))
        .check(matches(isDisplayed()));
  }

  @SuppressWarnings("unchecked")
  @SdkSuppress(minSdkVersion = 10)
  @FlakyTest
  public void clickOnBackFromFragment() {
    Intent fragmentStack =
        new Intent().setClassName(getApplicationContext(), "androidx.test.ui.app.FragmentStack");
    fragmentStack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    ActivityScenario.launch(MainActivity.class);
    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#1"))));
    try {
      pressBack();
      fail("Should have thrown NoActivityResumedException");
    } catch (NoActivityResumedException ignored) {
      // expected
    }
    ActivityScenario.launch(MainActivity.class);

    onView(withId(R.id.new_fragment)).perform(click()).perform(click()).perform(click());

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#4"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#3"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#2"))));

    pressBack();

    onView(allOf(withParent(withId(R.id.simple_fragment)), isAssignableFrom(TextView.class)))
        .check(matches(withText(containsString("#1"))));

    try {
      pressBack();
      fail("Should have thrown NoActivityResumedException");
    } catch (NoActivityResumedException ignored) {
      // expected
    }
  }

  @Test
  public void pressKeyWithKeyCode() {
    ActivityScenario.launch(MainActivity.class);
    onData(allOf(instanceOf(Map.class), hasValue("SendActivity"))).perform(click());
    onView(withId(R.id.enter_data_edit_text)).perform(click());
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_X));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_Y));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_Z));
    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));
    onView(allOf(withId(R.id.enter_data_response_text), withText("xyz")))
        .check(matches(isDisplayed()));
  }
}
