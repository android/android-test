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
import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Shows ActionBar with a lot of items to get Action overflow on large displays. Click on item
 * changes text of R.id.textActionBarResult.
 */
public class ActionBarTestActivity extends AppCompatActivity {
  private ActionMode mode;
  private MenuInflater inflater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actionbar_activity);
    inflater = getMenuInflater();
    mode = startSupportActionMode(new TestActionMode());

    ((Button) findViewById(R.id.show_contextual_action_bar)).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mode = startSupportActionMode(new TestActionMode());
          }
        });
    ((Button) findViewById(R.id.hide_contextual_action_bar)).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (mode != null) {
              mode.finish();
            }
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    inflater.inflate(R.menu.actionbar_context_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menu) {
    setResult(menu.getTitle());
    return true;
  }

  private void setResult(CharSequence result) {
    TextView text = (TextView) findViewById(R.id.text_action_bar_result);
    text.setText(result);
  }

  private final class TestActionMode implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      inflater.inflate(R.menu.actionbar_activity_actions, menu);
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      setResult(item.getTitle());
      return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}
  }
}
