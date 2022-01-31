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
import android.view.ViewGroup;
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
  private View child5;
  private View child6;

  @Before
  public void setUp() throws Exception {
    alwaysTrueMatcher = notNullValue(View.class);
    testView = new RelativeLayout(getInstrumentation().getContext());
    child1 = createView(testView, 1);
    child2 = createView(testView, 2);
    child3 = createView(testView, 3);
    child4 = createView(testView, 4);
    child5 = createView(testView, 5);
    child6 = createView(testView, 6);
  }


  @Test
  public void exceptionContainsMatcherDescription() {
    StringBuilder matcherDescription = new StringBuilder();
    alwaysTrueMatcher.describeTo(new StringDescription(matcherDescription));
    assertThat(createException4Views().getMessage(), containsString(matcherDescription.toString()));
  }

  @Test
  public void exceptionContainsView() {
    String exceptionMessage = createException4Views().getMessage();

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

  @Test
  public void exceptionMessageWithViewList() {
    String exceptionMessage = createException4Views().getMessage();

    assertThat(
        exceptionMessage.replaceAll("\\{[^}]+\\}", "{...}").replaceAll("[\n\r]+", "\n"),
        containsString(
            "'not null' matches 5 views in the hierarchy:\n"
                + "- [1] RelativeLayout{...}\n"
                + "- [2] TextView{...}\n"
                + "- [3] TextView{...}\n"
                + "- [4] TextView{...}\n"
                + "- [5] TextView{...}\n"
                + "Problem views are marked with '****MATCHES****' below."));
  }

  @Test
  public void exceptionMessageWithTruncatedViewList() {
    String exceptionMessage = createException6Views().getMessage();

    assertThat(
        exceptionMessage.replaceAll("\\{[^}]+\\}", "{...}").replaceAll("[\n\r]+", "\n"),
        containsString(
            "'not null' matches 7 views in the hierarchy:\n"
                + "- [1] RelativeLayout{...}\n"
                + "- [2] TextView{...}\n"
                + "- [3] TextView{...}\n"
                + "- [4] TextView{...}\n"
                + "- [5] TextView{...}\n"
                + "- [truncated, listing 5 out of 7 views].\n"
                + "Problem views are marked with '****MATCHES****' below."));
  }

  private static View createView(ViewGroup parent, int index) {
    View v = new TextView(getInstrumentation().getContext());
    v.setId(index);
    parent.addView(v);
    return v;
  }

  private AmbiguousViewMatcherException createException4Views() {
    return new AmbiguousViewMatcherException.Builder()
        .withViewMatcher(alwaysTrueMatcher)
        .withRootView(testView)
        .withView1(testView)
        .withView2(child1)
        .withOtherAmbiguousViews(child2, child3, child4)
        .build();
  }

  private AmbiguousViewMatcherException createException6Views() {
    return new AmbiguousViewMatcherException.Builder()
        .withViewMatcher(alwaysTrueMatcher)
        .withRootView(testView)
        .withView1(testView)
        .withView2(child1)
        .withOtherAmbiguousViews(child2, child3, child4, child5, child6)
        .build();
  }
}
