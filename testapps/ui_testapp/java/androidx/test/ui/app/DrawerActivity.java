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

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;

/** Activity to demonstrate actions on a {@link DrawerLayout}. */
public class DrawerActivity extends AppCompatActivity {

  public static final String[] DRAWER_CONTENTS =
      new String[] {"Platypus", "Wombat", "Pickle", "Badger"};

  private ActionBarDrawerToggle drawerToggle;
  private CharSequence title;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawer_activity);

    ListAdapter listAdapter = new ArrayAdapter<String>(
        getApplicationContext(), R.layout.drawer_row, R.id.drawer_row_name, DRAWER_CONTENTS);
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView drawerList = (ListView) findViewById(R.id.drawer_list);
    drawerList.setAdapter(listAdapter);

    final TextView textView = (TextView) findViewById(R.id.drawer_text_view);

    drawerList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        textView.setText("You picked: " + DRAWER_CONTENTS[(int) id]);
        drawerLayout.closeDrawers();
      }
    });

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    title = getTitle();

    drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_close) {

          /** Called when a drawer has settled in a completely closed state. */
          @Override
          public void onDrawerClosed(View view) {
            getSupportActionBar().setTitle(title);
          }

          /** Called when a drawer has settled in a completely open state. */
          @Override
          public void onDrawerOpened(View drawerView) {
            getSupportActionBar().setTitle(title);
          }
        };
    drawerLayout.setDrawerListener(drawerToggle);
  }

  @Override
  public void setTitle(CharSequence title) {
    this.title = title;
    getSupportActionBar().setTitle(title);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggls
    drawerToggle.onConfigurationChanged(newConfig);
  }
}

