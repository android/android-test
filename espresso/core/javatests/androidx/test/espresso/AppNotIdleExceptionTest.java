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

package androidx.test.espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.fail;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SyncActivity;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test case for {@link AppNotIdleException}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AppNotIdleExceptionTest {

  @Rule
  public ActivityScenarioRule<SyncActivity> rule = new ActivityScenarioRule<>(SyncActivity.class);

  @Before
  public void setUp() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS);
  }

  @After
  public void tearDown() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
  }

  @Test
  public void appIdleException() throws Exception {
    final AtomicBoolean continueBeingBusy = new AtomicBoolean(true);
    try {
      final Handler handler = new Handler(Looper.getMainLooper());
      Runnable runnable =
          new Runnable() {
            @Override
            public void run() {
              if (!continueBeingBusy.get()) {
                return;
              } else {
                handler.post(this);
              }
            }
          };
      FutureTask<Void> task = new FutureTask<Void>(runnable, null);
      handler.post(task);
      task.get(); // Will Make sure that the first post is sent before we do a lookup.
      // Request the "hello world!" text by clicking on the request button.
      onView(withId(R.id.request_button)).perform(click());
      fail("Espresso failed to throw AppNotIdleException");
    } catch (AppNotIdleException expected) {
      // Do Nothing. Test pass.
    } finally {
      continueBeingBusy.getAndSet(false);
    }
  }
}
