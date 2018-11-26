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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.RecyclerViewFragment.LayoutManagerType;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * {@link RecyclerViewIntegrationTest}s for {@link androidx.recyclerview.widget.RecyclerView} using a
 * {@link 	androidx.recyclerview.widget.StaggeredGridLayoutManager}
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecyclerViewStaggeredGridLayoutManagerTest extends RecyclerViewIntegrationTest {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    // Click on the GridLayoutManager Tab.
    onView(withText("STAGGERED")).perform(click());
    onView(withId(getRVLayoutId())).check(matches(isCompletelyDisplayed()));
  }

  @Override
  protected int getRVLayoutId() {
    return LayoutManagerType.STAGGERED.getRVId();
  }

  @Override
  protected int getSelectedItemId() {
    return LayoutManagerType.STAGGERED.getSelectedItemId();
  }
}
