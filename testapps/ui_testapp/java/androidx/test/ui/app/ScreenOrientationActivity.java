/*
 * Copyright (C) 2021 The Android Open Source Project
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
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/** Activity that updates a TextView when its screen orientation is changed. */
public class ScreenOrientationActivity extends Activity {
  private static final String TAG = "ScreenOrientationActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.screen_orientation_activity);

    // Set orientation in onCreate the first time it's called.
    TextView textView = (TextView) findViewById(R.id.current_screen_orientation);
    if (textView
        .getText()
        .toString()
        .equals(getResources().getString(R.string.screen_orientation_text))) {
      String orientation = setOrientationString(getResources().getConfiguration().orientation);
      Log.d(TAG, "onCreate. Orientation set to " + orientation);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String newOrientation = setOrientationString(newConfig.orientation);
    Log.d(TAG, "onConfigurationChanged. New orientation is " + newOrientation);
  }

  private String setOrientationString(int orientation) {
    String orientationString;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      orientationString = "landscape";
    } else {
      orientationString = "portrait";
    }

    TextView textView = (TextView) findViewById(R.id.current_screen_orientation);
    textView.setText(orientationString);
    return orientationString;
  }
}
