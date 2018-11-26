/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import android.os.Handler;
import androidx.annotation.VisibleForTesting;
import android.widget.TextView;

/** An activity with a delayed text view loading */
public class IdlingUIActivity extends Activity {
  @VisibleForTesting static final String DATA = "ąęćśasdfgfłófdfg";
  @VisibleForTesting static final String FINISHED = "FINISHED";

  /** Listener that will notified when the delayed text view is started/finished loading */
  public interface Listener {
    void onLoadStarted();

    void onLoadFinished();
  }

  static Listener listener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_idling_ui);

    if (listener != null) {
      listener.onLoadStarted();
    }
    Handler handler = new Handler();
    handler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            IdlingUIActivity.this.<TextView>findViewById(R.id.textView).setText(FINISHED);
            if (listener != null) {
              listener.onLoadFinished();
            }
          }
        },
        1000);
  }
}
