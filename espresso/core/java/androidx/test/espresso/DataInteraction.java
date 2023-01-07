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

import static androidx.test.espresso.DataInteraction.DisplayDataMatcher.displayDataMatcher;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.action.AdapterDataLoaderAction;
import androidx.test.espresso.action.AdapterViewProtocol;
import androidx.test.espresso.action.AdapterViewProtocol.AdaptedData;
import androidx.test.espresso.action.AdapterViewProtocols;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.remote.ConstructorInvocation;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.EspressoOptional;
import javax.annotation.CheckReturnValue;
import kotlin.jvm.functions.Function1;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * An interface to interact with data displayed in AdapterViews.
 *
 * <p>This interface builds on top of {@link ViewInteraction} and should be the preferred way to
 * interact with elements displayed inside AdapterViews.
 *
 * <p>This is necessary because an AdapterView may not load all the data held by its Adapter into
 * the view hierarchy until a user interaction makes it necessary. Also it is more fluent / less
 * brittle to match upon the data object being rendered into the display then the rendering itself.
 *
 * <p>By default, a DataInteraction takes place against any AdapterView found within the current
 * screen, if you have multiple AdapterView objects displayed, you will need to narrow the selection
 * by using the inAdapterView method.
 *
 * <p>The check and perform method operate on the top level child of the adapter view, if you need
 * to operate on a subview (eg: a Button within the list) use the onChildView method before calling
 * perform or check.
 */
public class DataInteraction {

  private final Matcher<? extends Object> dataMatcher;
  private Matcher<View> adapterMatcher = isAssignableFrom(AdapterView.class);
  @Nullable private Matcher<View> childViewMatcher = null;
  @Nullable private Integer atPosition = null;
  private AdapterViewProtocol adapterViewProtocol = AdapterViewProtocols.standardProtocol();
  private Matcher<Root> rootMatcher = RootMatchers.DEFAULT;

  DataInteraction(Matcher<? extends Object> dataMatcher) {
    this.dataMatcher = checkNotNull(dataMatcher);
  }

  /**
   * Causes perform and check methods to take place on a specific child view of the view returned by
   * Adapter.getView()
   */
  @CheckResult
  @CheckReturnValue
  public DataInteraction onChildView(Matcher<View> childMatcher) {
    this.childViewMatcher = checkNotNull(childMatcher);
    return this;
  }

  /** Causes this data interaction to work within the Root specified by the given root matcher. */
  @CheckResult
  @CheckReturnValue
  public DataInteraction inRoot(Matcher<Root> rootMatcher) {
    this.rootMatcher = checkNotNull(rootMatcher);
    return this;
  }

  /**
   * Selects a particular adapter view to operate on, by default we operate on any adapter view on
   * the screen.
   */
  @CheckResult
  @CheckReturnValue
  public DataInteraction inAdapterView(Matcher<View> adapterMatcher) {
    this.adapterMatcher = checkNotNull(adapterMatcher);
    return this;
  }

  /** Selects the view which matches the nth position on the adapter based on the data matcher. */
  @CheckResult
  @CheckReturnValue
  public DataInteraction atPosition(Integer atPosition) {
    this.atPosition = checkNotNull(atPosition);
    return this;
  }

  /**
   * Use a different AdapterViewProtocol if the Adapter implementation does not satisfy the
   * AdapterView contract like (@code ExpandableListView)
   */
  @CheckResult
  @CheckReturnValue
  public DataInteraction usingAdapterViewProtocol(AdapterViewProtocol adapterViewProtocol) {
    this.adapterViewProtocol = checkNotNull(adapterViewProtocol);
    return this;
  }

  /**
   * Performs an action on the view after we force the data to be loaded.
   *
   * @return an {@link ViewInteraction} for more assertions or actions.
   * @throws RuntimeException when being invoked on the main thread.
   */
  public ViewInteraction perform(ViewAction... actions) {
    return onView(makeTargetMatcher()).inRoot(rootMatcher).perform(actions);
  }

  /**
   * Performs an assertion on the state of the view after we force the data to be loaded.
   *
   * @return an {@link ViewInteraction} for more assertions or actions.
   * @throws RuntimeException when being invoked on the main thread.
   */
  public ViewInteraction check(ViewAssertion assertion) {
    return onView(makeTargetMatcher()).inRoot(rootMatcher).check(assertion);
  }

  private Matcher<View> makeTargetMatcher() {
    Matcher<View> targetView =
        displayDataMatcher(
            adapterMatcher, dataMatcher, rootMatcher, atPosition, adapterViewProtocol);
    if (childViewMatcher != null) {
      targetView = allOf(childViewMatcher, isDescendantOfA(targetView));
    }
    return targetView;
  }

  /**
   * Internal matcher that is required for {@link Espresso#onData(Matcher)}.
   *
   * <p>This matcher is only visible to support proto serialization. Do not use this matcher in any
   * Espresso test code!
   */
  public static final class DisplayDataMatcher extends TypeSafeMatcher<View> {
    private static final String TAG = "DisplayDataMatcher";

    @RemoteMsgField(order = 0)
    private final Matcher<View> adapterMatcher;

    @RemoteMsgField(order = 1)
    private final Matcher<? extends Object> dataMatcher;

    @SuppressWarnings("unused") // Used reflectively
    @RemoteMsgField(order = 2)
    private final Class<? extends AdapterViewProtocol> adapterViewProtocolClass;

