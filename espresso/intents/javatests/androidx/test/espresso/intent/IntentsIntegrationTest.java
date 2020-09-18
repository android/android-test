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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasPackageName;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasHost;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasParamWithName;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasParamWithValue;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasPath;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasScheme;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasSchemeSpecificPart;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.rules.ExpectedException.none;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.os.Build;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import junit.framework.AssertionFailedError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Integration tests for {@Intents}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntentsIntegrationTest {

  @Rule
  public IntentsTestRule<SendActivity> mIntentsTestRule = new IntentsTestRule<>(SendActivity.class);

  @Rule public ExpectedException expectedException = none();

  @Before
  public void stubExternalIntents() {
    intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, null));
  }

  @Test
  public void externalIntent_AnyIntentToCall() {
    onView(withId(R.id.send_data_to_call_edit_text))
        .perform(scrollTo(), typeText("11111"), closeSoftKeyboard());
    onView(withId(R.id.send_to_call_button)).perform(click());
    intended(anyIntent());
    assertOnSendActivity();
  }

  @Test
  public void externalIntent_WithResultStubbing_toPackage() {
    Intent resultData = new Intent();
    String phoneNumber = "123-345-6789";
    resultData.putExtra("phone", phoneNumber);
    intending(toPackage("com.android.contacts"))
        .respondWith(new ActivityResult(Activity.RESULT_OK, resultData));
    pick(phoneNumber);
  }

  @Test
  public void externalIntent_WithResultStubbing_hasAction() {
    Intent resultData = new Intent();
    String phoneNumber = "123-345-6789";
    resultData.putExtra("phone", phoneNumber);
    intending(hasAction("android.intent.action.PICK"))
        .respondWith(new ActivityResult(Activity.RESULT_OK, resultData));
    pick(phoneNumber);
  }

  @Test
  public void externalIntents_WithResultStubbingMultiple_toPackage() {
    Intent resultData = new Intent();
    String phoneNumber = "123-345-6789";
    resultData.putExtra("phone", phoneNumber);
    intending(toPackage("com.android.contacts"))
        .respondWith(new ActivityResult(Activity.RESULT_OK, resultData));
    Intent resultData2 = new Intent();
    String phoneNumber2 = "987-654-3210";
    resultData2.putExtra("phone", phoneNumber2);
    intending(toPackage("com.android.contacts"))
        .respondWith(new ActivityResult(Activity.RESULT_OK, resultData2));

    pick(phoneNumber2);
  }

  @Test
  public void externalIntent_WithResultStubbingCallable() {
    Intent resultData = new Intent();
    String phoneNumber = "123-345-6789";
    resultData.putExtra("phone", phoneNumber);
    TestActivityResultFunction resultCallable =
        new TestActivityResultFunction(new ActivityResult(Activity.RESULT_OK, resultData));
    intending(toPackage("com.android.contacts")).respondWithFunction(resultCallable);
    pick(phoneNumber);
    assertThat(resultCallable.wasCalled).isTrue();
  }

  @Test
  public void recordsIntentsOnlyAfterInit() {
    // Ensure that getActivity() in setUp() is not recorded by Intents
    expectedException.expect(AssertionFailedError.class);
    intended(hasComponent(hasShortClassName(".SendActivity")));
  }

  @Test
  public void intentScheme() {
    String dialerPackage = "com.android.phone";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // Starting with Android Lollipop the dialer package has changed.
      dialerPackage = "com.android.server.telecom";
    }

    // Testing Scheme "tel:xxx-xxx-xxxx"
    onView(withId(R.id.send_data_to_call_edit_text))
        .perform(closeSoftKeyboard(), scrollTo(), typeText("123-345-6789"), closeSoftKeyboard());
    onView(withId(R.id.send_to_call_button)).perform(scrollTo(), click());
    intended(
        allOf(
            hasAction(Intent.ACTION_CALL),
            toPackage(dialerPackage),
            hasData(allOf(hasSchemeSpecificPart("tel", "123-345-6789")))));

    // Testing Scheme "market://details?id=x"
    onView(withId(R.id.send_to_market_data))
        .perform(scrollTo(), typeText("com.google.android.apps.plus"), closeSoftKeyboard());
    onView(withId(R.id.send_to_market_button)).perform(scrollTo(), click());
    intended(
        allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(
                allOf(
                    hasScheme("market"),
                    hasHost("details"),
                    hasParamWithValue("id", "com.google.android.apps.plus")))));
  }

  @Test
  public void internalMultipleIntents() {
    clickToDisplayActivity();
    pressBack();
    clickToDisplayActivity();

    try {
      intended(anyIntent());
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException(
          "intended should have failed: too many matching recorded intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    try {
      intended(anyIntent(), Intents.times(3));
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException(
          "intended should have failed: too few matching recorded intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    intended(anyIntent(), Intents.times(2));

    try {
      intended(toPackage("androidx.test.ui.app"));
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException(
          "intended should have failed: too many matching recorded intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    try {
      intended(toPackage("androidx.test.ui.app"), Intents.times(3));
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException(
          "intended should have failed: too few matching recorded intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    intended(toPackage("androidx.test.ui.app"), Intents.times(2));
  }

  @Test
  public void noUnverifiedIntents() {
    clickToDisplayActivity();
    try {
      assertNoUnverifiedIntents();
      // Can't call fail() because it throws an AssertionFailedError, just like
      // assertNoUnverifiedIntents() does.
      throw new IllegalStateException(
          "assertNoUnverifiedIntents should have failed: should be unverified intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    intended(anyIntent());
    assertNoUnverifiedIntents();

    pressBack();
    assertNoUnverifiedIntents();

    clickToDisplayActivity();
    try {
      assertNoUnverifiedIntents();
      // Can't call fail() because it throws an AssertionFailedError, just like
      // assertNoUnverifiedIntents() does.
      throw new IllegalStateException(
          "assertNoUnverifiedIntents should have failed: should be unverified intents");
    } catch (AssertionFailedError e) {
      // expected.
    }
    intended(toPackage("androidx.test.ui.app"), Intents.times(2));
    assertNoUnverifiedIntents();
  }

  // TODO(b/168904560): relies on MMS app. Rewrite to not have this dependency
  @Test
  @Ignore
  public void externalIntentWithType() {
    onView(withId(R.id.send_message_button)).perform(scrollTo(), click());
    intended(
        allOf(
            hasAction(Intent.ACTION_SEND),
            anyOf(
                toPackage("com.android.mms"),
                toPackage("com.google.android.apps.messaging"),
                toPackage("com.android.messaging") /* api 23 pkg name */),
            hasType("text/plain")));
  }

  @Test
  public void customUriDataMatcher() {
    String uri = "https://www.google.com/search?q=Matcher&aq=f&oq=Matcher&sourceid=chrome&ie=UTF-8";
    sendUriToBrowser(uri);
    intended(
        allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(
                allOf(
                    hasHost("www.google.com"),
                    hasParamWithName("sourceid"),
                    hasParamWithName("aq"),
                    hasPath("/search"),
                    hasParamWithValue("sourceid", "chrome"),
                    hasParamWithValue("q", "Matcher"),
                    hasParamWithValue("ie", "UTF-8")))));
    assertOnSendActivity();
  }

  @Test
  public void customIntentMatcher_WithExternalIntent() {
    String uri = "http://www.google.com";
    sendUriToBrowser(uri);
    intended(
        allOf(
            hasAction(Intent.ACTION_VIEW),
            hasCategories(hasItem(Intent.CATEGORY_BROWSABLE)),
            hasData(hasHost("www.google.com")),
            hasExtras(allOf(hasEntry("key1", "value1"), hasEntry("key2", "value2")))));
    assertOnSendActivity();
  }

  @Test
  public void componentNameMatcher() {
    clickToDisplayActivity();
    intended(
        hasComponent(
            allOf(
                hasPackageName("androidx.test.ui.app"),
                hasShortClassName(".DisplayActivity"))));
  }

  @Test
  public void internalIntentStubbing() {
    intending(isInternal()).respondWith(new ActivityResult(Activity.RESULT_OK, null));
    clickToDisplayActivity();

    // Validate that we're still on the same screen (i.e. the intent to the DisplayActivity was
    // stubbed)
    onView(withId(R.id.send_button)).check(matches(isDisplayed()));
  }

  private void pick(String phoneNumber) {
    onView(withId(R.id.pick_button)).perform(scrollTo(), click());
    onView(withText(phoneNumber)).perform(scrollTo()).check(matches(isDisplayed()));
  }

  private void assertOnSendActivity() {
    onView(withId(R.id.send_title)).perform(scrollTo()).check(matches(isDisplayed()));
  }

  private void clickToDisplayActivity() {
    onView(withId(R.id.send_data_edit_text))
        .perform(scrollTo(), typeText("data be here"), closeSoftKeyboard());
    onView(withId(R.id.send_button)).perform(click());
  }

  private void sendUriToBrowser(String uri) {
    onView(withId(R.id.pick_button)).perform(scrollTo());
    onView(withId(R.id.send_data_to_browser_edit_text)).perform(typeText(uri), closeSoftKeyboard());
    onView(withId(R.id.send_to_browser_button)).perform(scrollTo(), click());
  }

  private static final class TestActivityResultFunction implements ActivityResultFunction {
    private final ActivityResult result;

    private boolean wasCalled = false;

    private TestActivityResultFunction(ActivityResult result) {
      this.result = result;
    }

    @Override
    public ActivityResult apply(Intent intent) {
      wasCalled = true;
      return result;
    }
  }
}
