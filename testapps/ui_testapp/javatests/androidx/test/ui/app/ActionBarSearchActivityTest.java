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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.test.ActivityInstrumentationTestCase2;
import androidx.test.filters.LargeTest;

/**
 * Demonstrates Espresso with action bar and app compat searchview widget
 */
@LargeTest
public class ActionBarSearchActivityTest extends
  ActivityInstrumentationTestCase2<ActionBarSearchActivity> {

  @SuppressWarnings("deprecation")
  public ActionBarSearchActivityTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("androidx.test.ui.app", ActionBarSearchActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Espresso will not launch our activity for us, we must launch it via getActivity().
    getActivity();
  }

  @SuppressWarnings("unchecked")
  public void testAppCompatSearchViewFromActionBar() {
    onView(withId(R.id.menu_search))
        .perform(click());

    // App Compat SearchView widget does not use the same id as in the regular
    // android.widget.SearchView. R.id.search_src_text is the id created by appcompat
    // search widget.
    onView(withId(R.id.search_src_text))
        .perform(typeText("Hello World"));
  }
}

