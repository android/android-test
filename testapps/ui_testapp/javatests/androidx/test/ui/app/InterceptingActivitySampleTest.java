/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.intercepting.SingleActivityFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Demonstrates the usage of {@link ActivityTestRule#ActivityTestRule(SingleActivityFactory,
 * boolean, boolean)} to intercept methods of Activity and/or its parent classes.
 */
@RunWith(AndroidJUnit4.class)
public class InterceptingActivitySampleTest {

  private Intent broadcastIntent;

  @Rule
  public ActivityTestRule<InterceptingActivity> activityTestRule =
      new ActivityTestRule<>(getActivityFactory(), true, false);

  @Before
  public void setUp() throws Exception {
    broadcastIntent = null;
  }

  @Test
  public void shouldSendBroadcastOnButtonClick() throws Exception {
    activityTestRule.launchActivity(null);

    onView(withId(R.id.btn_send_broadcast)).perform(click());
    getInstrumentation().waitForIdleSync();

    assertThat(broadcastIntent, hasAction("A_CUSTOM_ACTION"));
  }

  @NonNull
  private SingleActivityFactory<InterceptingActivity> getActivityFactory() {
    return new SingleActivityFactory<InterceptingActivity>(InterceptingActivity.class) {
      @Override
      public InterceptingActivity create(Intent intent) {
        return new InterceptingActivity() {
          @Override
          public void sendBroadcast(Intent intent) {
            broadcastIntent = intent;
          }
        };
      }
    };
  }
}
