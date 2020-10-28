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
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;

/** Activity that contains a {@link View} that scales in size. */
public class ScaledViewActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupView();
  }

  private void setupView() {
    setContentView(R.layout.scaledview_activity);
    View scaledView = findViewById(R.id.scaled_view);
    scaledView.setOnClickListener(
        new View.OnClickListener() {
          private int scaleIncrement = 2; // Value can be 1 (0.5), 2 (1.0) or 3 (1.5)

          @Override
          public void onClick(View v) {
            ViewPropertyAnimator animator = v.animate();
            animator.cancel();
            scaleIncrement = scaleIncrement % 3 + 1;
            float scale = scaleIncrement * 0.5f;
            animator.scaleX(scale).scaleY(scale).start();
          }
        });
  }
}
