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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;

import android.content.Intent;
import androidx.core.app.TaskStackBuilder;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TaskStackTest {

  @Before
  public void setUp() throws Exception {
    TaskStackBuilder.create(getApplicationContext())
        .addNextIntent(new Intent(getApplicationContext(), DrawerActivity.class))
        .addNextIntent(new Intent(getApplicationContext(), ToolbarActivity.class))
        .addNextIntent(new Intent(getApplicationContext(), DisplayActivity.class))
        .startActivities();
  }

  @Test
  public void testTaskStack() {
    onView(withText("display activity")).check(matches(isDisplayed()));
    pressBack();
    onView(withText("tool bar activity")).check(matches(isDisplayed()));
    pressBack();
    onView(withText("drawer activity")).check(matches(isDisplayed()));
  }


  @Test
  public void testBackUnconditionallyExitsAppAndDoesNotThrow() {
    onView(withText("display activity")).check(matches(isDisplayed()));
    pressBack();
    pressBack();
    pressBackUnconditionally();
  }
}
