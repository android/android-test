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

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static androidx.test.internal.util.Checks.checkNotNull;
import static org.hamcrest.Matchers.is;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.IterablesKt;
import androidx.test.espresso.util.Iterators;
import java.util.Iterator;
import java.util.Locale;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * A collection of {@link ViewAssertion}s for checking relative position of elements on the screen.
 *
 * <p>These comparisons are on the x,y plane; they ignore the z plane.
 */
public final class PositionAssertions {

  private static final String TAG = "PositionAssertions";

  private PositionAssertions() {}

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely left of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isCompletelyLeftOf(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.COMPLETELY_LEFT_OF);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely right of the
   * view matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isCompletelyRightOf(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.COMPLETELY_RIGHT_OF);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely left of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   * @deprecated Use {@link #isCompletelyLeftOf(Matcher)} instead.
   */
  @Deprecated
  public static ViewAssertion isLeftOf(Matcher<View> matcher) {
    return isCompletelyLeftOf(matcher);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely right of the
   * view matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   * @deprecated Use {@link #isCompletelyRightOf(Matcher)} instead.
   */
  @Deprecated
  public static ViewAssertion isRightOf(Matcher<View> matcher) {
    return isCompletelyRightOf(matcher);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is partially left of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is no horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isPartiallyLeftOf(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.PARTIALLY_LEFT_OF);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is partially right of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is no horizontal overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isPartiallyRightOf(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.PARTIALLY_RIGHT_OF);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is left aligned with the view
   * matching the given matcher.
   *
   * <p>The left 'x' coordinate of the view displayed must equal the left 'x' coordinate of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if the views are not aligned to the left.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isLeftAlignedWith(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.LEFT_ALIGNED);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is right aligned with the view
   * matching the given matcher.
   *
   * <p>The right 'x' coordinate of the view displayed must equal the right 'x' coordinate of the
   * view matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if the views are not aligned to the right.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isRightAlignedWith(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.RIGHT_ALIGNED);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely above the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isCompletelyAbove(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.COMPLETELY_ABOVE);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely below the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isCompletelyBelow(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.COMPLETELY_BELOW);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is partially above the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is no vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isPartiallyAbove(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.PARTIALLY_ABOVE);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is partially below the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is no vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isPartiallyBelow(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.PARTIALLY_BELOW);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely above the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there is any vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   * @deprecated Use {@link #isCompletelyAbove(Matcher)} instead.
   */
  @Deprecated
  public static ViewAssertion isAbove(Matcher<View> matcher) {
    return isCompletelyAbove(matcher);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is completely below the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if there any vertical overlap.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   * @deprecated Use {@link #isCompletelyBelow(Matcher)} instead.
   */
  @Deprecated
  public static ViewAssertion isBelow(Matcher<View> matcher) {
    return isCompletelyBelow(matcher);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is bottom aligned with the
   * view matching the given matcher.
   *
   * <p>The bottom 'y' coordinate of the view displayed must equal the bottom 'y' coordinate of the
   * view matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if the views are not aligned bottom.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isBottomAlignedWith(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.BOTTOM_ALIGNED);
  }

  /**
   * Returns a {@link ViewAssertion} that asserts that view displayed is top aligned with the view
   * matching the given matcher.
   *
   * <p>The top 'y' coordinate of the view displayed must equal the top 'y' coordinate of the view
   * matching the given matcher.
   *
   * @throws junit.framework.AssertionFailedError if the views are not aligned top.
   * @throws AmbiguousViewMatcherException if more than one view matches the given matcher.
   * @throws NoMatchingViewException if no views match the given matcher.
   */
  public static ViewAssertion isTopAlignedWith(Matcher<View> matcher) {
    return relativePositionOf(matcher, Position.TOP_ALIGNED);
  }

  static ViewAssertion relativePositionOf(
      final Matcher<View> viewMatcher, final Position position) {
    checkNotNull(viewMatcher);
    return new ViewAssertion() {
      @Override
      public void check(final View foundView, NoMatchingViewException noViewException) {
        StringDescription description = new StringDescription();
        if (noViewException != null) {
          description.appendText(
              String.format(
                  Locale.ROOT,
                  "' check could not be performed because view '%s' was not found.\n",
                  noViewException.getViewMatcherDescription()));
          Log.e(TAG, description.toString());
          throw noViewException;
        } else {
          // TODO: describe the foundView matcher instead of the foundView itself.
          description
              .appendText("View:")
              .appendText(HumanReadables.describe(foundView))
              .appendText(" is not ")
              .appendText(position.toString())
              .appendText(" view ")
              .appendText(viewMatcher.toString());
          assertThat(
              description.toString(),
              isRelativePosition(
                  foundView, findView(viewMatcher, getTopViewGroup(foundView)), position),
              is(true));
        }
      }
    };
  }

  // Helper methods
  static View findView(final Matcher<View> toView, View root) {
    checkNotNull(toView);
    checkNotNull(root);
    Iterator<View> matchedViewIterator =
        IterablesKt.filter(breadthFirstViewTraversal(root), toView).iterator();
    View matchedView = null;
    while (matchedViewIterator.hasNext()) {
      if (matchedView != null) {
        // Ambiguous!
        throw new AmbiguousViewMatcherException.Builder()
            .withRootView(root)
            .withViewMatcher(toView)
            .withView1(matchedView)
            .withView2(matchedViewIterator.next())
            .withOtherAmbiguousViews(Iterators.toArray(matchedViewIterator, View.class))
            .build();
      } else {
        matchedView = matchedViewIterator.next();
      }
    }
    if (matchedView == null) {
      throw new NoMatchingViewException.Builder()
          .withViewMatcher(toView)
          .withRootView(root)
          .build();
    }
    return matchedView;
  }

  private static ViewGroup getTopViewGroup(View view) {
    ViewParent currentParent = view.getParent();
    ViewGroup topView = null;
    while (currentParent != null) {
      if (currentParent instanceof ViewGroup) {
        topView = (ViewGroup) currentParent;
      }
      currentParent = currentParent.getParent();
    }
    return topView;
  }

  static boolean isRelativePosition(View view1, View view2, Position position) {
    int[] location1 = new int[2];
    int[] location2 = new int[2];
    view1.getLocationOnScreen(location1);
    view2.getLocationOnScreen(location2);

    switch (position) {
      case COMPLETELY_LEFT_OF:
        return location1[0] + view1.getWidth() <= location2[0];
      case COMPLETELY_RIGHT_OF:
        return location2[0] + view2.getWidth() <= location1[0];
      case COMPLETELY_ABOVE:
        return location1[1] + view1.getHeight() <= location2[1];
      case COMPLETELY_BELOW:
        return location2[1] + view2.getHeight() <= location1[1];
      case PARTIALLY_LEFT_OF:
        return location1[0] < location2[0] && location2[0] < location1[0] + view1.getWidth();
      case PARTIALLY_RIGHT_OF:
        return location2[0] < location1[0] && location1[0] < location2[0] + view2.getWidth();
      case PARTIALLY_ABOVE:
        return location1[1] < location2[1] && location2[1] < location1[1] + view1.getHeight();
      case PARTIALLY_BELOW:
        return location2[1] < location1[1] && location1[1] < location2[1] + view2.getHeight();
      case LEFT_ALIGNED:
        return location1[0] == location2[0];
      case RIGHT_ALIGNED:
        return location1[0] + view1.getWidth() == location2[0] + view2.getWidth();
      case TOP_ALIGNED:
        return location1[1] == location2[1];
      case BOTTOM_ALIGNED:
        return location1[1] + view1.getHeight() == location2[1] + view2.getHeight();
      default:
        return false;
    }
  }

  enum Position {
    COMPLETELY_LEFT_OF("completely left of"),
    COMPLETELY_RIGHT_OF("completely right of"),
    COMPLETELY_ABOVE("completely above"),
    COMPLETELY_BELOW("completely below"),
    PARTIALLY_LEFT_OF("partially left of"),
    PARTIALLY_RIGHT_OF("partially right of"),
    PARTIALLY_ABOVE("partially above"),
    PARTIALLY_BELOW("partially below"),
    LEFT_ALIGNED("aligned left with"),
    RIGHT_ALIGNED("aligned right with"),
    TOP_ALIGNED("aligned top with"),
    BOTTOM_ALIGNED("aligned bottom with");

    private final String positionValue;

    private Position(String value) {
      positionValue = value;
    }

    @Override
    public String toString() {
      return positionValue;
    }
  }
}
