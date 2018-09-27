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

package androidx.test.multiprocess.app;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.elementById;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import android.util.Log;
import android.webkit.WebView;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.web.webdriver.DriverAtoms;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Sample tests showcasing Espresso Web testing in multi-process environment.
 *
 * <p>The sample has a simple layout which contains a single {@link WebView}. The HTML page displays
 * a form with an input tag and buttons to submit the form.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class WebViewActivityTest {
  private static final String TAG = "WebViewActivityTest";
  private static final String MACCHIATO = "Macchiato";
  private static final String DOPPIO = "Doppio";

  /** Note: That we're starting the MainActivity here in order to create a multi-process scenario */
  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

  @SuppressWarnings("rawtypes")
  public static Matcher<Object> withItemContent(final Matcher<String> itemTextMatcher) {
    return new BoundedMatcher<Object, Map>(Map.class) {
      @Override
      public boolean matchesSafely(Map map) {
        return hasEntry(equalTo("title"), itemTextMatcher).matches(map);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with item content: ");
        itemTextMatcher.describeTo(description);
      }
    };
  }

  @Test
  public void typeTextInInput_clickButton_SubmitsForm() {
    Log.i(TAG, "About to click on WebViewActivity...");
    onData(withItemContent(containsString("WebViewActivity"))).perform(click());

    // Selects the WebView in your layout. If you have multiple WebViews you can also use a
    // matcher to select a given WebView, onWebView(withId(R.id.web_view)).
    onWebView()
        // Find the input element by ID
        .withElement(findElement(Locator.ID, "text_input"))
        // Clear previous input
        .perform(clearElement())
        // Enter text into the input element
        .perform(DriverAtoms.webKeys(MACCHIATO))
        // Find the submit button
        .withElement(findElement(Locator.ID, "submitBtn"))
        // Simulate a click via javascript
        .perform(webClick())
        // Find the response element by ID
        .withElement(findElement(Locator.ID, "response"))
        // Verify that the response page contains the entered text
        .check(webMatches(getText(), containsString(MACCHIATO)))
        // Verify that the response page contains the entered text with webContents by id
        .check(webContent(elementById("response", withTextContent(containsString(MACCHIATO)))));
  }

  @Test
  public void typeTextInInput_clickButton_ChangesText() {
    Log.i(TAG, "About to click on WebViewActivity...");
    onData(withItemContent(containsString("WebViewActivity"))).perform(click());
    // Selects the WebView in your layout. If you have multiple WebViews you can also use a
    // matcher to select a given WebView, onWebView(withId(R.id.web_view)).
    onWebView()
        // Find the input element by ID
        .withElement(findElement(Locator.ID, "text_input"))
        // Clear previous input
        .perform(clearElement())
        // Enter text into the input element
        .perform(DriverAtoms.webKeys(DOPPIO))
        // Find the change text button.
        .withElement(findElement(Locator.ID, "changeTextBtn"))
        // Click on it.
        .perform(webClick())
        // Find the message element by ID
        .withElement(findElement(Locator.ID, "message"))
        // Verify that the text is displayed
        .check(webMatches(getText(), containsString(DOPPIO)))
        // Verify that the response page contains the entered text with webContents by id
        .check(webContent(elementById("message", withTextContent(containsString(DOPPIO)))));
  }
}
