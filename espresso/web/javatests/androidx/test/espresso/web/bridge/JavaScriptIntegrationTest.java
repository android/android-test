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

package androidx.test.espresso.web.bridge;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.internal.util.Checks.checkNotNull;
import static com.google.common.truth.Truth.assertThat;

import android.view.View;
import android.webkit.WebView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ui.app.SimpleWebViewActivity;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Evaluate some javascript thru the Javascript bridge. */
@RunWith(AndroidJUnit4.class)
public class JavaScriptIntegrationTest {

  @Rule
  public ActivityScenarioRule<SimpleWebViewActivity> activityScenarioRule =
      new ActivityScenarioRule<>(SimpleWebViewActivity.class);

  @Test
  public void testClickBlock() throws Exception {
    Conduit initialValue = JavaScriptBridge.makeConduit();
    onView(isAssignableFrom(WebView.class))
        .perform(new JavaScriptExecutionAction(
            initialValue.wrapScriptInConduit("document.was_clicked")));

    assertThat(initialValue.getResult().get(2, TimeUnit.SECONDS)).isEqualTo("false");

    // The displayed webview is one huge button... so no need for intellegently determining where
    // to click.
    onView(isAssignableFrom(WebView.class))
        .perform(click());

    Conduit endValue = JavaScriptBridge.makeConduit();
    onView(isAssignableFrom(WebView.class))
        .perform(new JavaScriptExecutionAction(
            endValue.wrapScriptInConduit("document.was_clicked")));

    assertThat(endValue.getResult().get(2, TimeUnit.SECONDS)).isEqualTo("true");
  }

  @Test
  public void testJavascriptEvaluation() throws Exception {
    Conduit resultConduit = JavaScriptBridge.makeConduit();
    onView(isAssignableFrom(WebView.class))
        .perform(new JavaScriptExecutionAction(resultConduit.wrapScriptInConduit("1+2+3")));
    assertThat(resultConduit.getResult().get(2, TimeUnit.SECONDS)).isEqualTo("6");
  }

  private static class JavaScriptExecutionAction implements ViewAction {
    private final String wrappedScript;

    JavaScriptExecutionAction(String wrappedScript) {
      this.wrappedScript = checkNotNull(wrappedScript);
    }

    @Override
    public Matcher<View> getConstraints() {
      return isAssignableFrom(WebView.class);
    }

    @Override
    public String getDescription() {
      return "exec me some javascript!";
    }

    @Override
    public void perform(UiController controller, View view) {
      controller.loopMainThreadForAtLeast(5000); // TODO(thomaswk): replace with Web sugar.
      ((WebView) view).loadUrl("javascript:" + wrappedScript);
    }

  }
}
