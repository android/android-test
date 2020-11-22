/*
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

package androidx.test.espresso.matcher;

import static androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.annotation.Beta;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.HumanReadables;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matchers that match {@link View}s. */
public final class ViewMatchers {
  private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("\\d+");

  private ViewMatchers() {}

  /**
   * Matches {@link View Views} based on instance or subclass of the provided class.
   *
   * <p>Some versions of Hamcrest make the generic typing of this a nightmare, so we have a special
   * case for our users.
   */
  public static Matcher<View> isAssignableFrom(Class<? extends View> clazz) {
    return new IsAssignableFromMatcher(clazz);
  }

  /** Returns a matcher that matches Views with class name matching the given matcher. */
  public static Matcher<View> withClassName(final Matcher<String> classNameMatcher) {
    return new WithClassNameMatcher(checkNotNull(classNameMatcher));
  }

  /**
   * Returns a matcher that matches {@link View Views} that are currently displayed on the screen to
   * the user.
   *
   * <p><b>Note:</b> isDisplayed will select views that are partially displayed (eg: the full
   * height/width of the view is greater than the height/width of the visible rectangle). If you
   * wish to ensure the entire rectangle this view draws is displayed to the user use {@link
   * #isCompletelyDisplayed}.
   */
  public static Matcher<View> isDisplayed() {
    return new IsDisplayedMatcher();
  }

  /**
   * Returns a matcher which only accepts a view whose height and width fit perfectly within the
   * currently displayed region of this view.
   *
   * <p>There exist views (such as ScrollViews) whose height and width are larger then the physical
   * device screen by design. Such views will <b>never</b> be completely displayed.
   */
  public static Matcher<View> isCompletelyDisplayed() {
    return isDisplayingAtLeast(100);
  }

  /**
   * Returns a matcher which accepts a view so long as a given percentage of that view's area is not
   * obscured by any parent view and is thus visible to the user.
   *
   * @param areaPercentage an integer ranging from (0, 100] indicating how much percent of the
   *     surface area of the view must be shown to the user to be accepted.
   */
  public static Matcher<View> isDisplayingAtLeast(final int areaPercentage) {
    checkState(areaPercentage <= 100, "Cannot have over 100 percent: %s", areaPercentage);
    checkState(areaPercentage > 0, "Must have a positive, non-zero value: %s", areaPercentage);
    return new IsDisplayingAtLeastMatcher(areaPercentage);
  }

  /** Returns a matcher that matches {@link View Views} that are enabled. */
  public static Matcher<View> isEnabled() {
    return new IsEnabledMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} that are not enabled. */
  public static Matcher<View> isNotEnabled() {
    return new IsEnabledMatcher(false);
  }

  /** Returns a matcher that matches {@link View Views} that are focusable. */
  public static Matcher<View> isFocusable() {
    return new IsFocusableMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} that are not focusable. */
  public static Matcher<View> isNotFocusable() {
    return new IsFocusableMatcher(false);
  }

  /** Returns a matcher that matches {@link View Views} that are focused. */
  public static Matcher<View> isFocused() {
    return new IsFocusedMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} that are not focused. */
  public static Matcher<View> isNotFocused() {
    return new IsFocusedMatcher(false);
  }

  /** Returns a matcher that matches {@link View Views} currently have focus. */
  public static Matcher<View> hasFocus() {
    return new HasFocusMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} currently do not have focus. */
  public static Matcher<View> doesNotHaveFocus() {
    return new HasFocusMatcher(false);
  }

  /** Returns a matcher that matches {@link View Views} that are selected. */
  public static Matcher<View> isSelected() {
    return new IsSelectedMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} that are not selected. */
  public static Matcher<View> isNotSelected() {
    return new IsSelectedMatcher(false);
  }

  /**
   * Returns an <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   * <code>Matcher</code></a> that matches {@link View Views} based on their siblings.
   *
   * <p>This may be particularly useful when a view cannot be uniquely selected on properties such
   * as text or {@code R.id}. For example: a call button is repeated several times in a contacts
   * layout and the only way to differentiate the call button view is by what appears next to it
   * (e.g. the unique name of the contact).
   *
   * @param siblingMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> for the sibling of the view.
   */
  public static Matcher<View> hasSibling(final Matcher<View> siblingMatcher) {
    return new HasSiblingMatcher(checkNotNull(siblingMatcher));
  }

  /**
   * Returns a <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   * <code>Matcher</code></a> that matches {@link View Views} based on content description property
   * value.
   *
   * @param resourceId the resource id of the content description to match on.
   */
  public static Matcher<View> withContentDescription(final int resourceId) {
    return new WithContentDescriptionFromIdMatcher(resourceId);
  }

  /**
   * Returns an <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   * <code>Matcher</code></a> that matches {@link View Views} based on content description's text
   * value.
   *
   * @param text the text to match on.
   */
  public static Matcher<View> withContentDescription(String text) {
    return new WithContentDescriptionTextMatcher(is(text));
  }

  /**
   * Returns an <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   * <code>Matcher</code></a> that matches {@link View Views} based on content description property
   * value.
   *
   * @param charSequenceMatcher a {@link CharSequence} <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> for the content description
   */
  public static Matcher<View> withContentDescription(
      final Matcher<? extends CharSequence> charSequenceMatcher) {
    return new WithContentDescriptionMatcher(checkNotNull(charSequenceMatcher));
  }

  /**
   * A matcher that matches {@link View}s based on its resource id.
   *
   * <p>Same as {@code withId(is(int))} but attempts to look up resource name of the given id and
   * use a {@code R.id.myView} style description with describeTo. If resource lookup is unavailable,
   * at the time describeTo is invoked, this will print out a simple "with id: %d". If the lookup
   * for a given id fails, "with id: %d (resource name not found)" will be returned as the
   * description.
   *
   * <p><b>Note:</b> Android resource ids are not guaranteed to be unique. You may have to pair this
   * matcher with another one to guarantee a unique view selection.
   *
   * @param id the resource id.
   */
  public static Matcher<View> withId(final int id) {
    return withId(is(id));
  }

