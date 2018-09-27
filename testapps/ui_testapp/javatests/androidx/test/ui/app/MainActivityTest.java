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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.Matcher;

/**
 * Demonstrates Espresso with action bar and app compat searchview widget
 */
@LargeTest
public class MainActivityTest extends
  ActivityInstrumentationTestCase2<MainActivity> {

  @SuppressWarnings("deprecation")
  public MainActivityTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("androidx.test.ui.app", MainActivity.class);
  }

  MainActivity underTest;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Espresso will not launch our activity for us, we must launch it via getActivity().
    underTest = getActivity();
  }

  private static class ScrapeListView implements ViewAction {

    private AtomicReference<List<String>> toReturn = new AtomicReference<List<String>>(null);
    @Override
    public Matcher<View> getConstraints() {
      return instanceOf(ListView.class);
    }

    @Override
    public String getDescription() {
      return "get the data in  alist view";
    }

    @Override
    public void perform(UiController uiController, View view) {
      ListView lv  = (ListView) view;
      List<String> contents = Lists.newArrayList();
      for (int idx = 0; idx < lv.getAdapter().getCount(); idx++) {
        Map<String, Object> itemData = (Map<String, Object>) lv.getAdapter().getItem(idx);
        String title = (String) itemData.get("title");
        if ("TaskStackActivity".equals(title)
            || "TransitionActivityMain".equals(title)
            || "SimpleActivity".equals(title)) {
          continue;
        } else if ("FragmentStack".equals(title)
            && VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH) {
          continue;
        }
        contents.add(title);
      }
      toReturn.set(contents);
    }
  }

  @SdkSuppress(maxSdkVersion = 23) // b/29833613
  @SuppressWarnings("unchecked")
  public void testOpenAllActivities() throws Exception {
    ScrapeListView slv = new ScrapeListView();

    onView(withId(android.R.id.list))
        .perform(slv);

    for (String content : slv.toReturn.get()) {
      onData(hasEntry(equalTo("title"), equalTo(content)))
          .perform(click());
      pressBack();
      if ("ActionBarTestActivity".equals(content)) {
        pressBack();
      }
    }
  }
}

