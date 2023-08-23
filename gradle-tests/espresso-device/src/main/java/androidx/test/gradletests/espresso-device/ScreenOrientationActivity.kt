/*
 * Copyright (C) 2023 The Android Open Source Project
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

package androidx.test.gradletests.espresso.device

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.TextView

/** Activity that updates a TextView when its screen orientation is changed. */
class ScreenOrientationActivity : Activity() {
  companion object {
    private val TAG = "ScreenOrientationActivity"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_screen_orientation)

    // Set orientation in onCreate the first time it's called.
    val textView: TextView = findViewById<TextView>(R.id.current_screen_orientation)
    if (
      textView
        .getText()
        .toString()
        .equals(getResources().getString(R.string.screen_orientation_text))
    ) {
      val orientation = setOrientationString(getResources().getConfiguration().orientation)
      Log.d(TAG, "onCreate. Orientation set to " + orientation)
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val newOrientation = setOrientationString(newConfig.orientation)
    Log.d(TAG, "onConfigurationChanged. New orientation is " + newOrientation)
  }

  private fun setOrientationString(orientation: Int): String {
    val orientationString =
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        "landscape"
      } else {
        "portrait"
      }

    val textView: TextView = findViewById<TextView>(R.id.current_screen_orientation)
    textView.setText(orientationString)
    return orientationString
  }
}