  /**
   * Returns a matcher that matches {@link View}s based on resource ids. Note: Android resource ids
   * are not guaranteed to be unique. You may have to pair this matcher with another one to
   * guarantee a unique view selection.
   *
   * @param integerMatcher a Matcher for resource ids
   */
  public static Matcher<View> withId(final Matcher<Integer> integerMatcher) {
    return new WithIdMatcher(checkNotNull(integerMatcher));
  }

  /**
   * Returns a matcher that matches {@link View}s based on resource id names, (for instance,
   * channel_avatar).
   *
   * @param name the resource id name
   */
  public static Matcher<View> withResourceName(String name) {
    return withResourceName(is(name));
  }

  /**
   * Returns a matcher that matches {@link View}s based on resource id names, (for instance,
   * channel_avatar).
   *
   * @param stringMatcher a Matcher for resource id names
   */
  public static Matcher<View> withResourceName(final Matcher<String> stringMatcher) {
    return new WithResourceNameMatcher(checkNotNull(stringMatcher));
  }

  /**
   * Returns a matcher that matches {@link View} based on tag keys.
   *
   * @param key to match
   */
  public static Matcher<View> withTagKey(final int key) {
    return withTagKey(key, Matchers.notNullValue());
  }

  /**
   * Returns a matcher that matches {@link View}s based on tag keys.
   *
   * @param key to match
   * @param objectMatcher Object to match
   */
  public static Matcher<View> withTagKey(final int key, final Matcher<?> objectMatcher) {
    return new WithTagKeyMatcher(key, checkNotNull(objectMatcher));
  }

  /**
   * Returns a matcher that matches {@link View Views} based on tag property values.
   *
   * @param tagValueMatcher a Matcher for the view's tag property value
   */
  public static Matcher<View> withTagValue(final Matcher<Object> tagValueMatcher) {
    return new WithTagValueMatcher(checkNotNull(tagValueMatcher));
  }

  /**
   * Returns a matcher that matches {@link TextView} based on its text property value.
   *
   * <p><b>Note:</b> View's sugar for {@code withText(is("string"))}.
   *
   * @param text {@link String} with the text to match
   */
  public static Matcher<View> withText(String text) {
    return withText(is(text));
  }

  /**
   * Returns a matcher that matches {@link TextView}s based on text property value.
   *
   * <p><b>Note:</b> A View text property is never {@code null}. If you call {@link
   * TextView#setText(CharSequence)} with a {@code null} value it will still be "" (empty string).
   * Do not use a null matcher.
   *
   * @param stringMatcher <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String} with text to match
   */
  public static Matcher<View> withText(final Matcher<String> stringMatcher) {
    return new WithTextMatcher(checkNotNull(stringMatcher));
  }

  /**
   * Returns a matcher that matches a descendant of {@link TextView} that is displaying the string
   * associated with the given resource id.
   *
   * @param resourceId the string resource the text view is expected to hold.
   */
  public static Matcher<View> withText(final int resourceId) {
    return new WithCharSequenceMatcher(resourceId, WithCharSequenceMatcher.TextViewMethod.GET_TEXT);
  }

  /**
   * Returns a matcher that matches {@link TextView} that contains the specific substring.
   *
   * <p><b>Note:</b> View's sugar for {@code withText(containsString("string"))}.
   *
   * @param substring {@link String} that is expected to be contained
   */
  public static Matcher<View> withSubstring(String substring) {
    return withText(containsString(substring));
  }

  /**
   * Returns a matcher that matches {@link TextView} based on it's hint property value.
   *
   * <p><b>Note:</b> View's sugar for {@code withHint(is("string"))}.
   *
   * @param hintText {@link String} with the hint text to match
   */
  public static Matcher<View> withHint(String hintText) {
    return withHint(is(checkNotNull(hintText)));
  }

  /**
   * Returns a matcher that matches {@link TextView}s based on hint property value.
   *
   * <p><b>Note:</b> View's hint property can be <code>null</code>, to match against it use <code>
   * withHint(nullValue(String.class)</code>
   *
   * @param stringMatcher <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String} with text to match
   */
  public static Matcher<View> withHint(final Matcher<String> stringMatcher) {
    return new WithHintMatcher(checkNotNull(stringMatcher));
  }

  /**
   * Returns a matcher that matches a descendant of {@link TextView} that is displaying the hint
   * associated with the given resource id.
   *
   * @param resourceId the string resource the text view is expected to have as a hint.
   */
  public static Matcher<View> withHint(final int resourceId) {
    return new WithCharSequenceMatcher(resourceId, WithCharSequenceMatcher.TextViewMethod.GET_HINT);
  }

  /**
   * Returns a matcher that accepts if and only if the view is a CompoundButton (or subtype of) and
   * is in checked state.
   */
  public static Matcher<View> isChecked() {
    return withCheckBoxState(is(true));
  }

  /**
   * Returns a matcher that accepts if and only if the view is a CompoundButton (or subtype of) and
   * is not in checked state.
   */
  public static Matcher<View> isNotChecked() {
    return withCheckBoxState(is(false));
  }

  private static <E extends View & Checkable> Matcher<View> withCheckBoxState(
      final Matcher<Boolean> checkStateMatcher) {
    return new WithCheckBoxStateMatcher<E>(checkStateMatcher);
  }

  /**
   * Returns an <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   * <code>Matcher</code></a> that matches {@link View Views} with any content description.
   */
  public static Matcher<View> hasContentDescription() {
    return new HasContentDescriptionMatcher();
  }

  /**
   * Returns a matcher that matches {@link View Views} based on the presence of a descendant in its
   * view hierarchy.
   *
   * @param descendantMatcher the type of the descendant to match on
   */
  public static Matcher<View> hasDescendant(final Matcher<View> descendantMatcher) {
    return new HasDescendantMatcher(checkNotNull(descendantMatcher));
  }

  /** Returns a matcher that matches {@link View Views} that are clickable. */
  public static Matcher<View> isClickable() {
    return new IsClickableMatcher(true);
  }

  /** Returns a matcher that matches {@link View Views} that are not clickable. */
  public static Matcher<View> isNotClickable() {
    return new IsClickableMatcher(false);
  }

  /**
   * Returns a matcher that matches {@link View Views} based on the given ancestor type.
   *
   * @param ancestorMatcher the type of the ancestor to match on
   */
  public static Matcher<View> isDescendantOfA(final Matcher<View> ancestorMatcher) {
    return new IsDescendantOfAMatcher(checkNotNull(ancestorMatcher));
  }

