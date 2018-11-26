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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.RecyclerViewFragment.LayoutManagerType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link RecyclerViewIntegrationTest}s for {@link androidx.recyclerview.widget.RecyclerView} using a
 * {@link androidx.recyclerview.widget.LinearLayoutManager}
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecyclerViewLinearLayoutManagerTest extends RecyclerViewIntegrationTest {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    onView(withId(getRVLayoutId())).check(matches(isDisplayed()));
  }

  @Override
  protected int getRVLayoutId() {
    return LayoutManagerType.LINEAR.getRVId();
  }

  @Override
  protected int getSelectedItemId() {
    return LayoutManagerType.LINEAR.getSelectedItemId();
  }

  @Test
  public void testScrolling_scrollToPosition() {
    onView(withId(getRVLayoutId())).perform(scrollToPosition(50));
    recyclerViewActivityScenario.onActivity(
        activity -> {
          RecyclerView appList = (RecyclerView) activity.findViewById(getRVLayoutId());
          LinearLayoutManager layoutManager = (LinearLayoutManager) appList.getLayoutManager();
          // If scrollToPosition does not wait for the main thread to be idle then this is 0.
          int scrollPosition = layoutManager.findFirstVisibleItemPosition();
          assertThat(scrollPosition).isNotEqualTo(0);
        });
  }
}
