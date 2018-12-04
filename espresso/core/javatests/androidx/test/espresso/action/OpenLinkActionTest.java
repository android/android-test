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

package androidx.test.espresso.action;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.openLinkWithText;
import static androidx.test.espresso.action.ViewActions.openLinkWithUri;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasHost;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.rules.ExpectedException.none;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** {@link OpenLinkAction} integration tests. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class OpenLinkActionTest {

  @Rule
  public ActivityScenarioRule<SendActivity> rule = new ActivityScenarioRule<>(SendActivity.class);

  @Rule public ExpectedException expectedException = none();

  @Before
  public void setUp() {
    Intents.init();
    // Stubbing to block all external intents
    intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, null));
  }

  @After
  public void tearDown() throws Exception {
    Intents.release();
  }

  @Test
  public void openLink_TargetViewNotSpanned() {
    expectedException.expect(PerformException.class);
    expectedException.expectCause(
        new CustomTypeSafeMatcher<Throwable>("message contains has-links=false") {
          @Override
          protected boolean matchesSafely(Throwable throwable) {
            return throwable.getMessage().contains("has-links=false");
          }
        });
    onView(withId(R.id.send_title)).perform(scrollTo(), openLinkWithText("altavista.com"));
  }

  @Test
  public void testOpenLink_NoLinkFound() {
    expectedException.expect(PerformException.class);
    expectedException.expectCause(
        new CustomTypeSafeMatcher<Throwable>("message contains has-links=false") {
          @Override
          protected boolean matchesSafely(Throwable throwable) {
            return throwable.getMessage().contains("bacon");
          }
        });
    onView(withId(R.id.spanned)).perform(scrollTo(), openLinkWithText("bacon"));
  }

}
