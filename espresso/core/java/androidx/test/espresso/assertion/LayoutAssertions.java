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

import static androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static androidx.test.espresso.matcher.LayoutMatchers.hasEllipsizedText;
import static androidx.test.espresso.matcher.LayoutMatchers.hasMultilineText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.util.HumanReadables.describe;
import static androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import android.graphics.Rect;
import androidx.annotation.VisibleForTesting;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;

/** A collection of layout {@link ViewAssertion}s. */
public final class LayoutAssertions {

  private LayoutAssertions() {}

  /**
   * Returns a {@link ViewAssertion} that asserts that view hierarchy does not contain ellipsized or
   * cut off text views.
   */
  public static ViewAssertion noEllipsizedText() {
    return selectedDescendantsMatch(isAssignableFrom(TextView.class), not(hasEllipsizedText()));
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view hierarchy does not contain multiline
   * buttons.
   */
  public static ViewAssertion noMultilineButtons() {
    return selectedDescendantsMatch(isAssignableFrom(Button.class), not(hasMultilineText()));
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that descendant views matching the selector do not
   * overlap each other.
   *
   * <p>Example: {@code onView(rootView).check(noOverlaps(isAssignableFrom(TextView.class));}
   */
  public static ViewAssertion noOverlaps(final Matcher<View> selector) {
    return new NoOverlapsViewAssertion(checkNotNull(selector));
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that descendant objects assignable to TextView or
   * ImageView do not overlap each other.
   *
   * <p>Example: {@code onView(rootView).check(noOverlaps());}
   */
  public static ViewAssertion noOverlaps() {
    return noOverlaps(
        allOf(
            withEffectiveVisibility(Visibility.VISIBLE),
            anyOf(isAssignableFrom(TextView.class), isAssignableFrom(ImageView.class))));
  }

  private static Rect getRect(View view) {
    int[] coords = {0, 0};
    view.getLocationOnScreen(coords);
    return new Rect(
        coords[0], coords[1], coords[0] + view.getWidth() - 1, coords[1] + view.getHeight() - 1);
  }

  @VisibleForTesting
  static class NoOverlapsViewAssertion implements ViewAssertion {
    @RemoteMsgField(order = 0)
    private final Matcher<View> selector;

    @RemoteMsgConstructor
    private NoOverlapsViewAssertion(Matcher<View> selector) {
      this.selector = selector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void check(View view, NoMatchingViewException noViewException) {

      final Predicate<View> viewPredicate =
          new Predicate<View>() {
            @Override
            public boolean apply(View input) {
              return selector.matches(input);
            }
          };

      if (noViewException != null) {
        throw noViewException;
      }

      Iterator<View> selectedViewIterator =
          Iterables.filter(breadthFirstViewTraversal(view), viewPredicate).iterator();

      List<View> prevViews = new LinkedList<>();
      StringBuilder errorMessage = new StringBuilder();
      while (selectedViewIterator.hasNext()) {
        View selectedView = selectedViewIterator.next();
        Rect viewRect = getRect(selectedView);
        if (!viewRect.isEmpty()
            && !(selectedView instanceof TextView
                && ((TextView) selectedView).getText().length() == 0)) {
          for (View prevView : prevViews) {
            // Mutual intersection of ImageViews is acceptable in most cases.
            if (selectedView instanceof ImageView && prevView instanceof ImageView) {
              continue;
            }
            Rect prevRect = getRect(prevView);
            if (Rect.intersects(viewRect, prevRect)) {
              // Overlap detected, add to the error message
              if (errorMessage.length() > 0) {
                errorMessage.append(",\n\n");
              }
              errorMessage.append(
                  String.format(
                      Locale.ROOT, "%s overlaps\n%s", describe(selectedView), describe(prevView)));
              break;
            }
          }
          prevViews.add(selectedView);
        }
      }

      if (errorMessage.length() > 0) {
        throw new AssertionFailedError(errorMessage.toString());
      }
    }

    @Override
    public String toString() {
      return String.format(Locale.ROOT, "NoOverlapsViewAssertion{selector=%s}", selector);
    }
  }
}
