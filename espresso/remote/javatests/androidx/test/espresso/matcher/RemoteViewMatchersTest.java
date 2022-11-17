/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.matcher;

import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.hasImeAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasLinks;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isFocusable;
import static androidx.test.espresso.matcher.ViewMatchers.isJavascriptEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static androidx.test.espresso.matcher.ViewMatchers.withAlpha;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withInputType;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withTagKey;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.espresso.matcher.ViewMatchers.HasChildCountMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasContentDescriptionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasDescendantMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasErrorTextMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasFocusMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasImeActionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasLinksMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasMinimumChildCountMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasSiblingMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsClickableMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDescendantOfAMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDisplayedMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDisplayingAtLeastMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsEnabledMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsFocusableMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsJavascriptEnabledMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsRootMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsSelectedMatcher;
import androidx.test.espresso.matcher.ViewMatchers.SupportsInputMethodsMatcher;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.espresso.matcher.ViewMatchers.WithAlphaMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithCharSequenceMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithCheckBoxStateMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithChildMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithClassNameMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithContentDescriptionFromIdMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithContentDescriptionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithEffectiveVisibilityMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithHintMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithInputTypeMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithParentIndexMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithParentMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithSpinnerTextIdMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithSpinnerTextMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithTagValueMatcher;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasChildCountMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasContentDescriptionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasDescendantMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasErrorTextMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasFocusMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasImeActionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasLinksMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasMinimumChildCountMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasSiblingMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsAssignableFromMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsClickableMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDescendantOfAMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDisplayedMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDisplayingAtLeastMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsEnabledMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsFocusableMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsJavascriptEnabledMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsRootMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsSelectedMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.SupportsInputMethodsMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithAlphaMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithCharSequenceMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithCheckBoxStateMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithChildMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithClassNameMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithContentDescriptionFromIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithContentDescriptionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithEffectiveVisibilityMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithHintMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithInputTypeMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithParentIndexMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithParentMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithResourceNameMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithSpinnerTextIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithSpinnerTextMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTagKeyMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTagValueMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTextMatcherProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.ui.app.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all matchers under {@link ViewMatchers} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteViewMatchersTest {
  private static final String TEXT_VIEW_TEXT = "Cortado";

  @Before
  public void registerMatcherWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteViewMatchers.init(remoteDescriptorRegistry);
    RemoteHamcrestCoreMatchers13.init(remoteDescriptorRegistry);
  }

  @Test
  public void withId_transformationToProto() {
    WithIdMatcherProto withIdMatcherProto =
        (WithIdMatcherProto) new GenericRemoteMessage(withId(R.id.testId1)).toProto();
    assertThat(withIdMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withId_transformationFromProto() {
    Matcher<View> withIdMatcher = withId(R.id.testId1);

    WithIdMatcherProto withIdMatcherProto =
        (WithIdMatcherProto) new GenericRemoteMessage(withIdMatcher).toProto();
    Matcher<View> withIdMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withIdMatcherProto);

    assertDescriptionsEqual(withIdMatcher, withIdMatcherFromProto);
  }

  @Test
  public void withText_transformationToProto() {
    TextView textView = new TextView(getInstrumentation().getContext());
    textView.setText(TEXT_VIEW_TEXT);
    Matcher<View> withTextMatcher = withText(is(TEXT_VIEW_TEXT));
    assertThat(textView, withTextMatcher);

    WithTextMatcherProto withTextMatcherProto =
        (WithTextMatcherProto) new GenericRemoteMessage(withTextMatcher).toProto();
    assertThat(withTextMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withText_transformationFromProto() {
    TextView textView = new TextView(getInstrumentation().getContext());
    textView.setText(TEXT_VIEW_TEXT);
    Matcher<View> withTextMatcher = withText(is(TEXT_VIEW_TEXT));
    assertThat(textView, withTextMatcher);

    WithTextMatcherProto withTextMatcherProto =
        (WithTextMatcherProto) new GenericRemoteMessage(withTextMatcher).toProto();
    Matcher<View> withTextMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withTextMatcherProto);
    assertThat(textView, withTextMatcherFromProto);
  }

  @Test
  public void withResourceName_transformationToProto() {
    WithResourceNameMatcherProto withResourceNameMatcherProto =
        (WithResourceNameMatcherProto)
            new GenericRemoteMessage(withResourceName("testId1")).toProto();
    assertThat(withResourceNameMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withResourceName_transformationFromProto() {
    Matcher<View> withResourceName = withResourceName("testId1");

    WithResourceNameMatcherProto withResourceNameMatcherProto =
        (WithResourceNameMatcherProto) new GenericRemoteMessage(withResourceName).toProto();
    Matcher<View> withResourceNameFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withResourceNameMatcherProto);
    assertDescriptionsEqual(withResourceName, withResourceNameFromProto);
  }

  @Test
  public void isAssignable_transformationToProto() {
    TextView textView = new TextView(getInstrumentation().getContext());
    textView.setText(TEXT_VIEW_TEXT);

    Matcher<View> isAssignableFromMatcher = isAssignableFrom(TextView.class);
    IsAssignableFromMatcherProto isAssignableFromMatcherProto =
        (IsAssignableFromMatcherProto) new GenericRemoteMessage(isAssignableFromMatcher).toProto();
    assertThat(isAssignableFromMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isAssignable_transformationFromProto() {
    TextView textView = new TextView(getInstrumentation().getContext());
    textView.setText(TEXT_VIEW_TEXT);

    Matcher<View> isAssignableFromMatcher = isAssignableFrom(TextView.class);
    IsAssignableFromMatcherProto isAssignableFromMatcherProto =
        (IsAssignableFromMatcherProto) new GenericRemoteMessage(isAssignableFromMatcher).toProto();

    Matcher<View> isAssignableFromMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isAssignableFromMatcherProto);
    assertThat(textView, isAssignableFromMatcherFromProto);
  }

  @Test
  public void withTagKey_transformationToProto() {
    WithTagKeyMatcherProto withTagKeyMatcherProto =
        (WithTagKeyMatcherProto) new GenericRemoteMessage(withTagKey(123)).toProto();
    assertThat(withTagKeyMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withTagKey_transformationFromProto() {
    Matcher<View> withTagKeyMatcher = withTagKey(R.id.testId1);

    WithTagKeyMatcherProto withTagKeyMatcherProto =
        (WithTagKeyMatcherProto) new GenericRemoteMessage(withTagKeyMatcher).toProto();
    Matcher<View> withTagKeyMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withTagKeyMatcherProto);

    View view = new View(getInstrumentation().getContext());
    assertThat(view, not(withTagKeyMatcherFromProto));
    view.setTag(R.id.testId1, "blah");
    assertThat(view, withTagKeyMatcherFromProto);
  }

  @Test
  public void withClassName_transformationToProto() {
    Matcher<View> withClassNameMatcher = withClassName(is("com.foo.ClassName"));
    WithClassNameMatcherProto withClassNameMatcherProto =
        (WithClassNameMatcherProto) new GenericRemoteMessage(withClassNameMatcher).toProto();
    assertThat(withClassNameMatcherProto, notNullValue());
  }

  @Test
  public void withClassName_transformationFromProto() {
    String expectedName = "com.foo.ClassName";
    Matcher<View> withClassNameMatcher = withClassName(is(expectedName));

    WithClassNameMatcherProto withClassNameMatcherProto =
        (WithClassNameMatcherProto) new GenericRemoteMessage(withClassNameMatcher).toProto();
    WithClassNameMatcher withClassNameMatcherFromProto =
        (WithClassNameMatcher) GenericRemoteMessage.FROM.fromProto(withClassNameMatcherProto);

    assertThat(expectedName, withClassNameMatcherFromProto.classNameMatcher);
  }

  @Test
  public void isDisplayed_transformationToProto() {
    Matcher<View> isDisplayedMatcher = isDisplayed();
    IsDisplayedMatcherProto isDisplayedMatcherProto =
        (IsDisplayedMatcherProto) new GenericRemoteMessage(isDisplayedMatcher).toProto();
    assertThat(isDisplayedMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isDisplayed_transformationFromProto() {
    Matcher<View> isDisplayedMatcher = isDisplayed();

    IsDisplayedMatcherProto isDisplayedMatcherProto =
        (IsDisplayedMatcherProto) new GenericRemoteMessage(isDisplayedMatcher).toProto();
    Matcher<View> isDisplayedMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isDisplayedMatcherProto);

    assertThat(isDisplayedMatcherFromProto, notNullValue());
    assertThat(isDisplayedMatcherFromProto, instanceOf(IsDisplayedMatcher.class));
  }

  @Test
  public void isDisplayingAtLeast_transformationToProto() {
    Matcher<View> isDisplayingAtLeastMatcher = isDisplayingAtLeast(25);
    IsDisplayingAtLeastMatcherProto isDisplayingAtLeastMatcherProto =
        (IsDisplayingAtLeastMatcherProto)
            new GenericRemoteMessage(isDisplayingAtLeastMatcher).toProto();
    assertThat(isDisplayingAtLeastMatcherProto, notNullValue());
  }

  @Test
  public void isDisplayingAtLeast_transformationFromProto() {
    int expectedValue = 25;
    Matcher<View> isDisplayingAtLeastMatcher = isDisplayingAtLeast(expectedValue);

    IsDisplayingAtLeastMatcherProto isDisplayedMatcherProto =
        (IsDisplayingAtLeastMatcherProto)
            new GenericRemoteMessage(isDisplayingAtLeastMatcher).toProto();
    IsDisplayingAtLeastMatcher isDisplayingAtLeastMatcherFromProto =
        (IsDisplayingAtLeastMatcher) GenericRemoteMessage.FROM.fromProto(isDisplayedMatcherProto);

    assertThat(isDisplayingAtLeastMatcherFromProto, notNullValue());
    assertThat(expectedValue, is(isDisplayingAtLeastMatcherFromProto.areaPercentage));
  }

  @Test
  public void isEnabled_transformationToProto() {
    Matcher<View> isEnabledMatcher = isEnabled();
    IsEnabledMatcherProto isEnabledMatcherProto =
        (IsEnabledMatcherProto) new GenericRemoteMessage(isEnabledMatcher).toProto();
    assertThat(isEnabledMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isEnabled_transformationFromProto() {
    Matcher<View> isEnabledMatcher = isEnabled();

    IsEnabledMatcherProto isEnabledMatcherProto =
        (IsEnabledMatcherProto) new GenericRemoteMessage(isEnabledMatcher).toProto();
    Matcher<View> isEnabledMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isEnabledMatcherProto);

    assertThat(isEnabledMatcherFromProto, notNullValue());
    assertThat(isEnabledMatcherFromProto, instanceOf(IsEnabledMatcher.class));
  }

  @Test
  public void isFocusable_transformationToProto() {
    Matcher<View> isFocusableMatcher = isFocusable();
    IsFocusableMatcherProto isFocusableMatcherProto =
        (IsFocusableMatcherProto) new GenericRemoteMessage(isFocusableMatcher).toProto();
    assertThat(isFocusableMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isFocusable_transformationFromProto() {
    Matcher<View> isFocusableMatcher = isFocusable();

    IsFocusableMatcherProto isFocusableMatcherProto =
        (IsFocusableMatcherProto) new GenericRemoteMessage(isFocusableMatcher).toProto();
    Matcher<View> isFocusableMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isFocusableMatcherProto);

    assertThat(isFocusableMatcherFromProto, notNullValue());
    assertThat(isFocusableMatcherFromProto, instanceOf(IsFocusableMatcher.class));
  }

  @Test
  public void hasFocus_transformationToProto() {
    Matcher<View> hasFocusMatcher = hasFocus();
    HasFocusMatcherProto hasFocusMatcherProto =
        (HasFocusMatcherProto) new GenericRemoteMessage(hasFocusMatcher).toProto();
    assertThat(hasFocusMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasFocus_transformationFromProto() {
    Matcher<View> hasFocusMatcher = hasFocus();

    HasFocusMatcherProto hasFocusMatcherProto =
        (HasFocusMatcherProto) new GenericRemoteMessage(hasFocusMatcher).toProto();
    Matcher<View> hasFocusMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasFocusMatcherProto);

    assertThat(hasFocusMatcherFromProto, notNullValue());
    assertThat(hasFocusMatcherFromProto, instanceOf(HasFocusMatcher.class));
  }

  @Test
  public void isSelected_transformationToProto() {
    Matcher<View> isSelectedMatcher = isSelected();
    IsSelectedMatcherProto isSelectedMatcherProto =
        (IsSelectedMatcherProto) new GenericRemoteMessage(isSelectedMatcher).toProto();
    assertThat(isSelectedMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isSelected_transformationFromProto() {
    Matcher<View> isSelectedMatcher = isSelected();

    IsSelectedMatcherProto isSelectedMatcherProto =
        (IsSelectedMatcherProto) new GenericRemoteMessage(isSelectedMatcher).toProto();
    Matcher<View> isSelectedMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isSelectedMatcherProto);

    assertThat(isSelectedMatcherFromProto, notNullValue());
    assertThat(isSelectedMatcherFromProto, instanceOf(IsSelectedMatcher.class));
  }

  @Test
  public void hasSibling_transformationToProto() {
    Matcher<View> hasSiblingMatcher = hasSibling(withText("sibling"));
    HasSiblingMatcherProto hasSiblingMatcherProto =
        (HasSiblingMatcherProto) new GenericRemoteMessage(hasSiblingMatcher).toProto();
    assertThat(hasSiblingMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasSibling_transformationFromProto() {
    Matcher<View> expected = withText("sibling");
    Matcher<View> hasSiblingMatcher = hasSibling(expected);

    HasSiblingMatcherProto hasSiblingMatcherProto =
        (HasSiblingMatcherProto) new GenericRemoteMessage(hasSiblingMatcher).toProto();
    Matcher<View> hasSiblingMatcherFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasSiblingMatcherProto);

    assertThat(hasSiblingMatcherFromProto, notNullValue());
    assertThat(hasSiblingMatcherFromProto, instanceOf(HasSiblingMatcher.class));
  }

  @Test
  public void withContentDescriptionFromId_transformationToProto() {
    Matcher<View> withContentDescriptionMatcher = withContentDescription(123);
    WithContentDescriptionFromIdMatcherProto hasSiblingMatcherProto =
        (WithContentDescriptionFromIdMatcherProto)
            new GenericRemoteMessage(withContentDescriptionMatcher).toProto();
    assertThat(hasSiblingMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withContentDescriptionFromId_transformationFromProto() {
    Matcher<View> withContentDescriptionMatcher = withContentDescription(123);

    WithContentDescriptionFromIdMatcherProto withContentDescriptionProto =
        (WithContentDescriptionFromIdMatcherProto)
            new GenericRemoteMessage(withContentDescriptionMatcher).toProto();
    Matcher<View> withContentDescriptionFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withContentDescriptionProto);

    assertThat(withContentDescriptionFromProto, notNullValue());
    assertThat(
        withContentDescriptionFromProto, instanceOf(WithContentDescriptionFromIdMatcher.class));
  }

  @Test
  public void withContentDescription_transformationToProto() {
    Matcher<View> withContentDescriptionMatcher = withContentDescription(is("foo"));
    WithContentDescriptionMatcherProto hasSiblingMatcherProto =
        (WithContentDescriptionMatcherProto)
            new GenericRemoteMessage(withContentDescriptionMatcher).toProto();
    assertThat(hasSiblingMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withContentDescription_transformationFromProto() {
    Matcher<View> withContentDescriptionMatcher = withContentDescription(is("foo"));

    WithContentDescriptionMatcherProto withContentDescriptionProto =
        (WithContentDescriptionMatcherProto)
            new GenericRemoteMessage(withContentDescriptionMatcher).toProto();
    Matcher<View> withContentDescriptionFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withContentDescriptionProto);

    assertThat(withContentDescriptionFromProto, notNullValue());
    assertThat(withContentDescriptionFromProto, instanceOf(WithContentDescriptionMatcher.class));
  }

  @Test
  public void withTagValue_transformationToProto() {
    Matcher<View> withTagValueMatcher = withTagValue(is("foo"));
    WithTagValueMatcherProto withTagValueProto =
        (WithTagValueMatcherProto) new GenericRemoteMessage(withTagValueMatcher).toProto();
    assertThat(withTagValueProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withTagValue_transformationFromProto() {
    Matcher<View> withTagValueMatcher = withTagValue(is("foo"));

    WithTagValueMatcherProto withTagValueProto =
        (WithTagValueMatcherProto) new GenericRemoteMessage(withTagValueMatcher).toProto();
    Matcher<View> withTagValueFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withTagValueProto);

    assertThat(withTagValueFromProto, notNullValue());
    assertThat(withTagValueFromProto, instanceOf(WithTagValueMatcher.class));
  }

  @Test
  public void withCharSeq_withText_transformationToProto() {
    Matcher<View> withCharSeqMatcher = withText(123);
    WithCharSequenceMatcherProto withCharSeqProto =
        (WithCharSequenceMatcherProto) new GenericRemoteMessage(withCharSeqMatcher).toProto();
    assertThat(withCharSeqProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withCharSeq_withText_transformationFromProto() {
    Matcher<View> withCharSeqMatcher = withText(123);

    WithCharSequenceMatcherProto withCharSeqProto =
        (WithCharSequenceMatcherProto) new GenericRemoteMessage(withCharSeqMatcher).toProto();
    Matcher<View> withCharSeqFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withCharSeqProto);

    assertThat(withCharSeqFromProto, notNullValue());
    assertThat(withCharSeqFromProto, instanceOf(WithCharSequenceMatcher.class));
  }

  @Test
  public void withCharSeq_withHint_transformationToProto() {
    Matcher<View> withCharSeqMatcher = withHint(123);
    WithCharSequenceMatcherProto withCharSeqProto =
        (WithCharSequenceMatcherProto) new GenericRemoteMessage(withCharSeqMatcher).toProto();
    assertThat(withCharSeqProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withCharSeq_withHint_transformationFromProto() {
    Matcher<View> withCharSeqMatcher = withHint(123);

    WithCharSequenceMatcherProto withCharSeqProto =
        (WithCharSequenceMatcherProto) new GenericRemoteMessage(withCharSeqMatcher).toProto();
    Matcher<View> withCharSeqFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withCharSeqProto);

    assertThat(withCharSeqFromProto, notNullValue());
    assertThat(withCharSeqFromProto, instanceOf(WithCharSequenceMatcher.class));
  }

  @Test
  public void withHint_transformationToProto() {
    Matcher<View> withHintMatcher = withHint(is("foo"));
    WithHintMatcherProto withHintProto =
        (WithHintMatcherProto) new GenericRemoteMessage(withHintMatcher).toProto();
    assertThat(withHintProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withHint_transformationFromProto() {
    Matcher<View> withHintMatcher = withHint(is("foo"));

    WithHintMatcherProto withHintProto =
        (WithHintMatcherProto) new GenericRemoteMessage(withHintMatcher).toProto();
    Matcher<View> withHintFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withHintProto);

    assertThat(withHintFromProto, notNullValue());
    assertThat(withHintFromProto, instanceOf(WithHintMatcher.class));
  }

  @Test
  public void isChecked_transformationToProto() {
    Matcher<View> withCheckBoxStateMatcher = isChecked();
    WithCheckBoxStateMatcherProto withCheckBoxStateMatcherProto =
        (WithCheckBoxStateMatcherProto)
            new GenericRemoteMessage(withCheckBoxStateMatcher).toProto();
    assertThat(withCheckBoxStateMatcherProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isChecked_transformationFromProto() {
    Matcher<View> withCheckBoxStateMatcher = isChecked();

    WithCheckBoxStateMatcherProto withCheckBoxStateMatcherProto =
        (WithCheckBoxStateMatcherProto)
            new GenericRemoteMessage(withCheckBoxStateMatcher).toProto();
    Matcher<View> withCheckBoxStateMatcherProtoFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withCheckBoxStateMatcherProto);

    assertThat(withCheckBoxStateMatcherProtoFromProto, notNullValue());
    assertThat(withCheckBoxStateMatcherProtoFromProto, instanceOf(WithCheckBoxStateMatcher.class));
  }

  @Test
  public void hasContentDescription_transformationToProto() {
    Matcher<View> hasContentDescriptionMatcher = hasContentDescription();
    HasContentDescriptionMatcherProto hasContentDescriptionProto =
        (HasContentDescriptionMatcherProto)
            new GenericRemoteMessage(hasContentDescriptionMatcher).toProto();
    assertThat(hasContentDescriptionProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasContentDescription_transformationFromProto() {
    Matcher<View> hasContentDescriptionMatcher = hasContentDescription();

    HasContentDescriptionMatcherProto hasContentDescriptionProto =
        (HasContentDescriptionMatcherProto)
            new GenericRemoteMessage(hasContentDescriptionMatcher).toProto();
    Matcher<View> hasContentDescriptionProtoFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasContentDescriptionProto);

    assertThat(hasContentDescriptionProtoFromProto, notNullValue());
    assertThat(hasContentDescriptionProtoFromProto, instanceOf(HasContentDescriptionMatcher.class));
  }

  @Test
  public void hasDescendant_transformationToProto() {
    Matcher<View> hasDescendantMatcher = hasDescendant(isAssignableFrom(TextView.class));
    HasDescendantMatcherProto hasDescendantProto =
        (HasDescendantMatcherProto) new GenericRemoteMessage(hasDescendantMatcher).toProto();
    assertThat(hasDescendantProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasDescendant_transformationFromProto() {
    Matcher<View> hasDescendantMatcher = hasDescendant(isAssignableFrom(TextView.class));

    HasDescendantMatcherProto hasDescendantProto =
        (HasDescendantMatcherProto) new GenericRemoteMessage(hasDescendantMatcher).toProto();
    Matcher<View> hasDescendantFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasDescendantProto);

    assertThat(hasDescendantFromProto, notNullValue());
    assertThat(hasDescendantFromProto, instanceOf(HasDescendantMatcher.class));
  }

  @Test
  public void isClickable_transformationToProto() {
    Matcher<View> isClickableMatcher = isClickable();
    IsClickableMatcherProto isClickableProto =
        (IsClickableMatcherProto) new GenericRemoteMessage(isClickableMatcher).toProto();
    assertThat(isClickableProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isClickable_transformationFromProto() {
    Matcher<View> isClickableMatcher = isClickable();

    IsClickableMatcherProto isClickableProto =
        (IsClickableMatcherProto) new GenericRemoteMessage(isClickableMatcher).toProto();
    Matcher<View> isClickableFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isClickableProto);

    assertThat(isClickableFromProto, notNullValue());
    assertThat(isClickableFromProto, instanceOf(IsClickableMatcher.class));
  }

  @Test
  public void isDescendantOfA_transformationToProto() {
    Matcher<View> isDescendantOfAMatcher = isDescendantOfA(isAssignableFrom(LinearLayout.class));
    IsDescendantOfAMatcherProto isDescendantOfAProto =
        (IsDescendantOfAMatcherProto) new GenericRemoteMessage(isDescendantOfAMatcher).toProto();
    assertThat(isDescendantOfAProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isDescendantOfA_transformationFromProto() {
    Matcher<View> isDescendantOfAMatcher = isDescendantOfA(isAssignableFrom(LinearLayout.class));

    IsDescendantOfAMatcherProto isDescendantOfAProto =
        (IsDescendantOfAMatcherProto) new GenericRemoteMessage(isDescendantOfAMatcher).toProto();
    Matcher<View> isDescendantOfAFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isDescendantOfAProto);

    assertThat(isDescendantOfAFromProto, notNullValue());
    assertThat(isDescendantOfAFromProto, instanceOf(IsDescendantOfAMatcher.class));
  }

  @Test
  public void withEffectiveVisibility_transformationToProto() {
    Matcher<View> withEffectiveVisibilityMatcher = withEffectiveVisibility(Visibility.VISIBLE);
    WithEffectiveVisibilityMatcherProto withEffectiveVisibilityProto =
        (WithEffectiveVisibilityMatcherProto)
            new GenericRemoteMessage(withEffectiveVisibilityMatcher).toProto();
    assertThat(withEffectiveVisibilityProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withEffectiveVisibility_transformationFromProto() {
    Matcher<View> withEffectiveVisibilityMatcher = withEffectiveVisibility(Visibility.VISIBLE);

    WithEffectiveVisibilityMatcherProto withEffectiveVisibilityProto =
        (WithEffectiveVisibilityMatcherProto)
            new GenericRemoteMessage(withEffectiveVisibilityMatcher).toProto();
    Matcher<View> withEffectiveVisibilityFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withEffectiveVisibilityProto);

    assertThat(withEffectiveVisibilityFromProto, notNullValue());
    assertThat(withEffectiveVisibilityFromProto, instanceOf(WithEffectiveVisibilityMatcher.class));
  }

  @Test
  public void withAlpha_transformationToProto() {
    Matcher<View> withAlphaMatcher = withAlpha(0.1f);
    WithAlphaMatcherProto withAlphaProto =
        (WithAlphaMatcherProto) new GenericRemoteMessage(withAlphaMatcher).toProto();
    assertThat(withAlphaProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withAlpha_transformationFromProto() {
    Matcher<View> withAlphaMatcher = withAlpha(0.1f);

    WithAlphaMatcherProto withAlphaProto =
        (WithAlphaMatcherProto) new GenericRemoteMessage(withAlphaMatcher).toProto();
    Matcher<View> withAlphaFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withAlphaProto);

    assertThat(withAlphaFromProto, notNullValue());
    assertThat(withAlphaFromProto, instanceOf(WithAlphaMatcher.class));
  }

  @Test
  public void withParent_transformationToProto() {
    Matcher<View> withParentMatcher = withParent(isAssignableFrom(LinearLayout.class));
    WithParentMatcherProto withParentProto =
        (WithParentMatcherProto) new GenericRemoteMessage(withParentMatcher).toProto();
    assertThat(withParentProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withParent_transformationFromProto() {
    // TODO(b/33789949) change this to use TV directly
    Matcher<View> withParentMatcher = withParent(isAssignableFrom(LinearLayout.class));

    WithParentMatcherProto withParentProto =
        (WithParentMatcherProto) new GenericRemoteMessage(withParentMatcher).toProto();
    Matcher<View> withParentFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withParentProto);

    assertThat(withParentFromProto, notNullValue());
    assertThat(withParentFromProto, instanceOf(WithParentMatcher.class));
  }

  @Test
  public void withChild_transformationToProto() {
    Matcher<View> withChildMatcher = withChild(isAssignableFrom(LinearLayout.class));
    WithChildMatcherProto withChildProto =
        (WithChildMatcherProto) new GenericRemoteMessage(withChildMatcher).toProto();
    assertThat(withChildProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withChild_transformationFromProto() {
    // TODO(b/33789949) change this to use TV directly
    Matcher<View> withChildMatcher = withChild(isAssignableFrom(LinearLayout.class));

    WithChildMatcherProto withChildProto =
        (WithChildMatcherProto) new GenericRemoteMessage(withChildMatcher).toProto();
    Matcher<View> withChildFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withChildProto);

    assertThat(withChildFromProto, notNullValue());
    assertThat(withChildFromProto, instanceOf(WithChildMatcher.class));
  }

  @Test
  public void hasChildCount_transformationToProto() {
    Matcher<View> hasChildCountMatcher = hasChildCount(0);
    HasChildCountMatcherProto hasChildCountProto =
        (HasChildCountMatcherProto) new GenericRemoteMessage(hasChildCountMatcher).toProto();
    assertThat(hasChildCountProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasChildCount_transformationFromProto() {
    Matcher<View> hasChildCountMatcher = hasChildCount(0);

    HasChildCountMatcherProto hasChildCountProto =
        (HasChildCountMatcherProto) new GenericRemoteMessage(hasChildCountMatcher).toProto();
    Matcher<View> hasChildCountFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasChildCountProto);

    assertThat(hasChildCountFromProto, notNullValue());
    assertThat(hasChildCountFromProto, instanceOf(HasChildCountMatcher.class));
  }

  @Test
  public void hasMinimumChildCount_transformationToProto() {
    Matcher<View> hasMinimumChildCountMatcher = hasMinimumChildCount(0);
    HasMinimumChildCountMatcherProto hasMinimumChildCountProto =
        (HasMinimumChildCountMatcherProto)
            new GenericRemoteMessage(hasMinimumChildCountMatcher).toProto();
    assertThat(hasMinimumChildCountProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasMinimumChildCount_transformationFromProto() {
    Matcher<View> hasMinimumChildCountMatcher = hasMinimumChildCount(0);

    HasMinimumChildCountMatcherProto hasMinimumChildCountProto =
        (HasMinimumChildCountMatcherProto)
            new GenericRemoteMessage(hasMinimumChildCountMatcher).toProto();
    Matcher<View> hasMinimumChildCountFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasMinimumChildCountProto);

    assertThat(hasMinimumChildCountFromProto, notNullValue());
    assertThat(hasMinimumChildCountFromProto, instanceOf(HasMinimumChildCountMatcher.class));
  }

  @Test
  public void isRoot_transformationToProto() {
    Matcher<View> isRootMatcher = isRoot();
    IsRootMatcherProto isRootProto =
        (IsRootMatcherProto) new GenericRemoteMessage(isRootMatcher).toProto();
    assertThat(isRootProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isRoot_transformationFromProto() {
    Matcher<View> isRootMatcher = isRoot();

    IsRootMatcherProto isRootProto =
        (IsRootMatcherProto) new GenericRemoteMessage(isRootMatcher).toProto();
    Matcher<View> isRootFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isRootProto);

    assertThat(isRootFromProto, notNullValue());
    assertThat(isRootFromProto, instanceOf(IsRootMatcher.class));
  }

  @Test
  public void supportsInputMethods_transformationToProto() {
    Matcher<View> supportsInputMethodsMatcher = supportsInputMethods();
    SupportsInputMethodsMatcherProto supportsInputMethodsProto =
        (SupportsInputMethodsMatcherProto)
            new GenericRemoteMessage(supportsInputMethodsMatcher).toProto();
    assertThat(supportsInputMethodsProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void supportsInputMethods_transformationFromProto() {
    Matcher<View> supportsInputMethodsMatcher = supportsInputMethods();

    SupportsInputMethodsMatcherProto supportsInputMethodsProto =
        (SupportsInputMethodsMatcherProto)
            new GenericRemoteMessage(supportsInputMethodsMatcher).toProto();
    Matcher<View> supportsInputMethodsFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(supportsInputMethodsProto);

    assertThat(supportsInputMethodsFromProto, notNullValue());
    assertThat(supportsInputMethodsFromProto, instanceOf(SupportsInputMethodsMatcher.class));
  }

  @Test
  public void hasImeAction_transformationToProto() {
    Matcher<View> hasImeActionMatcher = hasImeAction(EditorInfo.IME_ACTION_GO);
    HasImeActionMatcherProto hasImeActionProto =
        (HasImeActionMatcherProto) new GenericRemoteMessage(hasImeActionMatcher).toProto();
    assertThat(hasImeActionProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasImeAction_transformationFromProto() {
    Matcher<View> hasImeActionMatcher = hasImeAction(EditorInfo.IME_ACTION_GO);

    HasImeActionMatcherProto hasImeActionProto =
        (HasImeActionMatcherProto) new GenericRemoteMessage(hasImeActionMatcher).toProto();
    Matcher<View> hasImeActionFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasImeActionProto);

    assertThat(hasImeActionFromProto, notNullValue());
    assertThat(hasImeActionFromProto, instanceOf(HasImeActionMatcher.class));
  }

  @Test
  public void hasLinks_transformationToProto() {
    Matcher<View> hasLinksMatcher = hasLinks();
    HasLinksMatcherProto hasLinksProto =
        (HasLinksMatcherProto) new GenericRemoteMessage(hasLinksMatcher).toProto();
    assertThat(hasLinksProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasLinks_transformationFromProto() {
    Matcher<View> hasLinksMatcher = hasLinks();

    HasLinksMatcherProto hasLinksProto =
        (HasLinksMatcherProto) new GenericRemoteMessage(hasLinksMatcher).toProto();
    Matcher<View> hasLinksFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasLinksProto);

    assertThat(hasLinksFromProto, notNullValue());
    assertThat(hasLinksFromProto, instanceOf(HasLinksMatcher.class));
  }

  @Test
  public void withSpinnerTextId_transformationToProto() {
    Matcher<View> withSpinnerTextMatcher = withSpinnerText(123);
    WithSpinnerTextIdMatcherProto withSpinnerTextProto =
        (WithSpinnerTextIdMatcherProto) new GenericRemoteMessage(withSpinnerTextMatcher).toProto();
    assertThat(withSpinnerTextProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withSpinnerTextId_transformationFromProto() {
    Matcher<View> withSpinnerTextMatcher = withSpinnerText(123);

    WithSpinnerTextIdMatcherProto withSpinnerTextProto =
        (WithSpinnerTextIdMatcherProto) new GenericRemoteMessage(withSpinnerTextMatcher).toProto();
    Matcher<View> withSpinnerTextFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withSpinnerTextProto);

    assertThat(withSpinnerTextFromProto, notNullValue());
    assertThat(withSpinnerTextFromProto, instanceOf(WithSpinnerTextIdMatcher.class));
  }

  @Test
  public void withSpinnerText_transformationToProto() {
    Matcher<View> withSpinnerTextMatcher = withSpinnerText(is("string"));
    WithSpinnerTextMatcherProto withSpinnerTextProto =
        (WithSpinnerTextMatcherProto) new GenericRemoteMessage(withSpinnerTextMatcher).toProto();
    assertThat(withSpinnerTextProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withSpinnerText_transformationFromProto() {
    Matcher<View> withSpinnerTextMatcher = withSpinnerText(is("string"));

    WithSpinnerTextMatcherProto withSpinnerTextProto =
        (WithSpinnerTextMatcherProto) new GenericRemoteMessage(withSpinnerTextMatcher).toProto();
    Matcher<View> withSpinnerTextFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withSpinnerTextProto);

    assertThat(withSpinnerTextFromProto, notNullValue());
    assertThat(withSpinnerTextFromProto, instanceOf(WithSpinnerTextMatcher.class));
  }

  @Test
  public void isJavascriptEnabled_transformationToProto() {
    Matcher<View> isJavascriptEnabledMatcher = isJavascriptEnabled();
    IsJavascriptEnabledMatcherProto isJavascriptEnabledProto =
        (IsJavascriptEnabledMatcherProto)
            new GenericRemoteMessage(isJavascriptEnabledMatcher).toProto();
    assertThat(isJavascriptEnabledProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void isJavascriptEnabled_transformationFromProto() {
    Matcher<View> isJavascriptEnabledMatcher = isJavascriptEnabled();

    IsJavascriptEnabledMatcherProto isJavascriptEnabledProto =
        (IsJavascriptEnabledMatcherProto)
            new GenericRemoteMessage(isJavascriptEnabledMatcher).toProto();
    Matcher<View> isJavascriptEnabledFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(isJavascriptEnabledProto);

    assertThat(isJavascriptEnabledFromProto, notNullValue());
    assertThat(isJavascriptEnabledFromProto, instanceOf(IsJavascriptEnabledMatcher.class));
  }

  @Test
  public void hasErrorText_transformationToProto() {
    Matcher<View> hasErrorTextMatcher = hasErrorText("test");
    HasErrorTextMatcherProto hasErrorTextProto =
        (HasErrorTextMatcherProto) new GenericRemoteMessage(hasErrorTextMatcher).toProto();
    assertThat(hasErrorTextProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void hasErrorText_transformationFromProto() {
    Matcher<View> hasErrorTextMatcher = hasErrorText("test");

    HasErrorTextMatcherProto hasErrorTextProto =
        (HasErrorTextMatcherProto) new GenericRemoteMessage(hasErrorTextMatcher).toProto();
    Matcher<View> hasErrorTextFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(hasErrorTextProto);

    assertThat(hasErrorTextFromProto, notNullValue());
    assertThat(hasErrorTextFromProto, instanceOf(HasErrorTextMatcher.class));
  }

  @Test
  public void withInputType_transformationToProto() {
    Matcher<View> withInputTypeMatcher = withInputType(123);
    WithInputTypeMatcherProto withInputTypeProto =
        (WithInputTypeMatcherProto) new GenericRemoteMessage(withInputTypeMatcher).toProto();
    assertThat(withInputTypeProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withInputType_transformationFromProto() {
    Matcher<View> withInputTypeMatcher = withInputType(123);

    WithInputTypeMatcherProto withInputTypeProto =
        (WithInputTypeMatcherProto) new GenericRemoteMessage(withInputTypeMatcher).toProto();
    Matcher<View> withInputTypeFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withInputTypeProto);

    assertThat(withInputTypeFromProto, notNullValue());
    assertThat(withInputTypeFromProto, instanceOf(WithInputTypeMatcher.class));
  }

  @Test
  public void withParentIndex_transformationToProto() {
    Matcher<View> withParentIndexMatcher = withParentIndex(123);
    WithParentIndexMatcherProto withParentIndexProto =
        (WithParentIndexMatcherProto) new GenericRemoteMessage(withParentIndexMatcher).toProto();
    assertThat(withParentIndexProto, notNullValue());
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void withParentIndex_transformationFromProto() {
    Matcher<View> withParentIndexMatcher = withParentIndex(123);

    WithParentIndexMatcherProto withParentIndexProto =
        (WithParentIndexMatcherProto) new GenericRemoteMessage(withParentIndexMatcher).toProto();
    Matcher<View> withParentIndexFromProto =
        (Matcher<View>) GenericRemoteMessage.FROM.fromProto(withParentIndexProto);

    assertThat(withParentIndexFromProto, notNullValue());
    assertThat(withParentIndexFromProto, instanceOf(WithParentIndexMatcher.class));
  }

  // The only way to compare matchers (since they could be nested is by using their describeTo
  // method and relying on that being descriptive enough). Since non-descriptive describeTo
  // implementations are also considered bugs, this is a reasonable solution. It would be akin to
  // a custom implementation of equals(Object other) which is incomplete.
  private static void assertDescriptionsEqual(Matcher<?> expected, Matcher<?> actual) {
    Description expectedDescription = new StringDescription();
    Description actualDescription = new StringDescription();
    expected.describeTo(expectedDescription);
    actual.describeTo(actualDescription);
    assertEquals(
        "Check that both matchers provide the same description",
        expectedDescription.toString(),
        actualDescription.toString());
  }
}