  /**
   * Returns a matcher that matches {@link View Views} that have "effective" visibility set to the
   * given value.
   *
   * <p>Effective visibility takes into account not only the view's visibility value, but also that
   * of its ancestors. In case of {@link View.VISIBLE}, this means that the view and all of its
   * ancestors have {@code visibility=VISIBLE}. In case of GONE and INVISIBLE, it's the opposite -
   * any GONE or INVISIBLE parent will make all of its children have their effective visibility.
   *
   * <p><b>Note:</b> Contrary to what the name may imply, view visibility does not directly
   * translate into whether the view is displayed on screen (use {@link #isDisplayed()} for that).
   * For example, the view and all of its ancestors can have {@code visibility=VISIBLE}, but the
   * view may need to be scrolled to in order to be actually visible to the user. Unless you're
   * specifically targeting the visibility value with your test, use {@link #isDisplayed()}.
   */
  public static Matcher<View> withEffectiveVisibility(final Visibility visibility) {
    return new WithEffectiveVisibilityMatcher(visibility);
  }

  /** Enumerates the possible list of values for {@link View#getVisibility()}. */
  public enum Visibility {
    VISIBLE(View.VISIBLE),
    INVISIBLE(View.INVISIBLE),
    GONE(View.GONE);

    private final int value;

