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
import android.widget.TextView;

/**
 * Activity that updates a TextView when entering or exiting multi-window mode and implements
 * onConfigurationChanged().
 */
public class MultiWindowWithConfigChangesActivity extends Activity {
  private static final String TAG = MultiWindowWithConfigChangesActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.multiwindow_with_config_changes_activity);

    String multiWindowModeText = updateTextView();
    Log.d(TAG, "onCreate. " + multiWindowModeText);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    String multiWindowModeText = updateTextView();
    Log.d(TAG, "onConfigurationChanged. " + multiWindowModeText);
  }

  private String updateTextView() {
    String multiWindowModeText;
    if (this.isInMultiWindowMode()) {
      multiWindowModeText = "Device is in multi-window mode.";
    } else {
      multiWindowModeText = "Device is not in multi-window mode.";
    }
    TextView textView = (TextView) findViewById(R.id.multi_window_mode_textview);
    textView.setText(multiWindowModeText);
    return multiWindowModeText;
  }
}
