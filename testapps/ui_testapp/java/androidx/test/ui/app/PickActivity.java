/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * An activity that allows the user to pick a number and return the number as the activity result
 */
public class PickActivity extends Activity implements View.OnClickListener {

  public static final String EXTRA_PICKED_NUMBER = "picked_number";
  public static final String EXTRA_SET_NUMBER = "set_number";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pick_activity);

    Button button = (Button) findViewById(R.id.button_pick_number);
    button.setOnClickListener(this);

    button = (Button) findViewById(R.id.button_set_number);
    button.setOnClickListener(this);

    button = (Button) findViewById(R.id.button_cancel);
    button.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.button_pick_number) {
      Intent resultData = new Intent();
      resultData.putExtra(EXTRA_PICKED_NUMBER, 7);
      setResult(RESULT_OK, resultData);
      finish();
    } else if (v.getId() == R.id.button_set_number) {
      Intent resultData = new Intent();
      resultData.putExtra(EXTRA_SET_NUMBER, 10);
      setResult(RESULT_OK, resultData);
      // don't finish!
    } else {
      setResult(RESULT_CANCELED);
      finish();
    }
  }
}
