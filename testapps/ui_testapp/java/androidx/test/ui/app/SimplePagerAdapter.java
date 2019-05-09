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

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;

class SimplePagerAdapter extends PagerAdapter {

  private static final int[] COLORS = {
    Color.BLUE,
    Color.RED,
    Color.YELLOW,
  };

  private static final int NUM_PAGES = COLORS.length;

  @Override
  public int getCount() {
    return NUM_PAGES;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public int getItemPosition(Object object) {
    return ((ViewGroup) ((View) object).getParent()).indexOfChild((View) object);
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    LayoutInflater inflater = LayoutInflater.from(container.getContext());
    View view = inflater.inflate(R.layout.pager_view, null);
    ((TextView) view.findViewById(R.id.pager_content)).setText("Position #" + position);
    view.setBackgroundColor(COLORS[position]);
    container.addView(view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }
}
