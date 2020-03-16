/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.intent;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasHost;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ResettingStubberImpl}. */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ResettingStubberImplTest {

  private ResettingStubber resettingStubber;

  @Before
  public void setUp() throws Exception {
    resettingStubber = new ResettingStubberImpl();
    resettingStubber.initialize();
  }

  @UiThreadTest
  @Test
  public void isInitialized() {
    assertTrue(resettingStubber.isInitialized());

    resettingStubber.reset();
    assertFalse(resettingStubber.isInitialized());

    resettingStubber.initialize();
    assertTrue(resettingStubber.isInitialized());
  }

  @UiThreadTest
  @Test
  public void reset_getActivityResultForIntent() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    resettingStubber.setActivityResultForIntent(any(Intent.class), result);
    assertEquals(resettingStubber.getActivityResultForIntent(intent), result);

    resettingStubber.reset();
    resettingStubber.initialize();
    assertEquals(null, resettingStubber.getActivityResultForIntent(intent));
  }

  @UiThreadTest
  @Test
  public void reset_getActivityResultFunctionForIntent() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    resettingStubber.setActivityResultFunctionForIntent(any(Intent.class), unused -> result);
    assertEquals(resettingStubber.getActivityResultForIntent(intent), result);

    resettingStubber.reset();
    resettingStubber.initialize();
    assertEquals(null, resettingStubber.getActivityResultForIntent(intent));
  }

  @UiThreadTest
  @Test
  public void setActivityResultForIntent() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    resettingStubber.setActivityResultForIntent(
        allOf(hasAction(equalTo(Intent.ACTION_VIEW)), hasData(hasHost(equalTo("www.android.com")))),
        result);
    assertEquals(
        "Activity Result Not Equal.", resettingStubber.getActivityResultForIntent(intent), result);
  }

  @UiThreadTest
  @Test
  public void setActivityResultFunctionForIntent() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    resettingStubber.setActivityResultFunctionForIntent(
        allOf(hasAction(equalTo(Intent.ACTION_VIEW)), hasData(hasHost(equalTo("www.android.com")))),
        unused -> result);
    assertEquals(
        "Activity Result Not Equal.", resettingStubber.getActivityResultForIntent(intent), result);
  }

  @UiThreadTest
  @Test
  public void setActivityResultMultipleTime() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    ActivityResult duplicateResult =
        new ActivityResult(100, new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
    Matcher<Intent> matcher =
        allOf(hasAction(equalTo(Intent.ACTION_VIEW)), hasData(hasHost(equalTo("www.android.com"))));
    resettingStubber.setActivityResultForIntent(matcher, result);
    resettingStubber.setActivityResultForIntent(matcher, duplicateResult);
    assertEquals(
        "Activity result didn't match expected value.",
        resettingStubber.getActivityResultForIntent(intent),
        duplicateResult);
    assertEquals(
        "Activity result didn't match expected value.",
        resettingStubber.getActivityResultForIntent(intent),
        duplicateResult);
  }

  @UiThreadTest
  @Test
  public void setActivityResultFunctionMultipleTime() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com"));
    ActivityResult result = new ActivityResult(10, intent);
    ActivityResult duplicateResult =
        new ActivityResult(100, new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
    Matcher<Intent> matcher =
        allOf(hasAction(equalTo(Intent.ACTION_VIEW)), hasData(hasHost(equalTo("www.android.com"))));
    resettingStubber.setActivityResultFunctionForIntent(matcher, unused -> result);
    resettingStubber.setActivityResultFunctionForIntent(matcher, unused -> duplicateResult);
    assertEquals(
        "Activity result didn't match expected value.",
        resettingStubber.getActivityResultForIntent(intent),
        duplicateResult);
    assertEquals(
        "Activity result didn't match expected value.",
        resettingStubber.getActivityResultForIntent(intent),
        duplicateResult);
  }
}
