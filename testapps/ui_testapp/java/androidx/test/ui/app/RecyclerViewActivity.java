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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import 	androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import androidx.test.ui.app.RecyclerViewFragment.LayoutManagerType;
import androidx.viewpager.widget.ViewPager;
import com.google.common.annotations.VisibleForTesting;

/**
 * Simple RecylcerViewActivity which hosts a {@link androidx.viewpager.widget.ViewPager} that
 * contains three {@link androidx.fragment.app.Fragment}s. Each Fragment contains a {@link
 * androidx.recyclerview.widget.RecyclerView} with a different layout manager, {@link
 * androidx.recyclerview.widget.LinearLayoutManager}, {@link
 * androidx.recyclerview.widget.GridLayoutManager} and {@link StaggeredGridLayoutManager}
 */
public class RecyclerViewActivity extends FragmentActivity {

  private static final String TAG = "RecyclerViewActivity";

  private ViewPager viewPager;
  private RecyclerViewPagerAdapter recylcerViewPagerAdapter;
  private SlidingTabLayout slidingTabLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.recyclerview_activity);
    initViewPager();
    initSlidingTabLayout();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (null != viewPager) {
      viewPager.setCurrentItem(0);
    }
  }

  @VisibleForTesting
  public void addItems(String newItem) {
    recylcerViewPagerAdapter.addItems(newItem);
  }

  /**
   * The {@link androidx.fragment.app.FragmentPagerAdapter} to display the different
   * {@link androidx.test.ui.app.RecyclerViewFragment}s
   */
  private static class RecyclerViewPagerAdapter extends FragmentPagerAdapter {

    private SparseArray<RecyclerViewFragment> fragmentTracker =
        new SparseArray<RecyclerViewFragment>(3);

    public RecyclerViewPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      RecyclerViewFragment fragment = RecyclerViewFragment
          .newInstance(LayoutManagerType.LINEAR.values()[position]);
      fragmentTracker.put(position, fragment);
      return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      fragmentTracker.remove(position);
      super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
      return LayoutManagerType.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return LayoutManagerType.values()[position].toString();
    }

    public void addItems(String newItem) {
      for (int position = 0; position < getCount(); position++) {
        RecyclerViewFragment fragmentAtPos = fragmentTracker.get(position);
        if (fragmentAtPos != null) {
          fragmentAtPos.addItem(newItem);
        } else {
          Log.w(TAG, "fragmentAtPos " + position + "is null!");
        }
      }
    }

  }

  private void initSlidingTabLayout() {
    slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
    slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
    slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_selected_strip));
    slidingTabLayout.setDistributeEvenly(true);
    slidingTabLayout.setViewPager(viewPager);
  }

  private void initViewPager() {
    viewPager = (ViewPager) findViewById(R.id.rv_view_pager);
    recylcerViewPagerAdapter = new RecyclerViewPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(recylcerViewPagerAdapter);
  }

}
