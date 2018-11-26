/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.multiprocess.app;

import static androidx.test.multiprocess.app.Util.setCurrentRunningProcess;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.espresso.IdlingRegistry;
import com.google.android.apps.common.testing.ui.multiprocess.testapp.idling.MessageDelayer;
import com.google.android.apps.common.testing.ui.multiprocess.testapp.idling.SimpleIdlingResource;

/**
 * Gets a text String from the user and displays it back after a while.
 * Note: This Activity runs on a private process ":idling".
 */
public class IdlingActivity extends Activity implements View.OnClickListener,
    MessageDelayer.DelayerCallback {

  // The TextView used to display the message inside the Activity.
  private TextView textView;

  // The EditText where the user types the message.
  private EditText editText;

  // The Idling Resource which will be null in production (not test environment).
  @Nullable
  private SimpleIdlingResource idlingResource;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.idling_activity);

    // Set the listeners for the buttons.
    findViewById(R.id.changeTextBtn).setOnClickListener(this);

    textView = (TextView) findViewById(R.id.textToBeChanged);
    editText = (EditText) findViewById(R.id.editTextUserInput);

    TextView processNameTextView = (TextView) findViewById(R.id.textIdlingProcessName);
    setCurrentRunningProcess(processNameTextView, this);
    registerIdlingResource();
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterIdlingResource();
  }

  @Override
  public void onClick(View view) {
    // Get the text from the EditText view.
    String text = editText.getText().toString();

    if (view.getId() == R.id.changeTextBtn) {
      // Set a temporary text.
      textView.setText(R.string.waiting_msg);
      // Submit the message to the delayer.
      MessageDelayer.processMessage(text, this, idlingResource);
    }
  }

  @Override
  public void onDone(String text) {
    // The delayer notifies the activity via a callback.
    textView.setText(text);
  }

  /**
   * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
   */
  @VisibleForTesting
  boolean registerIdlingResource() {
    if (idlingResource == null) {
      idlingResource = new SimpleIdlingResource();
    }
    return IdlingRegistry.getInstance().register(idlingResource);
  }

  boolean unregisterIdlingResource() {
    if (idlingResource == null) {
      return false;
    }
    return IdlingRegistry.getInstance().unregister(idlingResource);
  }
}