    private Visibility(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  /** Matches {@link View Views} with the specified alpha value. */
  public static Matcher<View> withAlpha(final float alpha) {
    return new WithAlphaMatcher(alpha);
  }

  /**
   * A matcher that accepts a view if and only if the view's parent is accepted by the provided
   * matcher.
   *
   * @param parentMatcher the matcher to apply on getParent.
   */
  public static Matcher<View> withParent(final Matcher<View> parentMatcher) {
    return new WithParentMatcher(checkNotNull(parentMatcher));
  }

  /**
   * Returns {@code true} only if the view's child is accepted by the provided matcher.
   *
   * @param childMatcher the matcher to apply on the child views.
   */
  public static Matcher<View> withChild(final Matcher<View> childMatcher) {
    return new WithChildMatcher(checkNotNull(childMatcher));
  }

  /** Matches a {@link ViewGroup} if it has exactly the specified number of children. */
  public static Matcher<View> hasChildCount(final int childCount) {
    return new HasChildCountMatcher(childCount);
  }

  /** Matches a {@link ViewGroup} if it has at least the specified number of children. */
  public static Matcher<View> hasMinimumChildCount(final int minChildCount) {
    return new HasMinimumChildCountMatcher(minChildCount);
  }

  /** Returns a matcher that matches root {@link View}. */
  public static Matcher<View> isRoot() {
    return new IsRootMatcher();
  }

  /** Returns a matcher that matches views that support input methods. */
  public static Matcher<View> supportsInputMethods() {
    return new SupportsInputMethodsMatcher();
  }

  /**
   * Returns a matcher that matches views that support input methods (e.g. EditText) and have the
   * specified IME action set in its {@link EditorInfo}.
   *
   * @param imeAction the IME action to match
   */
  public static Matcher<View> hasImeAction(int imeAction) {
    return hasImeAction(is(imeAction));
  }

  /**
   * Returns a matcher that matches views that support input methods (e.g. EditText) and have the
   * specified IME action set in its {@link EditorInfo}.
   *
   * @param imeActionMatcher a matcher for the IME action
   */
  public static Matcher<View> hasImeAction(final Matcher<Integer> imeActionMatcher) {
    return new HasImeActionMatcher(imeActionMatcher);
  }

  /** Returns a matcher that matches {@link TextView TextViews} that have links. */
  public static Matcher<View> hasLinks() {
    return new HasLinksMatcher();
  }

  /**
   * A replacement for MatcherAssert.assertThat that renders View objects nicely.
   *
   * @param actual the actual value.
   * @param matcher a matcher that accepts or rejects actual.
   */
  public static <T> void assertThat(T actual, Matcher<T> matcher) {
    assertThat("", actual, matcher);
  }

  /**
   * A replacement for MatcherAssert.assertThat that renders View objects nicely.
   *
   * @param message the message to display.
   * @param actual the actual value.
   * @param matcher a matcher that accepts or rejects actual.
   */
  public static <T> void assertThat(String message, T actual, Matcher<T> matcher) {
    if (!matcher.matches(actual)) {
      Description description = new StringDescription();
      String mismatchDescriptionString = getMismatchDescriptionString(actual, matcher);
      description
          .appendText(message)
          .appendText("\nExpected: ")
          .appendDescriptionOf(matcher)
          .appendText("\n     Got: ")
          .appendText(mismatchDescriptionString);
      if (actual instanceof View) {
        // Get the human readable view description.
        description
            .appendText("\nView Details: ")
            .appendText(HumanReadables.describe((View) actual));
      }
      description.appendText("\n");
      throw new AssertionFailedError(description.toString());
    }
  }

  private static <T> String getMismatchDescriptionString(T actual, Matcher<T> matcher) {
    Description mismatchDescription = new StringDescription();
    matcher.describeMismatch(actual, mismatchDescription);
    String mismatchDescriptionString = mismatchDescription.toString().trim();
    return mismatchDescriptionString.isEmpty() ? actual.toString() : mismatchDescriptionString;
  }

  /**
   * Returns a matcher that matches a descendant of {@link Spinner} that is displaying the string of
   * the selected item associated with the given resource id.
   *
   * @param resourceId the resource id of the string resource the text view is expected to hold.
   */
  public static Matcher<View> withSpinnerText(final int resourceId) {
    return new WithSpinnerTextIdMatcher(resourceId);
  }

  /**
   * Returns a matcher that matches {@link Spinner Spinner's} based on {@code toString} value of the
   * selected item.
   *
   * @param stringMatcher <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String} with text to match.
   */
  public static Matcher<View> withSpinnerText(final Matcher<String> stringMatcher) {
    return new WithSpinnerTextMatcher(checkNotNull(stringMatcher));
  }

  /**
   * Returns a matcher that matches {@link Spinner} based on it's selected item's {@code toString}
   * value.
   *
   * <p>Note: Sugar for {@code withSpinnerText(is("string"))}.
   */
  public static Matcher<View> withSpinnerText(String text) {
    return withSpinnerText(is(text));
  }

  /** Returns a matcher that matches {@link WebView} if they are evaluating {@code JavaScript}. */
  public static Matcher<View> isJavascriptEnabled() {
    return new IsJavascriptEnabledMatcher();
  }

  /**
   * Returns a matcher that matches {@link EditText} based on edit text error string value.
   *
   * <p><b>Note:</b> View's error property can be <code>null</code>, to match against it use <code>
   * hasErrorText(nullValue(String.class)</code>
   */
  public static Matcher<View> hasErrorText(final Matcher<String> stringMatcher) {
    return new HasErrorTextMatcher(checkNotNull(stringMatcher));
  }

  /**
   * Returns a matcher that matches {@link EditText} based on edit text error string value.
   *
   * <p>Note: Sugar for {@code hasErrorText(is("string"))}.
   */
  public static Matcher<View> hasErrorText(final String expectedError) {
    return hasErrorText(is(expectedError));
  }

  /** Returns a matcher that matches {@link android.text.InputType}. */
  public static Matcher<View> withInputType(final int inputType) {
    return new WithInputTypeMatcher(inputType);
  }

  /** Returns a matcher that matches the child index inside the {@link ViewParent}. */
  public static Matcher<View> withParentIndex(final int index) {
    checkArgument(index >= 0, "Index %s must be >= 0", index);
    return new WithParentIndexMatcher(index);
  }

  static final class WithIdMatcher extends TypeSafeMatcher<View> {

    @VisibleForTesting
    @RemoteMsgField(order = 0)
    Matcher<Integer> viewIdMatcher;

    private Resources resources;

    @RemoteMsgConstructor
    private WithIdMatcher(Matcher<Integer> integerMatcher) {
      this.viewIdMatcher = integerMatcher;
    }

    @SuppressWarnings("JdkObsolete") // java.util.regex.Matcher requires the use of StringBuffer
    @Override
    public void describeTo(Description description) {
      String idDescription = viewIdMatcher.toString();
      java.util.regex.Matcher matcher = RESOURCE_ID_PATTERN.matcher(idDescription);
      StringBuffer buffer = new StringBuffer(idDescription.length());
      while (matcher.find()) {
        if (resources != null) {
          String idString = matcher.group();
          int id = Integer.parseInt(idString);
          try {
            matcher.appendReplacement(buffer, resources.getResourceName(id));
          } catch (Resources.NotFoundException e) {
            // No big deal, will just use the int value.
            matcher.appendReplacement(
                buffer, String.format(Locale.ROOT, "%s (resource name not found)", idString));
          }
        }
      }
      matcher.appendTail(buffer);
      description.appendText("with id ").appendText(buffer.toString());
    }

    @Override
    public boolean matchesSafely(View view) {
      resources = view.getResources();
      return viewIdMatcher.matches(view.getId());
    }
  }

  static final class WithTextMatcher extends BoundedDiagnosingMatcher<View, TextView> {

    @RemoteMsgField(order = 0)
    private final Matcher<String> stringMatcher;

    @RemoteMsgConstructor
    private WithTextMatcher(Matcher<String> stringMatcher) {
      super(TextView.class);
      this.stringMatcher = stringMatcher;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("view.getText() with or without transformation to match: ");
      stringMatcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(TextView textView, Description mismatchDescription) {
      String text = textView.getText().toString();
      // The reason we try to match both original text and the transformed one is because some UI
      // elements may inherit a default theme which behave differently for SDK below 19 and above.
      // So it is implemented in a backwards compatible way.
      if (stringMatcher.matches(text)) {
        return true;
      }
      mismatchDescription.appendText("view.getText() was ").appendValue(text);
      if (textView.getTransformationMethod() != null) {
        CharSequence transformedText =
            textView.getTransformationMethod().getTransformation(text, textView);
        mismatchDescription.appendText(" transformed text was ").appendValue(transformedText);
        if (transformedText != null) {
          return stringMatcher.matches(transformedText.toString());
        }
      }
      return false;
    }
  }

  static final class WithResourceNameMatcher extends TypeSafeMatcher<View> {

    @RemoteMsgField(order = 0)
    private final Matcher<String> stringMatcher;

    @RemoteMsgConstructor
    private WithResourceNameMatcher(Matcher<String> stringMatcher) {
      this.stringMatcher = stringMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with res-name that ");
      stringMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      if (view.getId() == -1 || view.getResources() == null || isViewIdGenerated(view.getId())) {
        return false;
      }
      try {
        return stringMatcher.matches(view.getResources().getResourceEntryName(view.getId()));
      } catch (Resources.NotFoundException ignore) {
        return false;
      }
    }
  }

  static final class WithTagKeyMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final int key;

    @RemoteMsgField(order = 1)
    private final Matcher<?> objectMatcher;

    @RemoteMsgConstructor
    private WithTagKeyMatcher(int key, Matcher<?> objectMatcher) {
      this.key = key;
      this.objectMatcher = objectMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with key: " + key);
      objectMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return objectMatcher.matches(view.getTag(key));
    }
  }

  static final class IsAssignableFromMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Class<?> clazz;

    @RemoteMsgConstructor
    private IsAssignableFromMatcher(@NonNull Class<?> clazz) {
      this.clazz = checkNotNull(clazz);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is assignable from class: " + clazz);
    }

    @Override
    public boolean matchesSafely(View view) {
      return clazz.isAssignableFrom(view.getClass());
    }
  }

  static final class WithClassNameMatcher extends TypeSafeMatcher<View> {
    @VisibleForTesting
    @RemoteMsgField(order = 0)
    final Matcher<String> classNameMatcher;

    @RemoteMsgConstructor
    private WithClassNameMatcher(Matcher<String> classNameMatcher) {
      this.classNameMatcher = classNameMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with class name: ");
      classNameMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return classNameMatcher.matches(view.getClass().getName());
    }
  }

