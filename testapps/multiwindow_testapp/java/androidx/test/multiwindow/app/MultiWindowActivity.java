/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.multiwindow.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** Activity that updates a TextView when entering or exiting multi-window mode. */
public class MultiWindowActivity extends Activity {
  private static final String TAG = MultiWindowActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.multiwindow_activity);

    String multiWindowModeText;
    if (this.isInMultiWindowMode()) {
      multiWindowModeText = "Device is in multi-window mode.";
    } else {
      multiWindowModeText = "Device is not in multi-window mode.";
    }
    Log.d(TAG, "onCreate." + multiWindowModeText);
    TextView textView = (TextView) findViewById(R.id.multi_window_mode);
    textView.setText(multiWindowModeText);

    ViewGroup container = (ViewGroup) getWindow().findViewById(android.R.id.content);
    container.addView(
        new View(this) {
          @Override
          protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            computeWindowSizeClasses();
          }
        });
    computeWindowSizeClasses();
  }

  private void computeWindowSizeClasses() {
    TextView screenWidthTextView = (TextView) findViewById(R.id.screen_width_display_size);
    float width =
        this.getResources().getDisplayMetrics().widthPixels
            / this.getResources().getDisplayMetrics().density;
    String screenWidthText;
    if (width < 600f) {
      screenWidthText = "Compact width";
    } else if (width < 840f) {
      screenWidthText = "Medium width";
    } else {
      screenWidthText = "Expanded width";
    }
    screenWidthTextView.setText(screenWidthText);

    TextView screenHeightTextView = (TextView) findViewById(R.id.screen_height_display_size);
    float height =
        this.getResources().getDisplayMetrics().heightPixels
            / this.getResources().getDisplayMetrics().density;
    String screenHeightText;
    if (height < 480f) {
      screenHeightText = "Compact height";
    } else if (height < 900f) {
      screenHeightText = "Medium height";
    } else {
      screenHeightText = "Expanded height";
    }
    screenHeightTextView.setText(screenHeightText);
  }
}
