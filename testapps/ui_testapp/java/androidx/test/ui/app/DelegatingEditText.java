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

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Custom edit text widget.
 */
public class DelegatingEditText extends LinearLayout {

  private final EditText delegateEditText;
  private final TextView messageView;
  private final Context mContext;

  public DelegatingEditText(Context context) {
    this(context, null);
  }

  public DelegatingEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);
    mContext = context;
    LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.delegating_edit_text, this, /* attachToRoot */ true);
    messageView = (TextView) findViewById(R.id.edit_text_message);
    delegateEditText = (EditText) findViewById(R.id.delegate_edit_text);
    delegateEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionCode, KeyEvent event) {
        messageView.setText("typed: " + delegateEditText.getText());
        messageView.setVisibility(View.VISIBLE);
        InputMethodManager imm =
            (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(delegateEditText.getWindowToken(), 0);
        return true;
      }
    });
  }
}
