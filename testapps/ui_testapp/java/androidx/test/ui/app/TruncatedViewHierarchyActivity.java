/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import java.util.Locale;

/** An activity displaying many views, enough to truncate view hierarchy during tests. */
public class TruncatedViewHierarchyActivity extends Activity {

  public static final String LEVEL_INTENT_KEY =
      "androidx.test.ui.app.TruncatedViewHierarchyActivity.LEVEL";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.truncated_view_activity);

    int level = 3;
    Intent intent = getIntent();
    if (intent != null) {
      level = intent.getIntExtra(LEVEL_INTENT_KEY, level);
    }

    FrameLayout container = findViewById(R.id.container);
    insertGroup(container, level, 800, 0, 0);
  }

  private void insertGroup(FrameLayout container, int level, int size, int offsetX, int offsetY) {
    FrameLayout group = new FrameLayout(container.getContext());
    LayoutParams params = new LayoutParams(size, size);
    params.leftMargin = offsetX;
    params.topMargin = offsetY;
    int id = Integer.parseInt(String.format(Locale.US, "%d%03d%03d", level, offsetX, offsetY));
    group.setId(id);
    container.addView(group, params);

    TextView text = new TextView(container.getContext());
    LayoutParams paramsTxt = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    paramsTxt.leftMargin = level;
    paramsTxt.topMargin = level;
    text.setText(String.format(Locale.US, "%d - %d - %dx%d", level, size, offsetX, offsetY));
    text.setId(id + 1);
    group.addView(text, paramsTxt);

    if (level > 0) {
      int size2 = size / 2;
      int level1 = level - 1;
      insertGroup(group, level1, size2, offsetX, offsetY);
      insertGroup(group, level1, size2, offsetX + size2, offsetY);
      insertGroup(group, level1, size2, offsetX + size2, offsetY);
      insertGroup(group, level1, size2, offsetX, offsetY + size2);
    }
  }
}
