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
public class ScreenOrientationWithoutOnConfigurationChangedActivity extends Activity {
  private static final String TAG = "ScreenOrientationWithoutOnConfigurationChangedActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.screen_orientation_without_onconfigurationchanged_activity);

    String orientation;
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      orientation = "landscape";
    } else {
      orientation = "portrait";
    }

    TextView textView = (TextView) findViewById(R.id.screen_orientation);
    textView.setText(orientation);
    Log.d(TAG, "onCreate. Orientation set to " + orientation);
  }
}
