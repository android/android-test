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

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.ui.app.LongListMatchers.withItemContent;
import static androidx.test.ui.app.LongListMatchers.withItemSize;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** UnitTests for LongListMatchers matcher factory. */
@RunWith(AndroidJUnit4.class)
public final class LongListMatchersTest {

  private ActivityScenario<LongListActivity> activityScenario;

  @Before
  public void setUp() throws Exception {
    activityScenario = ActivityScenario.launch(LongListActivity.class);
  }

  @Test
  public void testWithContent() {
    activityScenario.onActivity(
        activity -> {
          assertThat(activity.makeItem(54), withItemContent("item: 54"));
          assertThat(activity.makeItem(54), withItemContent(endsWith("54")));
          assertFalse(withItemContent("hello world").matches(activity.makeItem(54)));
        });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testWithItemSize() {
    activityScenario.onActivity(
        activity -> {
          assertThat(activity.makeItem(54), withItemSize(8));
          assertThat(activity.makeItem(54), withItemSize(anyOf(equalTo(8), equalTo(7))));
          assertFalse(withItemSize(7).matches(activity.makeItem(54)));
        });
  }
}
