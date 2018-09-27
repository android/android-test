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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity running in a private process which is manually defined in the AndroidManifest.xml using
 * the android:process attribute.
 *
 * <p>From D.A.C: "If the name assigned to this attribute begins with a colon (':'), a new process,
 * private to the application, is created when it's needed and the activity runs in that process."
 */
public class PrivateProcessActivity extends Activity implements OnItemClickListener {

  private TextView privateProcessNameTextView;
  private TextView selectedListItemTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_process_private);
    privateProcessNameTextView = (TextView) findViewById(R.id.textPrivateProcessName);
    setCurrentRunningProcess(privateProcessNameTextView, this);

    selectedListItemTextView = (TextView) findViewById(R.id.selectedListItemText);
    ListView listView = (ListView) findViewById(R.id.list);
    String[] listItems = getResources().getStringArray(R.array.list_items);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);
  }

  public void onBtnClick(View view) {
    TextView v = (TextView) findViewById(R.id.displayTextView);
    v.setText(R.string.button_clicked);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    selectedListItemTextView.setText(
        String.format(getString(R.string.list_selection), ((TextView) view).getText()));
  }
}
