/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.junit.Assert.assertTrue;

import android.util.Log;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Responsible to test the fact that all activities are stopped before and after each test. Ideally,
 * each test in this test class should run in the same instrumentation invocation to provide any
 * value.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ActivityFinisherTest {

  public static final String TAG = "ActivityFinisherTest";

  public final ActivityTestRule<SimpleActivity> rule =
      new ActivityTestRule<>(SimpleActivity.class, false, false);

  @Before
  public void setUp() throws Exception {
    rule.launchActivity(null);
    Log.d(TAG, "setUp " + rule.getActivity());
  }

  @After
  public void tearDown() throws Exception {
    Log.d(TAG, "tearDown  " + this);
  }

  private void assertCounter() {
    onView(isRoot())
        .perform(
            new ViewAction() {

              @Override
              public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
              }

              @Override
              public String getDescription() {
                return "blah";
              }

              @Override
              public void perform(UiController uiController, View view) {
                assertTrue(SimpleActivity.counter == 1);
              }
            });
  }

  @Test
  public void test1() {
    assertCounter();
  }

  @Test
  public void test2() {
    assertCounter();
  }

  @Test
  public void test3() {
    assertCounter();
  }

  @Test
  public void test4() {
    assertCounter();
  }

  @Test
  public void test5() {
    assertCounter();
  }

  @Test
  public void test6() {
    assertCounter();
  }

  @Test
  public void test7() {
    assertCounter();
  }

  @Test
  public void test8() {
    assertCounter();
  }

  @Test
  public void test9() {
    assertCounter();
  }

  @Test
  public void test10() {
    assertCounter();
  }

  @Test
  public void test11() {
    assertCounter();
  }

  @Test
  public void test12() {
    assertCounter();
  }

  @Test
  public void test13() {
    assertCounter();
  }

  @Test
  public void test14() {
    assertCounter();
  }

  @Test
  public void test15() {
    assertCounter();
  }

  @Test
  public void test16() {
    assertCounter();
  }

  @Test
  public void test17() {
    assertCounter();
  }

  @Test
  public void test18() {
    assertCounter();
  }

  @Test
  public void test19() {
    assertCounter();
  }

  @Test
  public void test20() {
    assertCounter();
  }

  @Test
  public void test21() {
    assertCounter();
  }

  @Test
  public void test22() {
    assertCounter();
  }

  @Test
  public void testActivityRestart() {
    rule.finishActivity();
    rule.launchActivity(null);
    rule.finishActivity();
    rule.launchActivity(null);
    rule.finishActivity();
    rule.launchActivity(null);
  }
}
