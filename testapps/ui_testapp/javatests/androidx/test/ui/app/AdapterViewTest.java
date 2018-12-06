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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.ui.app.LongListMatchers.isFooter;
import static androidx.test.ui.app.LongListMatchers.withItemContent;
import static androidx.test.ui.app.LongListMatchers.withItemSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Demonstrates the usage of {@link androidx.test.espresso.Espresso#onData(org.hamcrest.Matcher)} to
 * match data within list views.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdapterViewTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(LongListActivity.class);
  }

  @Test
  public void testClickOnItem50() {
    // The text view "item: 50" may not exist if we haven't scrolled to it.
    // By using onData api we tell Espresso to look into the Adapter for an item matching
    // the matcher we provide it. Espresso will then bring that item into the view hierarchy
    // and we can click on it.

    onData(withItemContent("item: 50"))
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("50")));
  }

  @Test
  public void testClickOnSpecificChildOfRow60() {
    onData(withItemContent("item: 60"))
      .onChildView(withId(R.id.item_size)) // resource id of second column from xml layout
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("60")));

    onView(withId(R.id.selection_column_value))
      .check(matches(withText("2")));
  }

  @Test
  public void testClickOnFirstAndFifthItemOfLength8() {
    onData(is(withItemSize(8)))
      .atPosition(0)
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("10")));

    onData(is(withItemSize(8)))
      .atPosition(4)
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("14")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testClickFooter() {
    onData(isFooter())
      .perform(click());

    onView(withId(R.id.selection_row_value))
      .check(matches(withText("100")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDataItemNotInAdapter() {
    onView(withId(R.id.list))
      .check(matches(not(withAdaptedData(withItemContent("item: 168")))));
  }

  private static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
    return new TypeSafeMatcher<View>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("with class name: ");
        dataMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(View view) {
        if (!(view instanceof AdapterView)) {
          return false;
        }
        @SuppressWarnings("rawtypes")
        Adapter adapter = ((AdapterView) view).getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
          if (dataMatcher.matches(adapter.getItem(i))) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
