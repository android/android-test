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
import static androidx.test.espresso.assertion.PositionAssertions.findView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.rules.ExpectedException.none;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Test case for {@link PositionAssertions} focusing on failure cases. Please check {@link
 * PositionAssertionsUnitTest} for more unit tests related to getLocationOnScreen.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class PositionAssertionsTest {

  @Rule public ExpectedException expectedException = none();

  private final String text1 = "text1";
  private final String text2 = "text2";

  @Test
  public void findView_Exists() {
    View root = setUpViewHierarchy();
    View foundView = findView(withText(text1), root);
    assertNotNull(foundView);
  }

  @Test
  public void findView_NotFound() {
    View root = setUpViewHierarchy();
    expectedException.expect(NoMatchingViewException.class);
    findView(withText("does not exist"), root);
  }

  @Test
  public void findView_Ambiguous() {
    View root = setUpViewHierarchy();
    expectedException.expect(AmbiguousViewMatcherException.class);
    findView(isAssignableFrom(TextView.class), root);
  }

  private View setUpViewHierarchy() {
    Context targetContext = getApplicationContext();
    TextView v1 = new TextView(targetContext);
    v1.setText(text1);
    TextView v2 = new TextView(targetContext);
    v2.setText(text2);
    v2.setContentDescription("content description");
    ViewGroup parent = new RelativeLayout(targetContext);
    View grany = new ScrollView(targetContext);
    ((ViewGroup) grany).addView(parent);
    parent.addView(v1);
    parent.addView(v2);

    return grany;
  }
}
