/*
 * Copyright (C) 2018. The Android Open Source Project
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
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IdlingTest {

  @Rule
  public ActivityTestRule<IdlingUIActivity> rule =
      new ActivityTestRule<>(IdlingUIActivity.class, false, false);

  private CountingIdlingResource resource;

  public void register() throws Exception {
    resource = new CountingIdlingResource("counter");
    IdlingRegistry.getInstance().register(resource);
    IdlingUIActivity.listener =
        new IdlingUIActivity.Listener() {
          @Override
          public void onLoadStarted() {
            resource.increment();
          }

          @Override
          public void onLoadFinished() {
            resource.decrement();
          }
        };
  }

  public void unregister() throws Exception {
    IdlingRegistry.getInstance().unregister(resource);
    IdlingUIActivity.listener = null;
  }

  /**
   * Reproduces a user reported error when the same IR is registered and un-registered in @before
   * and @after respectively.
   */
  @Test
  public void verifyRegistrationOfSameResourceIsTakenIntoAccountByEspressoSyncLogic()
      throws Exception {
    register(); // commonly placed ut in @Before
    test();
    unregister(); // commonly placed in @After
    register();
    test();
    unregister();
  }

  private void test() {
    rule.launchActivity(null);
    onView(ViewMatchers.withId(R.id.textView))
        .check(ViewAssertions.matches(withText(IdlingUIActivity.FINISHED)));
    rule.finishActivity();
  }
}
