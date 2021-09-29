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

package androidx.test.espresso.base;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;

import androidx.annotation.NonNull;
import android.view.View;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.MainActivity;
import junit.framework.AssertionFailedError;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Tests Espresso's default failure handling. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class DefaultFailureHandlerTest {

  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  @Rule public ExpectedException expectedException = none();

  @Test
  public void mismatchInCheck() {
    expectedException.expect(AssertionFailedError.class);
    expectedException.expect(stackTraceContainsThisClass());
    onView(isRoot()).check(matches(not(isDisplayed())));
  }

  @Test
  public void customAssertionError() {
    expectedException.expect(AssertionFailedError.class);
    expectedException.expectCause(stackTraceContainsThisClass());
    onView(isRoot())
        .check(
            new ViewAssertion() {
              @Override
              public void check(View view, NoMatchingViewException noViewFoundException) {
                fail();
              }
            });
  }

  /**
   * Test only passes if run in isolation. Unless Gradle supports a single instrumentation per test
   * this test is ignored"
   */
  @Test
  public void noMatchingViewException() {
    expectedException.expect(NoMatchingViewException.class);
    expectedException.expect(stackTraceContainsThisClass());
    onView(withMatchesThatReturns(false)).check(matches(not(isDisplayed())));
  }

  @Test
  public void ambiguousViewMatcherException() {
    expectedException.expect(AmbiguousViewMatcherException.class);
    expectedException.expect(stackTraceContainsThisClass());
    onView(withMatchesThatReturns(true)).check(matches(isDisplayed()));
  }

  private static Matcher<View> withMatchesThatReturns(final boolean returnValue) {
    return new BaseMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("matches=" + returnValue);
      }

      @Override
      public boolean matches(Object item) {
        return returnValue;
      }
    };
  }

  @NonNull
  private static CustomTypeSafeMatcher<Throwable> stackTraceContainsThisClass() {
    return new CustomTypeSafeMatcher<Throwable>("Stack Contains This Class") {
      @Override
      protected boolean matchesSafely(Throwable e) {
        return getStackTraceAsString(e).contains(DefaultFailureHandlerTest.class.getSimpleName());
      }
    };
  }
}
