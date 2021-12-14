/*
 * Copyright (C) 2021 The Android Open Source Project
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test cases generates a large view hierarchy, enough for it to be truncated. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TruncatedViewHierarchyActivityTest {

  /**
   * Creates an activity with 2 levels of views such that the view hierarchy dump is fairly long yet
   * does not get truncated.
   *
   * <p>This fails with NoMatchingViewException and dumps the view hierarchy as well as the stack
   * trace of where the view matcher failed.
   */
  @Test
  public void testViewHierarchyNotTruncated() {
    Intent intent =
        Intent.makeMainActivity(
            new ComponentName(
                getInstrumentation().getTargetContext(), TruncatedViewHierarchyActivity.class));
    intent.putExtra(TruncatedViewHierarchyActivity.LEVEL_INTENT_KEY, 2);
    try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
      assertNotNull(scenario);
      // The requested withId() is not in the layout.
      // This fails with NoMatchingViewException and dumps the view hierarchy.
      onView(withId(android.R.id.list)).check(matches(withText("Hello world!")));
    }
  }

  /**
   * Creates an activity with 3 levels of views such that the view hierarchy dump is long enough to
   * be truncated in the log output.
   *
   * <p>This fails with NoMatchingViewException and dumps the view hierarchy as well as the stack
   * trace of where the view matcher failed.
   */
  @Test
  public void testViewHierarchyIsTruncated() {
    Intent intent =
        Intent.makeMainActivity(
            new ComponentName(
                getInstrumentation().getTargetContext(), TruncatedViewHierarchyActivity.class));
    intent.putExtra(TruncatedViewHierarchyActivity.LEVEL_INTENT_KEY, 3);
    try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
      assertNotNull(scenario);
      // The requested withId() is not in the layout.
      // This fails with NoMatchingViewException and dumps the view hierarchy.
      onView(withId(android.R.id.list)).check(matches(withText("Hello world!")));
    }
  }

}