    @RemoteMsgField(order = 3)
    private final AdapterDataLoaderAction adapterDataLoaderAction;

    private final AdapterViewProtocol adapterViewProtocol;

    @RemoteMsgConstructor
    DisplayDataMatcher(
        @NonNull Matcher<View> adapterMatcher,
        @NonNull Matcher<? extends Object> dataMatcher,
        @NonNull Class<? extends AdapterViewProtocol> adapterViewProtocolClass,
        @NonNull AdapterDataLoaderAction adapterDataLoaderAction)
        throws IllegalAccessException, InstantiationException {
      this(
          adapterMatcher,
          dataMatcher,
          // TODO(b/33008615): MPE does not support root matchers yet, fallback to default for now.
          RootMatchers.DEFAULT,
          adapterViewProtocolClass.cast(
              new ConstructorInvocation(adapterViewProtocolClass, null).invokeConstructor()),
          adapterDataLoaderAction);
    }

    private DisplayDataMatcher(
        @NonNull final Matcher<View> adapterMatcher,
        @NonNull Matcher<? extends Object> dataMatcher,
        @NonNull final Matcher<Root> rootMatcher,
        @NonNull AdapterViewProtocol adapterViewProtocol,
        @NonNull AdapterDataLoaderAction adapterDataLoaderAction) {
      this(
          adapterMatcher,
          dataMatcher,
          adapterViewProtocol,
          adapterDataLoaderAction,
          adapterDataLoaderAction1 ->
              onView(adapterMatcher).inRoot(rootMatcher).perform(adapterDataLoaderAction1));
    }

    @VisibleForTesting
    DisplayDataMatcher(
        @NonNull Matcher<View> adapterMatcher,
        @NonNull Matcher<? extends Object> dataMatcher,
        @NonNull AdapterViewProtocol adapterViewProtocol,
        @NonNull AdapterDataLoaderAction adapterDataLoaderAction,
        @NonNull Function1<AdapterDataLoaderAction, ViewInteraction> loadDataFunction) {
      this.adapterMatcher = checkNotNull(adapterMatcher);
      this.dataMatcher = checkNotNull(dataMatcher);
      this.adapterViewProtocol = checkNotNull(adapterViewProtocol);
      this.adapterViewProtocolClass = adapterViewProtocol.getClass();
      this.adapterDataLoaderAction = checkNotNull(adapterDataLoaderAction);
      // TODO(b/223229374): This return value was unused, but likely should have been used.
      Object unused = checkNotNull(loadDataFunction).invoke(adapterDataLoaderAction);
    }

    /**
     * Returns an instance of {@link DisplayDataMatcher}.
     *
     * <p>Note: This is an internal method, do not call from test code!
     *
     * @param adapterMatcher matcher that matches an {@link AdapterView}
     * @param dataMatcher the data matcher for matching a {@link View} by it's adapter data
     * @param adapterViewProtocol the {@link AdapterViewProtocol} used for this data interaction
     * @deprecated use {@link #displayDataMatcher(Matcher, Matcher, Matcher, Integer,
     *     AdapterViewProtocol)} instead.
     */
    @Deprecated
    public static DisplayDataMatcher displayDataMatcher(
        @NonNull Matcher<View> adapterMatcher,
        @NonNull Matcher<? extends Object> dataMatcher,
        @NonNull Matcher<Root> rootMatcher,
        EspressoOptional<Integer> atPosition,
        @NonNull AdapterViewProtocol adapterViewProtocol) {
      return new DisplayDataMatcher(
          adapterMatcher,
          dataMatcher,
          rootMatcher,
          adapterViewProtocol,
          new AdapterDataLoaderAction(dataMatcher, atPosition, adapterViewProtocol));
    }

    /**
     * Returns an instance of {@link DisplayDataMatcher}.
     *
     * <p>Note: This is an internal method, do not call from test code!
     *
     * @param adapterMatcher matcher that matches an {@link AdapterView}
     * @param dataMatcher the data matcher for matching a {@link View} by it's adapter data
     * @param rootMatcher matcher for view's root
     * @param atPosition optional zero-based position of the data to be matched
     * @param adapterViewProtocol the {@link AdapterViewProtocol} used for this data interaction
     */
    public static DisplayDataMatcher displayDataMatcher(
        @NonNull Matcher<View> adapterMatcher,
        @NonNull Matcher<? extends Object> dataMatcher,
        @NonNull Matcher<Root> rootMatcher,
        @Nullable Integer atPosition,
        @NonNull AdapterViewProtocol adapterViewProtocol) {
      return new DisplayDataMatcher(
          adapterMatcher,
          dataMatcher,
          rootMatcher,
          adapterViewProtocol,
          new AdapterDataLoaderAction(dataMatcher, atPosition, adapterViewProtocol));
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(" displaying data matching: ");
      dataMatcher.describeTo(description);
      description.appendText(" within adapter view matching: ");
      adapterMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      checkState(adapterViewProtocol != null, "adapterViewProtocol cannot be null!");
      ViewParent parent = view.getParent();
      while (parent != null && !(parent instanceof AdapterView)) {
        parent = parent.getParent();
      }
      if (parent != null && adapterMatcher.matches(parent)) {
        AdaptedData data =
            adapterViewProtocol.getDataRenderedByView2(
                (AdapterView<? extends Adapter>) parent, view);
        if (data != null) {
          return data.opaqueToken.equals(adapterDataLoaderAction.getAdaptedData().opaqueToken);
        }
      }
      return false;
    }
  }
}
