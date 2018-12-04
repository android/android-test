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

package androidx.test.espresso.assertion;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static androidx.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.rules.ExpectedException.none;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Unit tests for {@link ViewAssertions}. */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ViewAssertionsTest {

  @Rule public ExpectedException expectedException = none();

  private View presentView;
  private View absentView;
  private NoMatchingViewException absentException;
  private NoMatchingViewException presentException;
  private Matcher<View> alwaysAccepts;
  private Matcher<View> alwaysFails;
  private Matcher<View> nullViewMatcher;
  private Context mTargetContext;

  @Before
  public void setUp() throws Exception {
    mTargetContext = getApplicationContext();
    presentView = new View(mTargetContext);
    absentView = null;
    absentException = null;
    alwaysAccepts = is(presentView);
    alwaysFails = not(is(presentView));
    nullViewMatcher = nullValue(View.class);

    presentException =
        new NoMatchingViewException.Builder()
            .withViewMatcher(alwaysFails)
            .withRootView(new View(mTargetContext))
            .build();
  }

  @Test
  public void viewPresent_MatcherFail() {
    expectedException.expect(AssertionFailedError.class);
    matches(alwaysFails).check(presentView, absentException);
  }

  @Test
  public void viewPresent_MatcherPass() {
    matches(alwaysAccepts).check(presentView, absentException);
  }

  @Test
  public void viewAbsent_Unexpectedly() {
    expectedException.expect(NoMatchingViewException.class);
    matches(alwaysAccepts).check(absentView, presentException);
  }

  @Test
  public void viewAbsent_AndThatsWhatIWant() {
    expectedException.expect(NoMatchingViewException.class);
    matches(nullViewMatcher).check(absentView, presentException);
  }

  @Test
  public void selectedDescendantsMatch_ThereAreNone() {
    View grany = setUpViewHierarchy();
    selectedDescendantsMatch(withText("welfjkw"), hasContentDescription())
        .check(grany, absentException);
  }

  @Test
  public void selectedDescendantsMatch_SelectedDescendantsMatch() {
    View grany = setUpViewHierarchy();
    selectedDescendantsMatch(withText("has content description"), hasContentDescription())
        .check(grany, absentException);
  }

  @Test
  public void selectedDescendantsMatch_SelectedDescendantsDoNotMatch() {
    View grany = setUpViewHierarchy();
    expectedException.expect(AssertionFailedError.class);
    selectedDescendantsMatch(withText("no content description"), hasContentDescription())
        .check(grany, absentException);
  }

  @Test
  public void selectedDescendantsMatch_SelectedDescendantsMatchAndDoNotMatch() {
    View grany = setUpViewHierarchy();
    expectedException.expect(AssertionFailedError.class);
    selectedDescendantsMatch(isAssignableFrom(TextView.class), hasContentDescription())
        .check(grany, absentException);
  }

  private View setUpViewHierarchy() {
    TextView v1 = new TextView(mTargetContext);
    v1.setText("no content description");
    TextView v2 = new TextView(mTargetContext);
    v2.setText("has content description");
    v2.setContentDescription("content description");
    ViewGroup parent = new RelativeLayout(mTargetContext);
    View grany = new ScrollView(mTargetContext);
    ((ViewGroup) grany).addView(parent);
    parent.addView(v1);
    parent.addView(v2);

    return grany;
  }
}
