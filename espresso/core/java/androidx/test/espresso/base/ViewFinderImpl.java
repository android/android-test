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
import static androidx.test.internal.util.Checks.checkMainThread;
import static androidx.test.internal.util.Checks.checkNotNull;

import android.view.View;
import android.widget.AdapterView;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewFinder;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.IterablesKt;
import androidx.test.espresso.util.Iterators;
import androidx.test.espresso.util.StringJoinerKt;
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
    checkNotNull(viewMatcher);

    View root = rootViewProvider.get();
    Iterator<View> matchedViewIterator =
        IterablesKt.filter(breadthFirstViewTraversal(root), viewMatcher).iterator();
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
      List<View> adapterViews =
          IterablesKt.filterToList(
              breadthFirstViewTraversal(root), ViewMatchers.isAssignableFrom(AdapterView.class));

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
              StringJoinerKt.joinToString(adapterViews, "\n- "));
      throw new NoMatchingViewException.Builder()
          .withViewMatcher(viewMatcher)
          .withRootView(root)
          .withAdapterViews(adapterViews)
          .withAdapterViewWarning(warning)
          .build();
    } else {
      return matchedView;
    }
  }
}
