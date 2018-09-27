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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A WebView with a form where you can type something into a field, click a submit button and see
 * some feedback on the screen.
 */
public class WebFormActivity extends Activity {
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    WebView mainWebView = new WebView(this);
    setContentView(mainWebView);
    mainWebView.loadData(
          "<html>"
          + "<body>"
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
        + "</html>", "text/html", null);
    WebSettings settings = mainWebView.getSettings();
    settings.setJavaScriptEnabled(true);
  }
}
