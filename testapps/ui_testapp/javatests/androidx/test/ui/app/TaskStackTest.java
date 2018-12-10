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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.test.ActivityInstrumentationTestCase2;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.filters.SdkSuppress;

public class TaskStackTest extends ActivityInstrumentationTestCase2<TaskStackActivity> {

  public TaskStackTest() {
    super(TaskStackActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  // The task stack behavior is available on API level 11 and up.
  @SdkSuppress(minSdkVersion = 11)
  public void testTaskStack() {
    onView(withText("display activity")).check(matches(isDisplayed()));
    pressBack();
    onView(withText("tool bar activity")).check(matches(isDisplayed()));
    pressBack();
    onView(withText("drawer activity")).check(matches(isDisplayed()));
  }


  // The task stack behavior is available on API level 11 and up.
  @SdkSuppress(minSdkVersion = 11)
  public void testBackUnconditionallyExitsAppAndDoesNotThrow() {
    onView(withText("display activity")).check(matches(isDisplayed()));
    pressBack();
    pressBack();
    pressBackUnconditionally();
  }
}
