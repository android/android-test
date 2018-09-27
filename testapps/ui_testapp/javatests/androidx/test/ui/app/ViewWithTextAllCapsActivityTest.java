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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for {@link androidx.test.espresso.matcher.ViewMatchers}, that use transformed text and
 * original text to match target UI, the match will succeed when any of the conditions get
 * satisfied.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewWithTextAllCapsActivityTest {
  @Rule
  public ActivityTestRule<ViewWithTextAllCapsActivity> rule =
      new ActivityTestRule<>(ViewWithTextAllCapsActivity.class);

  @Test
  public void testAllCapsTextView() {
    onView(withId(R.id.text_with_all_caps)).check(matches(withText("CAMEL CASE TEXT")));
  }

  @Test
  public void testMatchDefaultTextView() {
    onView(withId(R.id.text_without_all_caps)).check(matches(withText("Camel Case Text")));
  }

  @Test
  public void testMatchWithOriginalText() {
    onView(withId(R.id.text_with_all_caps)).check(matches(withText("Camel Case Text")));
  }
}
