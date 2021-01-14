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
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;

import androidx.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.HumanReadables;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/** Collection of common {@link ViewAssertion ViewAssertions}. */
public final class ViewAssertions {

  private static final String TAG = ViewAssertions.class.getSimpleName();

  private ViewAssertions() {}

  /**
   * Returns an assert that ensures the view matcher does not find any matching view in the
   * hierarchy.
   */
  public static ViewAssertion doesNotExist() {
    return new DoesNotExistViewAssertion();
  }

  /**
   * Returns a generic {@link ViewAssertion} that asserts that a view exists in the view hierarchy
   * and is matched by the given view matcher.
   */
  public static ViewAssertion matches(final Matcher<? super View> viewMatcher) {
    return new MatchesViewAssertion(checkNotNull(viewMatcher));
  }

  /**
   * Returns a generic {@link ViewAssertion} that asserts that a view exists in the view hierarchy
   * and is matched by the given view matcher. Additionally, allows using a custom {@link Logger}
   * which will be used in place of {@link Log}. If {@code null} is provided for the logger, then no
   * logging will take place.
   *
   * @param viewMatcher The {@link Matcher} used to test the {@link View}.
   * @param logger The {@link Logger} or {@code null} to use for any error logging.
   * @return An instance of {@link ViewAssertion} to use for testing {@link View}s.
   */
  public static ViewAssertion matches(
      final Matcher<? super View> viewMatcher, final Logger logger) {
    return new MatchesViewAssertion(checkNotNull(viewMatcher), logger);
  }

  /**
   * Returns a generic {@link ViewAssertion} that asserts that the descendant views selected by the
   * selector match the specified matcher.
   *
   * <p>Example: {@code
   * onView(rootView).check(selectedDescendantsMatch(not(isAssignableFrom(TextView.class)),
   * hasContentDescription()))};
   */
  public static ViewAssertion selectedDescendantsMatch(
      final Matcher<View> selector, final Matcher<View> matcher) {
    return new SelectedDescendantsMatchViewAssertion(selector, matcher);
  }

  @VisibleForTesting
  static class MatchesViewAssertion implements ViewAssertion {
    @VisibleForTesting
    @RemoteMsgField(order = 0)
    final Matcher<? super View> viewMatcher;

    private final Logger logger;
    private final boolean isLoggerSet;

    @RemoteMsgConstructor
    private MatchesViewAssertion(final Matcher<? super View> viewMatcher) {
      this.viewMatcher = viewMatcher;
      this.logger = null;
      this.isLoggerSet = false;
    }

    private MatchesViewAssertion(final Matcher<? super View> viewMatcher, Logger logger) {
      this.viewMatcher = viewMatcher;
      this.logger = logger;
      this.isLoggerSet = true;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewException) {
      StringDescription description = new StringDescription();
      description.appendText("'");
      viewMatcher.describeTo(description);
      if (noViewException != null) {
        description.appendText(
            String.format(
                Locale.ROOT,
                "' check could not be performed because view '%s' was not found.\n",
                noViewException.getViewMatcherDescription()));
        if (isLoggerSet) {
          if (logger != null) {
            logger.log(Level.SEVERE, description.toString());
          }
        } else {
          Log.e(TAG, description.toString());
        }
        throw noViewException;
      } else {
        description.appendText("' doesn't match the selected view.");
        assertThat(description.toString(), view, viewMatcher);
      }
    }

    @Override
    public String toString() {
      return String.format(Locale.ROOT, "MatchesViewAssertion{viewMatcher=%s}", viewMatcher);
    }
  }

  @VisibleForTesting
  static class DoesNotExistViewAssertion implements ViewAssertion {
    @RemoteMsgConstructor
    private DoesNotExistViewAssertion() {}

    @Override
    public void check(View view, NoMatchingViewException noView) {
      if (view != null) {
        assertThat(
            "View is present in the hierarchy: " + HumanReadables.describe(view), true, is(false));
      }
    }
  }

  @VisibleForTesting
  static class SelectedDescendantsMatchViewAssertion implements ViewAssertion {
    @RemoteMsgField(order = 0)
    private final Matcher<View> selector;

    @RemoteMsgField(order = 1)
    private final Matcher<View> matcher;

    @RemoteMsgConstructor
    private SelectedDescendantsMatchViewAssertion(
        final Matcher<View> selector, final Matcher<View> matcher) {
      this.selector = selector;
      this.matcher = matcher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void check(View view, NoMatchingViewException noViewException) {
      Preconditions.checkNotNull(view);

      final Predicate<View> viewPredicate =
          new Predicate<View>() {
            @Override
            public boolean apply(View input) {
              return selector.matches(input);
            }
          };

      Iterator<View> selectedViewIterator =
          Iterables.filter(breadthFirstViewTraversal(view), viewPredicate).iterator();

      List<View> nonMatchingViews = new ArrayList<>();
      while (selectedViewIterator.hasNext()) {
        View selectedView = selectedViewIterator.next();

        if (!matcher.matches(selectedView)) {
          nonMatchingViews.add(selectedView);
        }
      }

      if (nonMatchingViews.size() > 0) {
        String errorMessage =
            HumanReadables.getViewHierarchyErrorMessage(
                view,
                nonMatchingViews,
                String.format(
                    Locale.ROOT,
                    "At least one view did not match the required matcher: %s",
                    matcher),
                "****DOES NOT MATCH****");
        throw new AssertionFailedError(errorMessage);
      }
    }

    @Override
    public String toString() {
      return String.format(
          Locale.ROOT,
          "SelectedDescendantsMatchViewAssertion{selector=%s, matcher=%s}",
          selector,
          matcher);
    }
  }
}
