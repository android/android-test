/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.multiprocess.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Contains integration type tests since the {@link MainActivity} is just a dynamic list of all
 * {@link android.app.Activity Activities} defined in the manifest.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class MainActivityTest {
  private static final String TAG = "MainActivityTest";

  private static final String MAIN_PROC_NAME = "androidx.test.multiprocess.app";

  @Rule
  public ActivityTestRule<MainActivity> rule =
      new ActivityTestRule<>(MainActivity.class);

  @Test
  public void verifySynchronizingAgainstCustomIdlingResourceInRemoteProcessIsSuccessful()
      throws InterruptedException {

    Log.i(TAG, "About to click on IdlingActivity..");
    onView(withText("IdlingActivity")).perform(click());

    // Type text and then press the button.
    onView(withId(R.id.editTextUserInput))
        .perform(typeText("Hello Multiprocess Espresso!"), closeSoftKeyboard());
    onView(withId(R.id.changeTextBtn)).perform(click());

    // Check that the text was changed.
    onView(withId(R.id.textToBeChanged)).check(matches(withText("Hello Multiprocess Espresso!")));

    Log.d(TAG, "Checking private process name...");
    onView(withId(R.id.textIdlingProcessName)).check(matches(withText(MAIN_PROC_NAME + ":idling")));
  }
}
