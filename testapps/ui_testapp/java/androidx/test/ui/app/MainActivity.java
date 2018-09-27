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

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a list with all available activities.
 */
public class MainActivity extends ListActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  private static final Comparator<Map<String, Object>> sDisplayNameComparator =
      new Comparator<Map<String, Object>>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
          return collator.compare(map1.get("title"), map2.get("title"));
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setListAdapter(new SimpleAdapter(
        this, getData(), android.R.layout.simple_list_item_1, new String[] {"title"},
        new int[] {android.R.id.text1}));
    getListView().setTextFilterEnabled(true);
  }

  private List<Map<String, Object>> getData() {
    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    PackageInfo info = null;
    try {
      info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Packageinfo not found in: " + getPackageName());
    }

    if (null == info) {
      return data;
    } else {
      for (ActivityInfo activityInfo : info.activities) {

        // Only include Activities which are not the "MainActivity" and are part of the testapp
        // package!
        if (!activityInfo.name.equals(getComponentName().getClassName())
                & activityInfo.name.contains(getComponentName().getPackageName())) {
          CharSequence label = activityInfo.nonLocalizedLabel;
          if (label == null || label.toString().isEmpty()) {
            Log.w(TAG, "No label for Activity: " + activityInfo.name);
            continue;
          }
          String[] name = activityInfo.name.split(getPackageName() + ".");
          try {
            addItem(data, name[1],
                createActivityIntent(activityInfo.applicationInfo.packageName, activityInfo.name));
          } catch (ArrayIndexOutOfBoundsException aoobe) {
            Log.e(TAG, "No name for Activity: " + activityInfo.name, aoobe);
          }
        }
      }
    }

    Collections.sort(data, sDisplayNameComparator);
    return data;
  }

  private Intent createActivityIntent(String pkg, String componentName) {
    Intent result = new Intent();
    result.setClassName(pkg, componentName);
    return result;
  }

  private void addItem(List<Map<String, Object>> data, String name, Intent intent) {
    Map<String, Object> temp = new HashMap<String, Object>();
    temp.put("title", name);
    temp.put("intent", intent);
    data.add(temp);
  }

  @Override
  protected void onListItemClick(ListView listView, View view, int position, long id) {
    Map<?, ?> map = (Map<?, ?>) listView.getItemAtPosition(position);

    Intent intent = (Intent) map.get("intent");
    startActivity(intent);
  }
}
