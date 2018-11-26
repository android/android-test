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

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Shows Toolbar as ActionBar with action items, including an overflow. Shows second toolbar with
 * action items, including an overflow.
 */
public class ToolbarActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.toolbar_activity);

    //Set first toolbar as actionbar
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    //Set menu in second toolbar
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_over_text);
    toolbar.inflateMenu(R.menu.actionbar_context_actions);
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        setResult(item.getTitle());
        return true;
      }
    });
  }

  private void setResult(CharSequence result) {
   TextView text = (TextView) findViewById(R.id.toolbar_result);
   text.setText(result);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actionbar_activity_actions, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    setResult(item.getTitle());
    return super.onOptionsItemSelected(item);
  }
}
