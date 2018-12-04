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
 *
 *
 */

package androidx.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

/**
 * Since {@link UiThreadTest} handling was moved to the core Android runner there is no longer an
 * easy way to test it with local classes.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AndroidTimeoutUiTest {
  @Rule public Timeout globalTimeout = new Timeout(100);

  private static final ActivityFixture mockActivity = mock(ActivityFixture.class);

  public static class ActivityFixture extends Activity {}

  @Rule
  public ActivityTestRule<ActivityFixture> activityRule =
      new ActivityTestRule<ActivityFixture>(ActivityFixture.class) {
        @Override
        public ActivityFixture launchActivity(Intent startIntent) {
          return mockActivity;
        }
      };

  @Before
  @UiThreadTest
  public void beforeOnUiThread() {
    verifyRunsOnUiThread();
  }

  @Before
  public void beforeNotOnUiThread() {
    verifyRunsNotOnUiThread();
  }

  @After
  @UiThreadTest
  public void afterOnUiThread() {
    verifyRunsOnUiThread();
  }

  @After
  public void afterNotOnUiThread() {
    verifyRunsNotOnUiThread();
  }

  @Test
  @UiThreadTest
  public void verifyOnUiThread() throws Exception {
    verifyRunsOnUiThread();
  }

  @Test(timeout = 1000)
  @UiThreadTest
  public void verifyOnUiThreadWhenTimeoutUsed() {
    // Create a new handler to check if we are actually running on the UI thread
    new Handler();
    verifyRunsOnUiThread();
  }

  @Test(timeout = 100, expected = RuntimeException.class)
  public void attemptingToCreateHandlerNotOnUiThreadThrows() {
    verifyRunsNotOnUiThread();
    // Creating a new handler outside of the UI thread will throw an exception
    new Handler();
  }

  @Test
  @android.test.UiThreadTest
  public void verifyOnUiThreadWhenDeprecatedAnnotationIsUsed() {
    verifyRunsOnUiThread();
  }

  @Test(timeout = 100)
  @android.test.UiThreadTest
  public void verifyOnUiThreadWhenDeprecatedAnnotationIsUsedAndTimeoutIsUsed() {
    verifyRunsOnUiThread();
  }

  private static void verifyRunsOnUiThread() {
    assertTrue(
        "Not running on the UI Thread",
        Looper.getMainLooper().getThread() == Thread.currentThread());
    assertTrue("Not running on the UI Thread", Looper.myLooper() == Looper.getMainLooper());
  }

  private static void verifyRunsNotOnUiThread() {
    assertFalse(
        "Running on the UI Thread", Looper.getMainLooper().getThread() == Thread.currentThread());
    assertFalse("Running on the UI Thread", Looper.myLooper() == Looper.getMainLooper());
  }
}
