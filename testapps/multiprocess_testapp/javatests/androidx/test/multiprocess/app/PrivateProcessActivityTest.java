/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.multiprocess.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PrivateProcessActivityTest {

  private static final String PRIVATE_PROC_NAME = "androidx.test.multiprocess.app:PID2";

  @Rule
  public ActivityTestRule<PrivateProcessActivity> rule =
      new ActivityTestRule<>(PrivateProcessActivity.class);

  @Test
  public void sanityTest() {
    Assert.assertNotNull(InstrumentationRegistry.getTargetContext());
  }
  
  @Test
  public void verifyRunningInPrivateProcess() {
    onView(withId(R.id.textPrivateProcessName)).check(matches(withText(PRIVATE_PROC_NAME)));
  }
}
