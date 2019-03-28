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

import static androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewFinder;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.EspressoOptional;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Provider;
import org.hamcrest.Matcher;

/** Implementation of {@link ViewFinder}. */
// TODO: in the future we may want to collect stats here about the size of the view
// hierarchy, average matcher execution time, warn when matchers take too long to execute, etc.
public final class ViewFinderImpl implements ViewFinder {

  private final Matcher<View> viewMatcher;
  private final Provider<View> rootViewProvider;

  @Inject
  ViewFinderImpl(Matcher<View> viewMatcher, Provider<View> rootViewProvider) {
    this.viewMatcher = viewMatcher;
    this.rootViewProvider = rootViewProvider;
  }

  @Override
  public View getView() throws AmbiguousViewMatcherException, NoMatchingViewException {
    checkMainThread();
    final Predicate<View> matcherPredicate =
        new MatcherPredicateAdapter<View>(checkNotNull(viewMatcher));

    View root = rootViewProvider.get();
    Iterator<View> matchedViewIterator =
        Iterables.filter(breadthFirstViewTraversal(root), matcherPredicate).iterator();

    View matchedView = null;

    while (matchedViewIterator.hasNext()) {
      if (matchedView != null) {
        // Ambiguous!
        throw new AmbiguousViewMatcherException.Builder()
            .withViewMatcher(viewMatcher)
            .withRootView(root)
            .withView1(matchedView)
            .withView2(matchedViewIterator.next())
            .withOtherAmbiguousViews(Iterators.toArray(matchedViewIterator, View.class))
            .build();
      } else {
        matchedView = matchedViewIterator.next();
      }
    }
    if (null == matchedView) {
      final Predicate<View> adapterViewPredicate =
          new MatcherPredicateAdapter<View>(ViewMatchers.isAssignableFrom(AdapterView.class));
      List<View> adapterViews =
          Lists.newArrayList(
              Iterables.filter(breadthFirstViewTraversal(root), adapterViewPredicate).iterator());
      if (adapterViews.isEmpty()) {
        throw new NoMatchingViewException.Builder()
            .withViewMatcher(viewMatcher)
            .withRootView(root)
            .build();
      }

      String warning =
          String.format(
              Locale.ROOT,
              "\n"
                  + "If the target view is not part of the view hierarchy, you may need to use"
                  + " Espresso.onData to load it from one of the following AdapterViews:%s",
              Joiner.on("\n- ").join(adapterViews));
      throw new NoMatchingViewException.Builder()
          .withViewMatcher(viewMatcher)
          .withRootView(root)
          .withAdapterViews(adapterViews)
          .withAdapterViewWarning(EspressoOptional.of(warning))
          .build();
    } else {
      return matchedView;
    }
  }

  private void checkMainThread() {
    checkState(
        Thread.currentThread().equals(Looper.getMainLooper().getThread()),
        "Executing a query on the view hierarchy outside of the main thread (on: %s)",
        Thread.currentThread().getName());
  }

  private static class MatcherPredicateAdapter<T> implements Predicate<T> {
    private final Matcher<? super T> matcher;

    private MatcherPredicateAdapter(Matcher<? super T> matcher) {
      this.matcher = checkNotNull(matcher);
    }

    @Override
    public boolean apply(T input) {
      return matcher.matches(input);
    }
  }
}
