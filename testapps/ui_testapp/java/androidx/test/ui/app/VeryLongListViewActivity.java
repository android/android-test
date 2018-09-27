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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/** An activity displaying ListView with long header and footer. */
public class VeryLongListViewActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_with_dummy_entries);

    ListView listView = (ListView) findViewById(R.id.list);

    LayoutInflater inflater = getLayoutInflater();
    View header = inflater.inflate(R.layout.long_header, listView, false);
    View footer = inflater.inflate(R.layout.long_footer, listView, false);

    listView.addHeaderView(header);
    listView.addFooterView(footer);

    ArrayAdapter<String> itemsAdapter =
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            getResources().getStringArray(R.array.spinner_array));

    listView.setAdapter(itemsAdapter);
  }
}
