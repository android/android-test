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
import static androidx.test.espresso.action.ViewActions.openLink;
import static androidx.test.espresso.action.ViewActions.openLinkWithText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.net.Uri;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Sample test that demonstrates the use of Intento as well as the openLink action. */
@RunWith(AndroidJUnit4.class)
public class IntentTest {

  @Before
  public void setUp() throws Exception {
    // Espresso will not launch our activity for us, we must launch it via ActivityScenario.launch.
    ActivityScenario.launch(SendActivity.class);
    // Once initialized, Intento will begin recording and providing stubbing for intents.
    Intents.init();
    // Stubbing to block all external intents
    intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, null));
  }

  @After
  public void tearDown() throws Exception {
    // Releasing clears recorded intents and stubbing, ensuring a clean state for each test.
    Intents.release();
  }

  @Test
  public void testOpenLink() {
    // Basic usage of the openLink action
    onView(withId(R.id.spanned)).perform(scrollTo(), openLinkWithText("altavista.com"));
    // A simple Intento validation - verify an intent with our data was sent by opening the link.
    intended(hasData("http://altavista.com"));

    // You can also pass both a Matcher<String> and Matcher<Uri> to the openLink action.
    onView(withId(R.id.spanned)).perform(scrollTo(),
        openLink(containsString("google"), is(Uri.parse("http://www.google.com"))));
    // toPackage validates that intents from the actions above would get resolved to the browser.
    // depending on the device, you may have a different browser.
    intended(
        anyOf(
            toPackage("com.android.browser"),
            toPackage("com.android.chrome"),
            toPackage("org.chromium.webview_shell")),
        times(2));
  }
}