  static final class IsDisplayedMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgConstructor
    private IsDisplayedMatcher() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is displayed on the screen to the user");
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.getGlobalVisibleRect(new Rect())
          && withEffectiveVisibility(Visibility.VISIBLE).matches(view);
    }
  }

  static final class IsDisplayingAtLeastMatcher extends TypeSafeMatcher<View> {
    @VisibleForTesting
    @RemoteMsgField(order = 0)
    final int areaPercentage;

    @RemoteMsgConstructor
    private IsDisplayingAtLeastMatcher(int areaPercentage) {
      this.areaPercentage = areaPercentage;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              Locale.ROOT,
              "at least %s percent of the view's area is displayed to the user.",
              areaPercentage));
    }

    @Override
    public boolean matchesSafely(View view) {
      Rect visibleParts = new Rect();
      boolean visibleAtAll = view.getGlobalVisibleRect(visibleParts);
      if (!visibleAtAll) {
        return false;
      }

      Rect screen = getScreenWithoutStatusBarActionBar(view);

      float viewHeight = (view.getHeight() > screen.height()) ? screen.height() : view.getHeight();
      float viewWidth = (view.getWidth() > screen.width()) ? screen.width() : view.getWidth();

      if (Build.VERSION.SDK_INT >= 11) {
        // For API level 11 and above, factor in the View's scaleX and scaleY properties.
        viewHeight = Math.min(view.getHeight() * Math.abs(view.getScaleY()), screen.height());
        viewWidth = Math.min(view.getWidth() * Math.abs(view.getScaleX()), screen.width());
      }

      double maxArea = viewHeight * viewWidth;
      double visibleArea = visibleParts.height() * visibleParts.width();
      int displayedPercentage = (int) ((visibleArea / maxArea) * 100);

      return displayedPercentage >= areaPercentage
          && withEffectiveVisibility(Visibility.VISIBLE).matches(view);
    }

    private Rect getScreenWithoutStatusBarActionBar(View view) {
      DisplayMetrics m = new DisplayMetrics();
      ((WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE))
          .getDefaultDisplay()
          .getMetrics(m);

      // Get status bar height
      int resourceId =
          view.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
      int statusBarHeight =
          (resourceId > 0) ? view.getContext().getResources().getDimensionPixelSize(resourceId) : 0;

      // Get action bar height
      TypedValue tv = new TypedValue();
      int actionBarHeight =
          view.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)
              ? TypedValue.complexToDimensionPixelSize(
                  tv.data, view.getContext().getResources().getDisplayMetrics())
              : 0;

      return new Rect(0, 0, m.widthPixels, m.heightPixels - (statusBarHeight + actionBarHeight));
    }
  }

  static final class IsEnabledMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean isEnabled;

    @RemoteMsgConstructor
    private IsEnabledMatcher(boolean isEnabled) {
      this.isEnabled = isEnabled;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is enabled ").appendValue(isEnabled);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.isEnabled() == isEnabled;
    }
  }

  static final class IsFocusableMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean isFocusable;

    @RemoteMsgConstructor
    private IsFocusableMatcher(boolean isFocusable) {
      this.isFocusable = isFocusable;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is focusable ").appendValue(isFocusable);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.isFocusable() == isFocusable;
    }
  }

  static final class IsFocusedMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean isFocused;

    @RemoteMsgConstructor
    private IsFocusedMatcher(boolean isFocused) {
      this.isFocused = isFocused;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is focused ").appendValue(isFocused);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.isFocused() == isFocused;
    }
  }

  static final class HasFocusMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean hasFocus;

    @RemoteMsgConstructor
    private HasFocusMatcher(boolean hasFocus) {
      this.hasFocus = hasFocus;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has focus on the screen to the user ").appendValue(hasFocus);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.hasFocus() == hasFocus;
    }
  }

  static final class IsSelectedMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean isSelected;

    @RemoteMsgConstructor
    private IsSelectedMatcher(boolean isSelected) {
      this.isSelected = isSelected;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is selected ").appendValue(isSelected);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.isSelected() == isSelected;
    }
  }

  static final class HasSiblingMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> siblingMatcher;

    @RemoteMsgConstructor
    private HasSiblingMatcher(final Matcher<View> siblingMatcher) {
      this.siblingMatcher = siblingMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has sibling: ");
      siblingMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      ViewParent parent = view.getParent();
      if (!(parent instanceof ViewGroup)) {
        return false;
      }
      ViewGroup parentGroup = (ViewGroup) parent;
      for (int i = 0; i < parentGroup.getChildCount(); i++) {
        View child = parentGroup.getChildAt(i);
        if (view != child && siblingMatcher.matches(child)) {
          return true;
        }
      }
      return false;
    }
  }

  static final class WithContentDescriptionFromIdMatcher extends TypeSafeMatcher<View> {

    @RemoteMsgField(order = 0)
    private final int resourceId;

    private String resourceName = null;
    private String expectedText = null;

    @RemoteMsgConstructor
    private WithContentDescriptionFromIdMatcher(final int resourceId) {
      this.resourceId = resourceId;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with content description from resource id: ");
      description.appendValue(resourceId);
      if (null != this.resourceName) {
        description.appendText("[");
        description.appendText(resourceName);
        description.appendText("]");
      }
      if (null != this.expectedText) {
        description.appendText(" value: ");
        description.appendText(expectedText);
      }
    }

    @Override
    public boolean matchesSafely(View view) {
      if (null == this.expectedText) {
        try {
          expectedText = view.getResources().getString(resourceId);
          resourceName = view.getResources().getResourceEntryName(resourceId);
        } catch (Resources.NotFoundException ignored) {
          // view could be from a context unaware of the resource id.
        }
      }
      if (null != expectedText && null != view.getContentDescription()) {
        return expectedText.equals(view.getContentDescription().toString());
      } else {
        return false;
      }
    }
  }

  @VisibleForTesting
  static final class WithContentDescriptionMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<? extends CharSequence> charSequenceMatcher;

    @RemoteMsgConstructor
    private WithContentDescriptionMatcher(Matcher<? extends CharSequence> charSequenceMatcher) {
      this.charSequenceMatcher = charSequenceMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with content description: ");
      charSequenceMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return charSequenceMatcher.matches(view.getContentDescription());
    }
  }

  @VisibleForTesting
  static final class WithContentDescriptionTextMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<String> textMatcher;

    @RemoteMsgConstructor
    private WithContentDescriptionTextMatcher(Matcher<String> textMatcher) {
      this.textMatcher = textMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with content description text: ");
      textMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      String descriptionText =
          (view.getContentDescription() != null) ? view.getContentDescription().toString() : null;
      return textMatcher.matches(descriptionText);
    }
  }

  @VisibleForTesting
  static final class WithTagValueMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<Object> tagValueMatcher;

    @RemoteMsgConstructor
    private WithTagValueMatcher(Matcher<Object> tagValueMatcher) {
      this.tagValueMatcher = tagValueMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with tag value: ");
      tagValueMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return tagValueMatcher.matches(view.getTag());
    }
  }

  @VisibleForTesting
  static final class WithCharSequenceMatcher extends BoundedDiagnosingMatcher<View, TextView> {
    @RemoteMsgField(order = 0)
    private final int resourceId;

    @RemoteMsgField(order = 1)
    private final TextViewMethod method;

    @Nullable private String resourceName;
    @Nullable private String expectedText;

    private enum TextViewMethod {
      GET_TEXT,
      GET_HINT
    }

    @RemoteMsgConstructor
    private WithCharSequenceMatcher(int resourceId, TextViewMethod method) {
      super(TextView.class);
      this.resourceId = resourceId;
      this.method = method;
    }

    @Override
    protected void describeMoreTo(Description description) {
      switch (method) {
        case GET_TEXT:
          description.appendText("view.getText()");
          break;
        case GET_HINT:
          description.appendText("view.getHint()");
          break;
      }
      description.appendText(" equals string from resource id: ").appendValue(resourceId);
      if (null != resourceName) {
        description.appendText(" [").appendText(resourceName).appendText("]");
      }
      if (null != expectedText) {
        description.appendText(" value: ").appendText(expectedText);
      }
    }

    @Override
    protected boolean matchesSafely(TextView textView, Description mismatchDescription) {
      if (null == expectedText) {
        try {
          expectedText = textView.getResources().getString(resourceId);
          resourceName = textView.getResources().getResourceEntryName(resourceId);
        } catch (Resources.NotFoundException ignored) {
          /* view could be from a context unaware of the resource id. */
        }
      }
      CharSequence actualText;
      switch (method) {
        case GET_TEXT:
          actualText = textView.getText();
          mismatchDescription.appendText("view.getText() was ");
          break;
        case GET_HINT:
          actualText = textView.getHint();
          mismatchDescription.appendText("view.getHint() was ");
          break;
        default:
          throw new IllegalStateException("Unexpected TextView method: " + method);
      }
      mismatchDescription.appendValue(actualText);
      // FYI: actualText may not be string ... its just a char sequence convert to string.
      return null != expectedText && null != actualText && expectedText.contentEquals(actualText);
    }
  }

  @VisibleForTesting
  static final class WithHintMatcher extends BoundedDiagnosingMatcher<View, TextView> {
    @RemoteMsgField(order = 0)
    private final Matcher<String> stringMatcher;

    @RemoteMsgConstructor
    private WithHintMatcher(Matcher<String> stringMatcher) {
      super(TextView.class);
      this.stringMatcher = stringMatcher;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("view.getHint() matching: ");
      stringMatcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(TextView textView, Description mismatchDescription) {
      CharSequence hint = textView.getHint();
      mismatchDescription.appendText("view.getHint() was ").appendValue(hint);
      return stringMatcher.matches(hint);
    }
  }

  @VisibleForTesting
  static final class WithCheckBoxStateMatcher<E extends View & Checkable>
      extends BoundedDiagnosingMatcher<View, E> {
    @RemoteMsgField(order = 0)
    private final Matcher<Boolean> checkStateMatcher;

    @RemoteMsgConstructor
    private WithCheckBoxStateMatcher(Matcher<Boolean> checkStateMatcher) {
      super(View.class, Checkable.class);
      this.checkStateMatcher = checkStateMatcher;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("view.isChecked() matching: ").appendDescriptionOf(checkStateMatcher);
    }

    @Override
    protected boolean matchesSafely(E checkable, Description mismatchDescription) {
      boolean isChecked = checkable.isChecked();
      mismatchDescription.appendText("view.isChecked() was ").appendValue(isChecked);
      return checkStateMatcher.matches(isChecked);
    }
  }

  @VisibleForTesting
  static final class HasContentDescriptionMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgConstructor
    private HasContentDescriptionMatcher() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("has content description");
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.getContentDescription() != null;
    }
  }

  @VisibleForTesting
  static final class HasDescendantMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> descendantMatcher;

    @RemoteMsgConstructor
    private HasDescendantMatcher(Matcher<View> descendantMatcher) {
      this.descendantMatcher = descendantMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has descendant: ");
      descendantMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(final View view) {
      final Predicate<View> matcherPredicate =
          input -> input != view && descendantMatcher.matches(input);

      Iterator<View> matchedViewIterator =
          Iterables.filter(breadthFirstViewTraversal(view), matcherPredicate).iterator();

      return matchedViewIterator.hasNext();
    }
  }

  @VisibleForTesting
  static final class IsClickableMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final boolean isClickable;

    @RemoteMsgConstructor
    private IsClickableMatcher(boolean isClickable) {
      this.isClickable = isClickable;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is clickable ").appendValue(isClickable);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.isClickable() == isClickable;
    }
  }

  @VisibleForTesting
  static final class IsDescendantOfAMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> ancestorMatcher;

    @RemoteMsgConstructor
    private IsDescendantOfAMatcher(Matcher<View> ancestorMatcher) {
      this.ancestorMatcher = ancestorMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is descendant of a: ");
      ancestorMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return checkAncestors(view.getParent(), ancestorMatcher);
    }

    private boolean checkAncestors(ViewParent viewParent, Matcher<View> ancestorMatcher) {
      if (!(viewParent instanceof View)) {
        return false;
      }
      if (ancestorMatcher.matches(viewParent)) {
        return true;
      }
      return checkAncestors(viewParent.getParent(), ancestorMatcher);
    }
  }

  @VisibleForTesting
  static final class WithEffectiveVisibilityMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Visibility visibility;

    @RemoteMsgConstructor
    private WithEffectiveVisibilityMatcher(Visibility visibility) {
      this.visibility = visibility;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(Locale.ROOT, "view has effective visibility=%s", visibility));
    }

    @Override
    public boolean matchesSafely(View view) {
      if (visibility.getValue() == View.VISIBLE) {
        if (view.getVisibility() != visibility.getValue()) {
          return false;
        }
        while (view.getParent() instanceof View) {
          view = (View) view.getParent();
          if (view.getVisibility() != visibility.getValue()) {
            return false;
          }
        }
        return true;
      } else {
        if (view.getVisibility() == visibility.getValue()) {
          return true;
        }
        while (view.getParent() instanceof View) {
          view = (View) view.getParent();
          if (view.getVisibility() == visibility.getValue()) {
            return true;
          }
        }
        return false;
      }
    }
  }

  /**
   * Returns a matcher that matches {@link android.view.View} based on background resource.
   *
   * <p>Note: This method compares images at a pixel level and might have significant performance
   * implications for larger bitmaps.
   *
   * <p><b>This API is currently in beta.</b>
   */
  @Beta
  public static Matcher<View> hasBackground(final int drawableId) {
    return new HasBackgroundMatcher(drawableId);
  }

  /**
   * Returns a matcher that matches {@link android.widget.TextView} based on it's color.
   *
   * <p><b>This API is currently in beta.</b>
   */
  @Beta
  public static Matcher<View> hasTextColor(final int colorResId) {
    return new BoundedDiagnosingMatcher<View, TextView>(TextView.class) {
      private Context context;

      @Override
      protected boolean matchesSafely(TextView textView, Description mismatchDescription) {
        context = textView.getContext();
        int textViewColor = textView.getCurrentTextColor();
        int expectedColor;

        if (Build.VERSION.SDK_INT <= 22) {
          expectedColor = context.getResources().getColor(colorResId);
        } else {
          expectedColor = context.getColor(colorResId);
        }

        mismatchDescription
            .appendText("textView.getCurrentTextColor() was ")
            .appendText(getColorHex(textViewColor));
        return textViewColor == expectedColor;
      }

      @Override
      protected void describeMoreTo(Description description) {
        description.appendText("textView.getCurrentTextColor() is color with ");
        if (context == null) {
          description.appendText("ID ").appendValue(colorResId);
        } else {
          int color =
              (Build.VERSION.SDK_INT <= 22)
                  ? context.getResources().getColor(colorResId)
                  : context.getColor(colorResId);
          description.appendText("value " + getColorHex(color));
        }
      }

      private String getColorHex(int color) {
        return String.format(
            Locale.ROOT, "#%02X%06X", (0xFF & Color.alpha(color)), (0xFFFFFF & color));
      }
    };
  }


  @VisibleForTesting
  static final class WithAlphaMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final float alpha;

    @RemoteMsgConstructor
    private WithAlphaMatcher(final float alpha) {
      this.alpha = alpha;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has alpha: ").appendValue(alpha);
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.getAlpha() == alpha;
    }
  }

  @VisibleForTesting
  static final class WithParentMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> parentMatcher;

    @RemoteMsgConstructor
    private WithParentMatcher(Matcher<View> parentMatcher) {
      this.parentMatcher = parentMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has parent matching: ");
      parentMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      return parentMatcher.matches(view.getParent());
    }
  }

  @VisibleForTesting
  static final class WithChildMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> childMatcher;

    @RemoteMsgConstructor
    private WithChildMatcher(Matcher<View> childMatcher) {
      this.childMatcher = childMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has child: ");
      childMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      if (!(view instanceof ViewGroup)) {
        return false;
      }

      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        if (childMatcher.matches(group.getChildAt(i))) {
          return true;
        }
      }

      return false;
    }
  }

  @VisibleForTesting
  static final class HasChildCountMatcher extends BoundedDiagnosingMatcher<View, ViewGroup> {
    @RemoteMsgField(order = 0)
    private final int childCount;

    @RemoteMsgConstructor
    private HasChildCountMatcher(int childCount) {
      super(ViewGroup.class);
      this.childCount = childCount;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("viewGroup.getChildCount() to be ").appendValue(childCount);
    }

    @Override
    protected boolean matchesSafely(ViewGroup viewGroup, Description mismatchDescription) {
      mismatchDescription
          .appendText("viewGroup.getChildCount() was ")
          .appendValue(viewGroup.getChildCount());
      return viewGroup.getChildCount() == childCount;
    }
  }

  @VisibleForTesting
  static final class HasMinimumChildCountMatcher extends BoundedDiagnosingMatcher<View, ViewGroup> {
    @RemoteMsgField(order = 0)
    private final int minChildCount;

    @RemoteMsgConstructor
    private HasMinimumChildCountMatcher(int minChildCount) {
      super(ViewGroup.class);
      this.minChildCount = minChildCount;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description
          .appendText("viewGroup.getChildCount() to be at least ")
          .appendValue(minChildCount);
    }

    @Override
    protected boolean matchesSafely(ViewGroup viewGroup, Description mismatchDescription) {
      mismatchDescription
          .appendText("viewGroup.getChildCount() was ")
          .appendValue(viewGroup.getChildCount());
      return viewGroup.getChildCount() >= minChildCount;
    }
  }

  @VisibleForTesting
  static final class IsRootMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgConstructor
    private IsRootMatcher() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is a root view.");
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.getRootView().equals(view);
    }
  }

  @VisibleForTesting
  static final class SupportsInputMethodsMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgConstructor
    private SupportsInputMethodsMatcher() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("supports input methods");
    }

    @Override
    public boolean matchesSafely(View view) {
      // At first glance, it would make sense to use view.onCheckIsTextEditor, but the android
      // javadoc is wishy-washy about whether authors are required to implement this method when
      // implementing onCreateInputConnection.
      return view.onCreateInputConnection(new EditorInfo()) != null;
    }
  }

  @VisibleForTesting
  static final class HasImeActionMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final Matcher<Integer> imeActionMatcher;

    @RemoteMsgConstructor
    private HasImeActionMatcher(final Matcher<Integer> imeActionMatcher) {
      this.imeActionMatcher = imeActionMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has ime action: ");
      imeActionMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      EditorInfo editorInfo = new EditorInfo();
      InputConnection inputConnection = view.onCreateInputConnection(editorInfo);
      if (inputConnection == null) {
        return false;
      }
      int actionId =
          editorInfo.actionId != 0
              ? editorInfo.actionId
              : editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
      return imeActionMatcher.matches(actionId);
    }
  }

  @VisibleForTesting
  static final class HasLinksMatcher extends BoundedDiagnosingMatcher<View, TextView> {
    @RemoteMsgConstructor
    private HasLinksMatcher() {
      super(TextView.class);
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("textView.getUrls().length > 0");
    }

    @Override
    protected boolean matchesSafely(TextView textView, Description mismatchDescription) {
      mismatchDescription
          .appendText("textView.getUrls().length was ")
          .appendValue(textView.getUrls().length);
      return textView.getUrls().length > 0;
    }
  }

  @VisibleForTesting
  static final class WithSpinnerTextIdMatcher extends BoundedDiagnosingMatcher<View, Spinner> {
    @RemoteMsgField(order = 0)
    private final int resourceId;

    private String resourceName = null;
    private String expectedText = null;

    @RemoteMsgConstructor
    private WithSpinnerTextIdMatcher(int resourceId) {
      super(Spinner.class);
      this.resourceId = resourceId;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description
          .appendText("spinner.getSelectedItem().toString() to match string from resource id: ")
          .appendValue(resourceId);
      if (resourceName != null) {
        description.appendText(" [").appendText(resourceName).appendText("]");
      }
      if (expectedText != null) {
        description.appendText(" value: ").appendText(expectedText);
      }
    }

    @Override
    protected boolean matchesSafely(Spinner spinner, Description mismatchDescription) {
      if (expectedText == null) {
        try {
          expectedText = spinner.getResources().getString(resourceId);
          resourceName = spinner.getResources().getResourceEntryName(resourceId);
        } catch (Resources.NotFoundException ignored) {
          /*
           * view could be from a context unaware of the resource id.
           */
        }
      }
      if (expectedText == null) {
        mismatchDescription.appendText("failure to resolve resourceId ").appendValue(resourceId);
        return false;
      }
      Object selectedItem = spinner.getSelectedItem();
      if (selectedItem == null) {
        mismatchDescription.appendText("spinner.getSelectedItem() was null");
        return false;
      }
      mismatchDescription
          .appendText("spinner.getSelectedItem().toString() was ")
          .appendValue(selectedItem.toString());
      return expectedText.equals(selectedItem.toString());
    }
  }

  @VisibleForTesting
  static final class WithSpinnerTextMatcher extends BoundedDiagnosingMatcher<View, Spinner> {
    @RemoteMsgField(order = 0)
    private final Matcher<String> stringMatcher;

    @RemoteMsgConstructor
    private WithSpinnerTextMatcher(Matcher<String> stringMatcher) {
      super(Spinner.class);
      this.stringMatcher = stringMatcher;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description
          .appendText("spinner.getSelectedItem().toString() to match ")
          .appendDescriptionOf(stringMatcher);
    }

    @Override
    protected boolean matchesSafely(Spinner spinner, Description mismatchDescription) {
      Object selectedItem = spinner.getSelectedItem();
      if (selectedItem == null) {
        mismatchDescription.appendText("spinner.getSelectedItem() was null");
        return false;
      }
      mismatchDescription
          .appendText("spinner.getSelectedItem().toString() was ")
          .appendValue(selectedItem.toString());
      return stringMatcher.matches(spinner.getSelectedItem().toString());
    }
  }

  @VisibleForTesting
  static final class IsJavascriptEnabledMatcher extends BoundedDiagnosingMatcher<View, WebView> {
    @RemoteMsgConstructor
    private IsJavascriptEnabledMatcher() {
      super(WebView.class);
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("webView.getSettings().getJavaScriptEnabled() is ").appendValue(true);
    }

    @Override
    protected boolean matchesSafely(WebView webView, Description mismatchDescription) {
      mismatchDescription
          .appendText("webView.getSettings().getJavaScriptEnabled() was ")
          .appendValue(webView.getSettings().getJavaScriptEnabled());
      return webView.getSettings().getJavaScriptEnabled();
    }
  }

  @VisibleForTesting
  static final class HasErrorTextMatcher extends BoundedDiagnosingMatcher<View, EditText> {
    @RemoteMsgField(order = 0)
    private final Matcher<String> stringMatcher;

    @RemoteMsgConstructor
    private HasErrorTextMatcher(Matcher<String> stringMatcher) {
      super(EditText.class);
      this.stringMatcher = stringMatcher;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("editText.getError() to match ").appendDescriptionOf(stringMatcher);
    }

    @Override
    protected boolean matchesSafely(EditText view, Description mismatchDescription) {
      mismatchDescription.appendText("editText.getError() was ").appendValue(view.getError());
      return stringMatcher.matches(view.getError());
    }
  }

  @VisibleForTesting
  static final class WithInputTypeMatcher extends BoundedDiagnosingMatcher<View, EditText> {
    @RemoteMsgField(order = 0)
    private final int inputType;

    @RemoteMsgConstructor
    private WithInputTypeMatcher(int inputType) {
      super(EditText.class);
      this.inputType = inputType;
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("editText.getInputType() is ").appendValue(inputType);
    }

    @Override
    protected boolean matchesSafely(EditText view, Description mismatchDescription) {
      mismatchDescription
          .appendText("editText.getInputType() was ")
          .appendValue(view.getInputType());
      return view.getInputType() == inputType;
    }
  }

  @VisibleForTesting
  static final class WithParentIndexMatcher extends TypeSafeMatcher<View> {
    @RemoteMsgField(order = 0)
    private final int index;

    @RemoteMsgConstructor
    private WithParentIndexMatcher(int index) {
      this.index = index;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with parent index: " + index);
    }

    @Override
    public boolean matchesSafely(View view) {
      ViewParent parent = view.getParent();
      return parent instanceof ViewGroup
          && ((ViewGroup) parent).getChildCount() > index
          && ((ViewGroup) parent).getChildAt(index) == view;
    }
  }

  /**
   * IDs generated by {@link View#generateViewId} will fail if used as a resource ID in attempted
   * resources lookups. This now logs an error in API 28, causing test failures. This method is
   * taken from {@link View#isViewIdGenerated} to prevent resource lookup to check if a view id was
   * generated.
   */
  private static boolean isViewIdGenerated(int id) {
    return (id & 0xFF000000) == 0 && (id & 0x00FFFFFF) != 0;
  }
}
