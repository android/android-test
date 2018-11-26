/*
 * Copyright (C) 2017 The Android Open Source Project
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
 *
 */
package androidx.test.ui.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays a {@link DialogFragment} after a delay of {@code 3000} millis to check if {@link
 * androidx.test.espresso.base.RootViewPicker} waits for the window that matches the root
 * matcher.
 */
public class DelayedDialogActivity extends AppCompatActivity {

  private TextView selectedText;
  private int delayInMillis = 1000;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.delayed_dialog_activity);

    selectedText = (TextView) findViewById(R.id.delayed_dialog_selection);

    Button delayedDialogBtn = (Button) findViewById(R.id.delayed_dialog_btn);
    delayedDialogBtn.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(
                new Runnable() {
                  @Override
                  public void run() {
                    createAndShowDialogFragment();
                  }
                },
                delayInMillis);
          }
        });
  }

  private void createAndShowDialogFragment() {
    DialogFragment newFragment = DelayedDialogFragment.newInstance();
    newFragment.show(getSupportFragmentManager(), "dialog");
  }

  public void setDelayInMillis(int delayInMillis) {
    this.delayInMillis = delayInMillis;
  }

  private void setSelectedText(String text) {
    selectedText.setText(text);
  }

  /** {@link DialogFragment} which is shown after a short delay. */
  public static final class DelayedDialogFragment extends DialogFragment {

    static DelayedDialogFragment newInstance() {
      return new DelayedDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new AlertDialog.Builder(getActivity())
          .setTitle(getString(R.string.dialog_delayed_title))
          .setPositiveButton(
              getString(R.string.dialog_delayed_btn_text_espresso),
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                  ((DelayedDialogActivity) getActivity())
                      .setSelectedText(getString(R.string.dialog_delayed_btn_text_espresso));
                }
              })
          .setNegativeButton(
              getString(R.string.dialog_delayed_btn_text_latte),
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                  ((DelayedDialogActivity) getActivity())
                      .setSelectedText(getString(R.string.dialog_delayed_btn_text_latte));
                }
              })
          .create();
    }
  }
}
