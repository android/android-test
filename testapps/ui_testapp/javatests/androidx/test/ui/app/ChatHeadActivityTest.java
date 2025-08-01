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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isFocusable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChatHeadActivityTest {
  private static final String TAG = "ChatHeadActivityTest";

  @Rule
  public ActivityTestRule<ChatHeadActivity> activityTestRule =
      new ActivityTestRule<>(ChatHeadActivity.class);

  private Activity activity;

  @Before
  public void setUp() {
    // Espresso will not launch our activity for us, we must launch it via getActivity().
    activity = activityTestRule.getActivity();
  }

  @After
  public void tearDown() {
  }

  /** Helper method to click on the chat head */
  private void clickOnChatHead() {
    onView(withId(R.id.chat_head_btn_id))
        .inRoot(
            withDecorView(
                allOf(
                    not(is(activity.getWindow().getDecorView())),
                    not(isFocusable())
                )
            )
        )
        .perform(click());
  }
}
