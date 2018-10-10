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

package androidx.test.espresso.web.assertion;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.elementById;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.junit.Assert.fail;

import android.webkit.WebView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.WebFormActivity;
import junit.framework.AssertionFailedError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test case for Espresso web assertions. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WebViewAssertionsTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(WebFormActivity.class);
  }

  @Test
  public void testWebContent_legitAssertion() {
    onWebView(isAssignableFrom(WebView.class))
        .check(
            webContent(
                elementById("info", withTextContent("Enter input and click the Submit button."))));
  }

  @Test
  public void testWebContent_NotWebView() {
    try {
      onWebView(isRoot())
          .check(
              webContent(
                  elementById(
                      "info", withTextContent("Enter input and click the Submit button."))));
      fail("Previous call should have failed");
    } catch (RuntimeException expected) {
    }
  }

  @Test
  public void testWebContent_NoViewFound() {
    try {
      onWebView(withText("not there"))
          .check(
              webContent(
                  elementById(
                      "info", withTextContent("Enter input and click the Submit button."))));
      fail("Previous call should have failed");
    } catch (NoMatchingViewException expected) {
    }
  }

  @Test
  public void testWebContent_validAssertion_fails() {
    try {
      onWebView(isAssignableFrom(WebView.class))
          .check(webContent(elementById("info", withTextContent("Not what we expect."))));
    } catch (AssertionFailedError expected) {
    }
  }
}
