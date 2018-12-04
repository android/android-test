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

package androidx.test.espresso;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link AmbiguousViewMatcherException}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class AmbiguousViewMatcherExceptionTest {
  private Matcher<View> alwaysTrueMatcher;

  private RelativeLayout testView;
  private View child1;
  private View child2;
  private View child3;
  private View child4;

  @Before
  public void setUp() throws Exception {
    alwaysTrueMatcher = notNullValue(View.class);
    testView = new RelativeLayout(getInstrumentation().getContext());
    child1 = new TextView(getInstrumentation().getContext());
    child1.setId(1);
    child2 = new TextView(getInstrumentation().getContext());
    child2.setId(2);
    child3 = new TextView(getInstrumentation().getContext());
    child3.setId(3);
    child4 = new TextView(getInstrumentation().getContext());
    child4.setId(4);
    testView.addView(child1);
    testView.addView(child2);
    testView.addView(child3);
    testView.addView(child4);
  }

  @Test
  public void exceptionContainsMatcherDescription() {
    StringBuilder matcherDescription = new StringBuilder();
    alwaysTrueMatcher.describeTo(new StringDescription(matcherDescription));
    assertThat(createException().getMessage(), containsString(matcherDescription.toString()));
  }

  @Test
  public void exceptionContainsView() {
    String exceptionMessage = createException().getMessage();

    assertThat(
        "missing elements",
        exceptionMessage,
        allOf(
            containsString("{id=1,"), // child1
            containsString("{id=2,"), // child2
            containsString("{id=3,"), // child3
            containsString("{id=4,"), // child4
            containsString("{id=-1,"))); // root
  }

  private AmbiguousViewMatcherException createException() {

    return new AmbiguousViewMatcherException.Builder()
        .withViewMatcher(alwaysTrueMatcher)
        .withRootView(testView)
        .withView1(testView)
        .withView2(child1)
        .withOtherAmbiguousViews(child2, child3, child4)
        .build();
  }
}
