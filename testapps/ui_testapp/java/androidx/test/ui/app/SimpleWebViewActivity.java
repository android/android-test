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
 * One big web view to play with.
 */
public class SimpleWebViewActivity extends Activity {
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    WebView mainWebView = new WebView(this);
    setContentView(mainWebView);
    mainWebView.loadData(
        "<html>" +
        "<script>document.was_clicked = false</script>" +
        "<body> " +
        "<button style='height:1000px;width:1000px;' onclick='document.was_clicked = true'> " +
        "I'm a button</button>" +
        "</body> " +
        "</html>", "text/html", null);
    WebSettings settings = mainWebView.getSettings();
    settings.setJavaScriptEnabled(true);
  }
}
