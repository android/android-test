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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Simple activity used to demonstrate a simple Espresso test.
 */
public class SimpleActivity extends Activity implements OnItemSelectedListener{
  private static final String TAG = "SimpleActivity";

  static final String EXTRA_DATA = "androidx.test.ui.app.DATA";

  static int counter = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);

    Spinner spinner = (Spinner) findViewById(R.id.spinner_simple);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.spinner_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    counter++;
    Log.d(TAG, "onStart " + this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    counter--;
    Log.d(TAG, "onStop " + this);
  }

  public void simpleButtonClicked(View view) {
    TextView textView = (TextView) findViewById(R.id.text_simple);
    String message = "Hello Espresso!";
    textView.setText(message);
  }

  /** Called when user clicks the Send button */
  public void sendButtonClicked(@SuppressWarnings("unused") View view) {
    Intent intent = new Intent(this, DisplayActivity.class);
    EditText editText = (EditText) findViewById(R.id.sendtext_simple);
    intent.putExtra(EXTRA_DATA, editText.getText().toString());
    startActivity(intent);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    TextView textView = (TextView) findViewById(R.id.spinnertext_simple);
    textView.setText(String.format("One %s a day!", parent.getItemAtPosition(pos)));
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }
}

