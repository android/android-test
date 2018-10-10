/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.web.action;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.elementById;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.model.Atoms.script;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import android.view.View;
import android.webkit.WebView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingResourceTimeoutException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.WebFormActivity;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test case for {@link JavaScriptExecutionAction}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class JavaScriptExecutionActionTest {

  /**
   * View action that loads data into a WebView using loadDataFromBaseUrl setting both baseUrl and
   * historyUrl to null.
   */
  static class WebViewLoadDataBaseUrl implements ViewAction {

    String historyUrl;
    String baseUrl;

    public WebViewLoadDataBaseUrl(String baseUrl, String historyUrl) {
      this.baseUrl = baseUrl;
      this.historyUrl = historyUrl;
    }

    @Override
    public Matcher<View> getConstraints() {
      return isAssignableFrom(WebView.class);
    }

    @Override
    public String getDescription() {
      return "Loading Data from BaseUrl";
    }

    @Override
    public void perform(UiController uiController, View view) {
      ((WebView) view)
          .loadDataWithBaseURL(
              this.baseUrl,
              "<html>"
                  + "<body>"
                  + "This data was loaded using loadDataWithBaseURL"
                  + "<script>"
                  + "  function onSubmit() {"
                  + "    value = document.getElementById('input').value;"
                  + "    document.getElementById('info').innerHTML = 'Submitted: ' + value;"
                  + "  }"
                  + "</script>"
                  + "<form action='javascript:onSubmit()'>"
                  + "  Input: <input type='text' id='input' value='sample'>"
                  + "  <input type='submit' id='submit' value='Submit'>"
                  + "</form>"
                  + "<p id='info'>Enter input and click the Submit button.</p>"
                  + "</body>"
                  + "</html>",
              "text/html",
              null,
              this.historyUrl);
    }
  }

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(WebFormActivity.class);
  }

  @Test
  public void testJavaScriptExecution() {
    onWebView(isAssignableFrom(WebView.class))
        .perform(script("document.getElementById('input').value = 'stuff'"))
        .perform(script("document.getElementById('submit').click()"))
        .check(webContent(elementById("info", withTextContent(containsString("stuff")))));
  }

  @Test
  public void testJavaScriptExecution_BadCommand() {
    try {
      onWebView(isAssignableFrom(WebView.class)).perform(script("rubbish"));
      fail("Previous command should have failed.");
    } catch (RuntimeException expected) {
      assertThat(expected.getMessage(), containsString("rubbish is not defined"));
    }
  }

  @Test
  public void testJavaScriptExecution_Timeout() {
    try {
      onWebView(isAssignableFrom(WebView.class))
          .withTimeout(2, TimeUnit.SECONDS)
          .perform(script("while(true){};"));
      fail("Previous command should have timed out.");
    } catch (IdlingResourceTimeoutException expected) {
      // Expected on API level 18 and lower (javascript handling is synchronized with Espresso
      // via IdlingResource).
    } catch (RuntimeException e) {
      // Expected on API level 19 and above
      // (synchronized by polling the Conduit.getResult().isDone()).
      assertThat(e.getCause(), instanceOf(TimeoutException.class));
    }
  }

  @Test
  public void testJavascriptExectionWithDataFromBaseUrl() {
    // Load the data using loadDataFromBaseUrl instead to test what happens when
    // both baseUrl and historyUrl are null.
    onView(isAssignableFrom(WebView.class)).perform(new WebViewLoadDataBaseUrl(null, null));
    onWebView(isAssignableFrom(WebView.class))
        .perform(script("document.getElementById('input').value = 'stuff'"))
        .perform(script("document.getElementById('submit').click()"))
        .check(webContent(elementById("info", withTextContent(containsString("stuff")))));
  }
}
