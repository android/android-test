/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.test.espresso.matcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.matcher.MatcherTestUtils.getDescription;
import static androidx.test.espresso.matcher.MatcherTestUtils.getMismatchDescription;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

/** Integration tests for {@link ViewMatchers#isDisplayingAtLeast(int)}. */
// TODO: Use real views instead of mocking.
@LargeTest
@RunWith(AndroidJUnit4.class)
public class IsDisplayingAtLeastIntegrationTest {

  private Context context;

  @Before
  public void setUp() throws Exception {
    context = getApplicationContext();
  }

  @Test
  public void invalidPercentageRange() {
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(-1));
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(101));
  }

  @Test
  public void partiallyDisplayed() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);

    view.setVisibility(View.GONE);
    assertFalse(isDisplayingAtLeast(5).matches(view));

    // Set the view to be 100x100: 10,000 pixels
    view.setVisibility(View.VISIBLE);
    view.layout(0, 0, 100, 100);
    when(providerMock.get(any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // Set the output rectangle to 50x50: 2500 pixels
                  Rect argRect = invocation.getArgument(0);
                  argRect.set(0, 0, 50, 50);
                  return true;
                });

    assertFalse(isDisplayingAtLeast(30).matches(view));
    assertTrue(isDisplayingAtLeast(20).matches(view));
  }

  @Test
  public void description() {
    assertThat(
        getDescription(isDisplayingAtLeast(15)),
        is(
            "("
                + getDescription(withEffectiveVisibility(Visibility.VISIBLE))
                + " and view.getGlobalVisibleRect() covers at least <15> percent of the view's"
                + " area)"));
  }

  @Test
  public void mismatchDescription_wrongVisibility() {
    View view = new View(context);
    view.setVisibility(View.GONE);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is(getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view)));
  }

  @Test
  public void mismatchDescription_notVisible() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);
    view.setVisibility(View.VISIBLE);
    when(providerMock.get(any(), any())).thenReturn(false);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is("view was <0> percent visible to the user"));
  }

  @Test
  public void mismatchDescription_lowVisibility() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);
    view.setVisibility(View.VISIBLE);
    // Set the area of the view to 100x100 = 10,000
    view.layout(0, 0, 100, 100);
    when(providerMock.get(any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // Set the output rectangle to 50x50: 2500 pixels
                  Rect argRect = invocation.getArgument(0);
                  argRect.set(0, 0, 50, 50);
                  return true;
                });
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(35), view),
        is("view was <25> percent visible to the user"));
  }

  /** This interface is used to mock the {@link View#getGlobalVisibleRect(Rect, Point)} method. */
  interface GlobalVisibleRectProvider {
    boolean get(Rect r, Point offset);
  }

  private static class GlobalVisibleRectTestView extends View {

    private final GlobalVisibleRectProvider provider;

    GlobalVisibleRectTestView(Context context, GlobalVisibleRectProvider provider) {
      super(context);
      this.provider = provider;
    }

    @Override
    public final boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
      return provider.get(r, globalOffset);
    }
  }
}
