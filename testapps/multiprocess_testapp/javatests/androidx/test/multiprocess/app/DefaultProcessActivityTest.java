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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.fail;

import android.util.Log;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.remote.RemoteEspressoException;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DefaultProcessActivityTest {
  private static final String TAG = "DefaultProcActivityTest";

  private static final String DEFAULT_PROC_NAME = "androidx.test.multiprocess.app";

  @Rule
  public ActivityTestRule<DefaultProcessActivity> rule =
      new ActivityTestRule<>(DefaultProcessActivity.class);

  @Test
  public void verifyAssertingOnViewInRemoteProcessIsSuccessful() {
    Log.d(TAG, "Checking main process name...");
    onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));

    Log.d(TAG, "Starting activity in a secondary process...");
    onView(withId(R.id.startActivityBtn)).perform(click());

    Log.d(TAG, "Checking private process name...");
    onView(withId(R.id.textPrivateProcessName))
        .check(matches(withText(is(DEFAULT_PROC_NAME + ":PID2"))));
  }

  @Test
  public void verifyFailingAssertionsOnRemoteProcessIsPropagatedBack() {
    Log.d(TAG, "Checking main process name...");
    onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));

    Log.d(TAG, "Starting activity in a secondary process...");
    onView(withId(R.id.startActivityBtn)).perform(click());

    try {
      Log.d(TAG, "Attempting to check a non existing view...");
      int wrongId = 123;
      onView(withId(wrongId)).check(matches(withText(is(DEFAULT_PROC_NAME + ":PID2"))));
      fail("Expected to throw RemoteEspressoException");
    } catch (RemoteEspressoException e) {
      // expected
    }
  }

  @Test
  public void verifyAssertingOnDataInRemoteProcessIsSuccessful() {
    Log.d(TAG, "Checking main process name...");
    onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));

    Log.d(TAG, "Starting activity in a secondary process...");
    onView(withId(R.id.startActivityBtn)).perform(click());

    Log.d(TAG, "Clicking list item in private process activity...");
    onData(allOf(instanceOf(String.class), is("Latte"))).perform(click());

    Log.d(TAG, "Check selected text appears...");
    onView(withId(R.id.selectedListItemText)).check(matches(withText("Selected: Latte")));
  }

  @Test
  public void verifySwitchingBetweenTwoProcessesQuicklyWorks() {
    Log.d(TAG, "Checking main process name...");
    onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));

    launchRemoteActivityAndVerify_PressBack_VerifyOnMainActivity();
    // Lets do it again just for fun.
    launchRemoteActivityAndVerify_PressBack_VerifyOnMainActivity();
    // One more time, are you not entertained?
    launchRemoteActivityAndVerify_PressBack_VerifyOnMainActivity();
  }

  private void launchRemoteActivityAndVerify_PressBack_VerifyOnMainActivity() {
    Log.d(TAG, "Starting activity in a secondary process...");
    onView(withId(R.id.startActivityBtn)).perform(click());

    Log.d(TAG, "Checking private process name...");
    onView(withId(R.id.textPrivateProcessName))
        .check(matches(withText(is(DEFAULT_PROC_NAME + ":PID2"))));

    Log.d(TAG, "Pressing back to go back to the main activity...");
    onView(isRoot()).perform(ViewActions.pressBackUnconditionally());

    Log.d(TAG, "Checking main process name again...");
    onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));
  }
}
