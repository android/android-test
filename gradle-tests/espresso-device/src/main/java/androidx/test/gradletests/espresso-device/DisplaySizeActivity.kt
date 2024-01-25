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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/** Activity that updates a TextView when display size changes. */
class DisplaySizeActivity : Activity() {
  companion object {
    private val TAG = DisplaySizeActivity::class.java.getSimpleName()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_display_size)

    val container: ViewGroup = getWindow().findViewById(android.R.id.content) as ViewGroup
    container.addView(
      object : View(this) {
        override fun onConfigurationChanged(newConfig: Configuration) {
          super.onConfigurationChanged(newConfig)
          computeDisplaySizeClasses()
        }
      }
    )
    computeDisplaySizeClasses()
  }

  private fun computeDisplaySizeClasses() {
    val screenWidthTextView: TextView = findViewById<TextView>(R.id.screen_width_display_size)
    val width: Float =
      this.getResources().getDisplayMetrics().widthPixels /
        this.getResources().getDisplayMetrics().density
    val screenWidthText: String =
      if (width < 600f) {
        "Compact width"
      } else if (width < 840f) {
        "Medium width"
      } else {
        "Expanded width"
      }
    screenWidthTextView.setText(screenWidthText)

    val screenHeightTextView: TextView = findViewById<TextView>(R.id.screen_height_display_size)
    val height: Float =
      this.getResources().getDisplayMetrics().heightPixels /
        this.getResources().getDisplayMetrics().density
    val screenHeightText: String =
      if (height < 480f) {
        "Compact height"
      } else if (height < 900f) {
        "Medium height"
      } else {
        "Expanded height"
      }
    screenHeightTextView.setText(screenHeightText)
  }
}
