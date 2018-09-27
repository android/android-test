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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity designed to have layout issues in it for testing purposes.
 */
public class LayoutIssuesActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_issues_activity);

    final Button length = (Button) findViewById(R.id.length);
    length.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        TextView tv = (TextView) findViewById(R.id.overlap);
        tv.setText(tv.getText() + " some more text");
      }
    });

    final Button wrap = (Button) findViewById(R.id.wrap);
    wrap.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Button self = (Button) v;
        self.setText(self.getText() + " some more text");
      }
    });
  }
}
