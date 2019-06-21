/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import android.graphics.Color;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/**
 * Activity to demonstrate actions on a {@link NavigationView}.
 */
public class NavigationViewActivity extends AppCompatActivity {

  private DrawerLayout mDrawerLayout;
  private TextView selectedNavItemTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.navigation_view_activity);


    // Set up the toolbar.
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitleTextColor(Color.WHITE);
    setSupportActionBar(toolbar);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    // Set up the navigation drawer.
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerLayout.setStatusBarBackground(R.color.theme_primary);
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    if (navigationView != null) {
      setupDrawerContent(navigationView);
    }
    selectedNavItemTextView = (TextView) findViewById(R.id.selected_nav_item_text_view);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // Open the navigation drawer when the home icon is selected from the toolbar.
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupDrawerContent(NavigationView navigationView) {
    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(MenuItem menuItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            CharSequence menuItemTitle = menuItem.getTitle();
            if (!TextUtils.isEmpty(menuItemTitle)) {
              selectedNavItemTextView.setText(menuItemTitle);
            }
            // Close the navigation drawer when an item is selected.
            menuItem.setChecked(true);
            return true;
          }
        });
  }

}
