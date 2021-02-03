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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.matcher.MatcherTestUtils.getDescription;
import static androidx.test.espresso.matcher.MatcherTestUtils.getMismatchDescription;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagKey;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.SpannedString;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import junit.framework.AssertionFailedError;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

/** Unit tests for {@link ViewMatchers}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewMatchers1Test {

  private static final int UNRECOGNIZED_INPUT_TYPE = 999999;

  private Context context;

  @Before
  public void setUp() throws Exception {
    context = getApplicationContext();
  }

  @Test
  public void isAssignableFrom_notAnInstance() {
    View v = new View(context);
    assertFalse(isAssignableFrom(Spinner.class).matches(v));
  }

  @Test
  public void isAssignableFrom_plainView() {
    View v = new View(context);
    assertTrue(isAssignableFrom(View.class).matches(v));
  }

  @Test
  public void isAssignableFrom_superclass() {
    View v = new RadioButton(context);
    assertTrue(isAssignableFrom(Button.class).matches(v));
  }

  @Test
  public void isAssignableFrom_description() {
    assertThat(
        getDescription(isAssignableFrom(Button.class)),
        is("is assignable from class <" + Button.class + ">"));
  }

  @Test
  public void isAssignableFrom_mismatchDescription() {
    View view = new View(context);
    assertThat(
        getMismatchDescription(isAssignableFrom(Spinner.class), view),
        is("view.getClass() was <" + View.class + ">"));
  }

  @Test
  public void withClassNameTest() {
    TextView textView = new TextView(context);
    assertTrue(withClassName(is(TextView.class.getName())).matches(textView));
    assertTrue(withClassName(endsWith("TextView")).matches(textView));
    assertFalse(withClassName(startsWith("test")).matches(textView));
  }

  @Test
  public void withClassName_description() {
    Matcher<String> matcher = is(TextView.class.getName());
    assertThat(
        getDescription(withClassName(matcher)),
        is("view.getClass().getName() matches: " + getDescription(matcher)));
  }

  @Test
  public void withClassName_mismatchDescription() {
    assertThat(
        getMismatchDescription(withClassName(is(TextView.class.getName())), new Button(context)),
        is("view.getClass().getName() was \"" + Button.class.getName() + "\""));
  }

  @Test
  public void withContentDescriptionCharSequence() {
    View view = new View(context);
    view.setContentDescription(null);
    assertTrue(withContentDescription(nullValue(CharSequence.class)).matches(view));
    CharSequence testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(withContentDescription(is(testText)).matches(view));
    assertFalse(withContentDescription(is((CharSequence) "blah")).matches(view));
    assertFalse(withContentDescription(is((CharSequence) "")).matches(view));
  }

  @Test
  public void withContentDescriptionNull() {
    assertThrows(
        NullPointerException.class, () -> withContentDescription((Matcher<CharSequence>) null));
  }

  @Test
  public void hasContentDescriptionTest() {
    View view = new View(context);
    view.setContentDescription(null);
    assertFalse(hasContentDescription().matches(view));
    CharSequence testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(hasContentDescription().matches(view));
  }

  @Test
  public void hasContentDescription_description() {
    assertThat(
        getDescription(hasContentDescription()), is("view.getContentDescription() is not null"));
  }

  @Test
  public void hasContentDescription_mismatchDescription() {
    assertThat(
        getMismatchDescription(hasContentDescription(), new View(context)),
        is("view.getContentDescription() was null"));
  }

  @Test
  public void withContentDescriptionString() {
    View view = new View(context);
    view.setContentDescription(null);
    assertTrue(withContentDescription(nullValue(String.class)).matches(view));

    String testText = "test text!";
    view.setContentDescription(testText);
    assertTrue(withContentDescription(is(testText)).matches(view));
    assertFalse(withContentDescription(is("blah")).matches(view));
    assertFalse(withContentDescription(is("")).matches(view));

    // Test withContentDescription(String) directly.
    assertTrue(withContentDescription(testText).matches(view));

    view.setContentDescription(null);
    String nullString = null;
    assertTrue(withContentDescription(nullString).matches(view));
    assertFalse(withContentDescription(testText).matches(view));

    // Test when the view's content description is not a String type.
    view.setContentDescription(new SpannedString(testText));
    assertTrue(withContentDescription(testText).matches(view));
    assertFalse(withContentDescription("different text").matches(view));
  }

  @Test
  public void withContentDescriptionCharSequence_description() {
    Matcher<CharSequence> matcher = is("charsequence");
    assertThat(
        getDescription(withContentDescription(matcher)),
        is("view.getContentDescription() " + getDescription(matcher)));
  }

  @Test
  public void withContentDescriptionCharSequence_mismatchDescription() {
    View view = new View(context);
    view.setContentDescription("blah");
    Matcher<CharSequence> matcher = is("charsequence");
    assertThat(
        getMismatchDescription(withContentDescription(matcher), view),
        is("view.getContentDescription() " + getMismatchDescription(matcher, "blah")));
  }

  @Test
  public void withContentDescriptionString_description() {
    Matcher<String> matcher = is("string");
    assertThat(
        getDescription(withContentDescription(matcher)),
        is("view.getContentDescription() " + getDescription(matcher)));
  }

  @Test
  public void withContentDescriptionString_mismatchDescription() {
    View view = new View(context);
    view.setContentDescription("blah");
    Matcher<String> matcher = is("string");
    assertThat(
        getMismatchDescription(withContentDescription(matcher), view),
        is("view.getContentDescription() " + getMismatchDescription(matcher, "blah")));
  }

  @Test
  public void withContentDescriptionFromResourceId() {
    View view = new View(context);
    view.setContentDescription(context.getString(R.string.something));
    assertFalse(withContentDescription(R.string.other_string).matches(view));
    assertTrue(withContentDescription(R.string.something).matches(view));
  }

  @Test
  public void withContentDescriptionFromResourceId_description_noResourceName() {
    assertThat(
        getDescription(withContentDescription(R.string.other_string)),
        is("view.getContentDescription() to match resource id <" + R.string.other_string + ">"));
  }

  @Test
  public void withContentDescriptionFromResourceId_description_withResourceName() {
    Matcher<View> matcher = withContentDescription(R.string.other_string);
    matcher.matches(new View(context));
    assertThat(
        getDescription(matcher),
        is(
            "view.getContentDescription() to match resource id <"
                + R.string.other_string
                + ">[other_string] with value \"Goodbye!!\""));
  }

  @Test
  public void withContentDescriptionFromResourceId_mismatchDescription() {
    View view = new View(context);
    view.setContentDescription("test");
    assertThat(
        getMismatchDescription(withContentDescription(R.string.other_string), view),
        is("view.getContentDescription() was \"test\""));
  }

  @Test
  public void withIdTest() {
    View view = new View(context);
    view.setId(R.id.testId1);
    assertTrue(withId(is(R.id.testId1)).matches(view));
    assertFalse(withId(is(R.id.testId2)).matches(view));
    assertFalse(withId(is(1234)).matches(view));
  }

  @Test
  public void withId_describeWithNoResourceLookup() {
    assertThat(getDescription(withId(5)), is("view.getId() is <5>"));
  }

  @Test
  public void withId_describeWithFailedResourceLookup() {
    View view = new View(context);
    Matcher<View> matcher = withId(5);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(getDescription(matcher), is("view.getId() is <5 (resource name not found)>"));
  }

  @Test
  public void withId_describeWithResourceLookup() {
    View view = new View(context);
    Matcher<View> matcher = withId(R.id.testId1);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(
        getDescription(matcher),
        is("view.getId() is <" + R.id.testId1 + "/" + context.getPackageName() + ":id/testId1>"));
  }

  @Test
  public void withId_describeMismatchWithNoResourceLookup() {
    View view = new View(context);
    view.setId(7);
    assertThat(
        getMismatchDescription(withId(5), view),
        is("view.getId() was <7 (resource name not found)>"));
  }

  @Test
  public void withId_describeMismatchWithFailedResourceLookup() {
    View view = new View(context);
    view.setId(7);
    Matcher<View> matcher = withId(5);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(
        getMismatchDescription(matcher, view),
        is("view.getId() was <7 (resource name not found)>"));
  }

  @Test
  public void withId_describeMismatchWithResourceLookup() {
    View view = new View(context);
    view.setId(R.id.testId1);
    Matcher<View> matcher = withId(R.id.testId2);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(
        getMismatchDescription(matcher, view),
        is("view.getId() was <" + R.id.testId1 + "/" + context.getPackageName() + ":id/testId1>"));
  }

  @Test
  public void withTagNull() {
    assertThrows(NullPointerException.class, () -> withTagKey(0, null));
    assertThrows(NullPointerException.class, () -> withTagValue(null));
  }

  @Test
  public void withTagObject() {
    View view = new View(context);
    view.setTag(null);
    assertTrue(withTagValue(Matchers.nullValue()).matches(view));
    String testObjectText = "test text!";
    view.setTag(testObjectText);
    assertFalse(withTagKey(R.id.testId1).matches(view));
    assertTrue(withTagValue(is((Object) testObjectText)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
    assertFalse(withTagValue(is((Object) "")).matches(view));
  }

  @Test
  public void withTagValue_description() {
    Matcher<Object> matcher = is((Object) "blah");
    assertThat(
        getDescription(withTagValue(matcher)), is("view.getTag() " + getDescription(matcher)));
  }

  @Test
  public void withTagValue_mismatchDescription() {
    View view = new View(context);
    view.setTag("tag");
    Matcher<Object> matcher = is((Object) "blah");
    assertThat(
        getMismatchDescription(withTagValue(matcher), view),
        is("view.getTag() " + getMismatchDescription(matcher, "tag")));
  }

  @Test
  public void withTagKeyTest() {
    View view = new View(context);
    assertFalse(withTagKey(R.id.testId1).matches(view));
    view.setTag(R.id.testId1, "blah");
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
    assertTrue(withTagKey(R.id.testId1).matches(view));
    assertFalse(withTagKey(R.id.testId2).matches(view));
    assertFalse(withTagKey(R.id.testId3).matches(view));
    assertFalse(withTagKey(65535).matches(view));

    view.setTag(R.id.testId2, "blah2");
    assertTrue(withTagKey(R.id.testId1).matches(view));
    assertTrue(withTagKey(R.id.testId2).matches(view));
    assertFalse(withTagKey(R.id.testId3).matches(view));
    assertFalse(withTagKey(65535).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
  }

  @Test
  public void withTagKeyObject() {
    View view = new View(context);
    String testObjectText1 = "test text1!";
    String testObjectText2 = "test text2!";
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    view.setTag(R.id.testId1, testObjectText1);
    assertTrue(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText2)).matches(view));
    assertFalse(withTagKey(R.id.testId2, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId3, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(65535, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));

    view.setTag(R.id.testId2, testObjectText2);
    assertTrue(withTagKey(R.id.testId1, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId1, is((Object) testObjectText2)).matches(view));
    assertTrue(withTagKey(R.id.testId2, is((Object) testObjectText2)).matches(view));
    assertFalse(withTagKey(R.id.testId2, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(R.id.testId3, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagKey(65535, is((Object) testObjectText1)).matches(view));
    assertFalse(withTagValue(is((Object) "blah")).matches(view));
  }

  @Test
  public void withTagKeyObject_description() {
    Matcher<String> matcher = is("test");
    assertThat(
        getDescription(withTagKey(R.id.testId1, matcher)),
        is("view.getTag(" + R.id.testId1 + ") " + getDescription(matcher)));
  }

  @Test
  public void withTagKeyObject_mismatchDescription() {
    View view = new View(context);
    view.setTag(R.id.testId1, "test1");
    Matcher<String> matcher = is("test2");
    assertThat(
        getMismatchDescription(withTagKey(R.id.testId1, matcher), view),
        is("view.getTag(" + R.id.testId1 + ") " + getMismatchDescription(matcher, "test1")));
  }

  @Test
  public void withTextNull() {
    assertThrows(NullPointerException.class, () -> withText((Matcher<String>) null));
  }

  @UiThreadTest
  @Test
  public void checkBoxMatchers() {
    assertFalse(isChecked().matches(new Spinner(context)));
    assertFalse(isNotChecked().matches(new Spinner(context)));

    CheckBox checkBox = new CheckBox(context);
    checkBox.setChecked(true);
    assertTrue(isChecked().matches(checkBox));
    assertFalse(isNotChecked().matches(checkBox));

    checkBox.setChecked(false);
    assertFalse(isChecked().matches(checkBox));
    assertTrue(isNotChecked().matches(checkBox));

    RadioButton radioButton = new RadioButton(context);
    radioButton.setChecked(false);
    assertFalse(isChecked().matches(radioButton));
    assertTrue(isNotChecked().matches(radioButton));

    radioButton.setChecked(true);
    assertTrue(isChecked().matches(radioButton));
    assertFalse(isNotChecked().matches(radioButton));

    CheckedTextView checkedText = new CheckedTextView(context);
    checkedText.setChecked(false);
    assertFalse(isChecked().matches(checkedText));
    assertTrue(isNotChecked().matches(checkedText));

    checkedText.setChecked(true);
    assertTrue(isChecked().matches(checkedText));
    assertFalse(isNotChecked().matches(checkedText));

    Checkable checkable =
        new Checkable() {
          @Override
          public boolean isChecked() {
            return true;
          }

          @Override
          public void setChecked(boolean ignored) {}

          @Override
          public void toggle() {}
        };

    assertFalse(isChecked().matches(checkable));
    assertFalse(isNotChecked().matches(checkable));
  }

  @Test
  public void withTextString() {
    TextView textView = new TextView(context);
    textView.setText(null);
    assertTrue(withText(is("")).matches(textView));
    String testText = "test text!";
    textView.setText(testText);
    assertTrue(withText(is(testText)).matches(textView));
    assertFalse(withText(is("blah")).matches(textView));
    assertFalse(withText(is("")).matches(textView));
  }

  @Test
  public void withTextString_describe() {
    Matcher<String> isMatcher = is("test");
    assertThat(
        getDescription(withText(isMatcher)),
        is(
            "an instance of android.widget.TextView and "
                + "view.getText() with or without transformation to match: "
                + getDescription(isMatcher)));
  }

  @Test
  public void withTextString_describeMismatch() {
    TextView textView = new TextView(context);
    textView.setText("text");
    Matcher<View> viewMatcher = withText(is("blah"));

    assertThat(getMismatchDescription(viewMatcher, textView), is("view.getText() was \"text\""));
  }

  @Test
  public void withTextString_withTransformation_describeMismatch() {
    TextView textView = new TextView(context);
    textView.setText("text");
    textView.setTransformationMethod(
        new TransformationMethod() {
          @Override
          public CharSequence getTransformation(CharSequence source, View view) {
            return source + "_transformed";
          }

          @Override
          public void onFocusChanged(
              View view,
              CharSequence sourceText,
              boolean focused,
              int direction,
              Rect previouslyFocusedRect) {}
        });
    assertThat(
        getMismatchDescription(withText(is("blah")), textView),
        is("view.getText() was \"text\" transformed text was \"text_transformed\""));
  }

  @Test
  public void hasTextColorTest() {
    TextView textView = new TextView(context);
    textView.setText("text");

    int color;
    if (Build.VERSION.SDK_INT <= 22) {
      color = context.getResources().getColor(R.color.green);
    } else {
      color = context.getColor(R.color.green);
    }

    textView.setTextColor(color);

    assertTrue(hasTextColor(R.color.green).matches(textView));
    assertFalse(hasTextColor(R.color.red).matches(textView));
  }

  @Test
  public void hasTextColor_withId_describe() {
    assertThat(
        getDescription(hasTextColor(R.color.green)),
        is(
            "an instance of android.widget.TextView and "
                + "textView.getCurrentTextColor() is color with ID <"
                + R.color.green
                + ">"));
  }

  @Test
  public void hasTextColor_withName_describe() {
    Matcher<View> matcher = hasTextColor(R.color.green);
    matcher.matches(new TextView(context));
    assertThat(
        getDescription(matcher),
        is(
            "an instance of android.widget.TextView and "
                + "textView.getCurrentTextColor() is color with value #FF377E00"));
  }

  @Test
  public void hasTextColor_describeMismatch() {
    TextView textView = new TextView(context);
    textView.setTextColor(Color.GREEN);
    assertThat(
        getMismatchDescription(hasTextColor(R.color.green), textView),
        is("textView.getCurrentTextColor() was #FF00FF00"));
  }

  @Test
  public void hasDescendantTest() {
    View v = new TextView(context);
    ViewGroup parent = new RelativeLayout(context);
    ViewGroup grany = new ScrollView(context);
    grany.addView(parent);
    parent.addView(v);
    assertTrue(hasDescendant(isAssignableFrom(TextView.class)).matches(grany));
    assertTrue(hasDescendant(isAssignableFrom(TextView.class)).matches(parent));
    assertFalse(hasDescendant(isAssignableFrom(ScrollView.class)).matches(parent));
    assertFalse(hasDescendant(isAssignableFrom(TextView.class)).matches(v));
  }

  @Test
  public void hasDescendant_description() {
    Matcher<View> matcher = isAssignableFrom(TextView.class);
    assertThat(
        getDescription(hasDescendant(matcher)),
        is(
            "(view "
                + getDescription(Matchers.isA(ViewGroup.class))
                + " and has descendant matching "
                + getDescription(matcher)
                + ")"));
  }

  @Test
  public void hasDescendant_mismatchDescription_notViewGroup() {
    View view = new View(context);
    assertThat(
        getMismatchDescription(hasDescendant(isAssignableFrom(TextView.class)), view),
        is("view " + getMismatchDescription(Matchers.isA(ViewGroup.class), view)));
  }

  @Test
  public void hasDescendant_mismatchDescription_noMatch() {
    ViewGroup parent = new LinearLayout(context);
    parent.addView(new View(context));
    Matcher<View> matcher = isAssignableFrom(TextView.class);
    assertThat(
        getMismatchDescription(hasDescendant(matcher), parent),
        is("no descendant matching " + getDescription(matcher) + " was found"));
  }

  @Test
  public void isDescendantOfATest() {
    View v = new TextView(context);
    ViewGroup parent = new RelativeLayout(context);
    ViewGroup grany = new ScrollView(context);
    grany.addView(parent);
    parent.addView(v);
    assertTrue(isDescendantOfA(isAssignableFrom(RelativeLayout.class)).matches(v));
    assertTrue(isDescendantOfA(isAssignableFrom(ScrollView.class)).matches(v));
    assertFalse(isDescendantOfA(isAssignableFrom(LinearLayout.class)).matches(v));
  }

  @Test
  public void isDescendantOfA_description() {
    Matcher<View> matcher = isAssignableFrom(LinearLayout.class);
    assertThat(
        getDescription(isDescendantOfA(matcher)),
        is("is descendant of a view matching " + getDescription(matcher)));
  }

  @Test
  public void isDescendantOfA_mismatchDescription_parentNotView() {
    View view = new View(context);
    Matcher<View> matcher = isAssignableFrom(LinearLayout.class);
    assertThat(
        getMismatchDescription(isDescendantOfA(matcher), view),
        is("none of the ancestors match " + getDescription(matcher)));
  }

  @Test
  public void isDisplayedTest() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);
    Matcher<View> matcher = isDisplayed();

    view.setVisibility(View.GONE);
    assertFalse(matcher.matches(view));

    view.setVisibility(View.INVISIBLE);
    assertFalse(matcher.matches(view));

    when(providerMock.get(any(), any())).thenReturn(false);
    view.setVisibility(View.VISIBLE);
    assertFalse(matcher.matches(view));

    when(providerMock.get(any(), any())).thenReturn(true);
    assertTrue(matcher.matches(view));
  }

  @Test
  public void isDisplayed_description() {
    assertThat(
        getDescription(isDisplayed()),
        is(
            "("
                + getDescription(withEffectiveVisibility(Visibility.VISIBLE))
                + " and view.getGlobalVisibleRect() to return non-empty rectangle)"));
  }

  @Test
  public void isDisplayed_mismatchDescription_wrongVisibility() {
    View view = new View(context);
    view.setVisibility(View.GONE);
    assertThat(
        getMismatchDescription(isDisplayed(), view),
        is(getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view)));
  }

  @Test
  public void isDisplayed_mismatchDescription_emptyRectangle() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);
    view.setVisibility(View.VISIBLE);
    when(providerMock.get(any(), any())).thenReturn(false);
    assertThat(
        getMismatchDescription(isDisplayed(), view),
        is("view.getGlobalVisibleRect() returned empty rectangle"));
  }

  @Test
  public void isDisplayingAtLeast_invalidPercentageRange() {
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(-1));
    assertThrows(IllegalArgumentException.class, () -> isDisplayingAtLeast(101));
  }

  @Test
  public void isDisplayingAtLeastTest() {
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
  public void isDisplayingAtLeast_description() {
    assertThat(
        getDescription(isDisplayingAtLeast(15)),
        is(
            "("
                + getDescription(withEffectiveVisibility(Visibility.VISIBLE))
                + " and view.getGlobalVisibleRect() covers at least <15> percent of the view's"
                + " area)"));
  }

  @Test
  public void isDisplayingAtLeast_mismatchDescription_wrongVisibility() {
    View view = new View(context);
    view.setVisibility(View.GONE);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is(getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view)));
  }

  @Test
  public void isDisplayingAtLeast_mismatchDescription_notVisible() {
    GlobalVisibleRectProvider providerMock = mock(GlobalVisibleRectProvider.class);
    View view = new GlobalVisibleRectTestView(context, providerMock);
    view.setVisibility(View.VISIBLE);
    when(providerMock.get(any(), any())).thenReturn(false);
    assertThat(
        getMismatchDescription(isDisplayingAtLeast(15), view),
        is("view was <0> percent visible to the user"));
  }

  @Test
  public void isDisplayingAtLeast_mismatchDescription_lowVisibility() {
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
  private interface GlobalVisibleRectProvider {
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

  private static final class MismatchTestMatcher<T> extends BaseMatcher<T> {

    private final T expected;
    private final String description;
    private final String mismatchDescription;

    MismatchTestMatcher(T expected, String description, String mismatchDescription) {
      this.expected = expected;
      this.description = description;
      this.mismatchDescription = mismatchDescription;
    }

    @Override
    public boolean matches(Object item) {
      return (expected == null && item == null) || (expected != null && expected.equals(item));
    }

    @Override
    public void describeMismatch(Object item, Description mismatchDescription) {
      mismatchDescription.appendText(this.mismatchDescription);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(this.description);
    }
  }

  @Test
  public void testAssertThat_matcherSuccess() {
    ViewMatchers.assertThat("test", new MismatchTestMatcher<String>("test", "", ""));
  }

  @Test
  public void testAssertThat_matcherFails_printsCustomMismatchDescription() {
    try {
      ViewMatchers.assertThat(
          "test",
          new MismatchTestMatcher<String>("expected", "description", "mismatchDescription"));
      fail("Expected to throw exception");
    } catch (AssertionFailedError error) {
      assertEquals("\nExpected: description\n     Got: mismatchDescription\n", error.getMessage());
    }
  }

  @Test
  public void testAssertThat_matcherFails_usesFallbackObjectToString() {
    try {
      ViewMatchers.assertThat(
          "test", new MismatchTestMatcher<String>("expected", "description", ""));
      fail("Expected to throw exception");
    } catch (AssertionFailedError error) {
      assertEquals("\nExpected: description\n     Got: test\n", error.getMessage());
    }
  }

  @Test
  public void testAssertThat_matcherFails_usesHumanReadablesForViewMatchers() {
    String description = "Expect to fail match";
    TextView view = new TextView(context);
    Matcher<TextView> matcher = new MismatchTestMatcher<>(null, description, "");
    try {
      ViewMatchers.assertThat(view, matcher);
      fail("Expected to throw exception");
    } catch (AssertionFailedError error) {
      assertEquals(
          "\nExpected: "
              + description
              + "\n     Got: "
              + view
              + "\nView Details: "
              + HumanReadables.describe(view)
              + "\n",
          error.getMessage());
    }
  }

  @Test
  public void isVisibleTest() {
    View visible = new View(context);
    visible.setVisibility(View.VISIBLE);
    View invisible = new View(context);
    invisible.setVisibility(View.INVISIBLE);
    assertTrue(withEffectiveVisibility(Visibility.VISIBLE).matches(visible));
    assertFalse(withEffectiveVisibility(Visibility.VISIBLE).matches(invisible));

    // Make the visible view invisible by giving it an invisible parent.
    ViewGroup parent = new RelativeLayout(context);
    parent.addView(visible);
    parent.setVisibility(View.INVISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.VISIBLE).matches(visible));
  }

  @Test
  public void isInvisibleTest() {
    View visible = new View(context);
    visible.setVisibility(View.VISIBLE);
    View invisible = new View(context);
    invisible.setVisibility(View.INVISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.INVISIBLE).matches(visible));
    assertTrue(withEffectiveVisibility(Visibility.INVISIBLE).matches(invisible));

    // Make the visible view invisible by giving it an invisible parent.
    ViewGroup parent = new RelativeLayout(context);
    parent.addView(visible);
    parent.setVisibility(View.INVISIBLE);
    assertTrue(withEffectiveVisibility(Visibility.INVISIBLE).matches(visible));
  }

  @Test
  public void isGoneTest() {
    View gone = new View(context);
    gone.setVisibility(View.GONE);
    View visible = new View(context);
    visible.setVisibility(View.VISIBLE);
    assertFalse(withEffectiveVisibility(Visibility.GONE).matches(visible));
    assertTrue(withEffectiveVisibility(Visibility.GONE).matches(gone));

    // Make the gone view gone by giving it a gone parent.
    ViewGroup parent = new RelativeLayout(context);
    parent.addView(visible);
    parent.setVisibility(View.GONE);
    assertTrue(withEffectiveVisibility(Visibility.GONE).matches(visible));
  }

  @Test
  public void withEffectiveVisibility_description() {
    assertThat(
        getDescription(withEffectiveVisibility(Visibility.VISIBLE)),
        is("view has effective visibility <VISIBLE>"));
    assertThat(
        getDescription(withEffectiveVisibility(Visibility.INVISIBLE)),
        is("view has effective visibility <INVISIBLE>"));
    assertThat(
        getDescription(withEffectiveVisibility(Visibility.GONE)),
        is("view has effective visibility <GONE>"));
  }

  @Test
  public void withEffectiveVisibility_visible_mismatchDescription() {
    View view = new View(context);
    view.setVisibility(View.INVISIBLE);
    assertThat(
        getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view),
        is("view.getVisibility() was <INVISIBLE>"));

    // Make the visible view invisible by giving it an invisible parent.
    ViewGroup parent = new RelativeLayout(context);
    parent.addView(view);
    view.setVisibility(View.VISIBLE);
    parent.setVisibility(View.INVISIBLE);
    assertThat(
        getMismatchDescription(withEffectiveVisibility(Visibility.VISIBLE), view),
        is("ancestor <" + parent + ">'s getVisibility() was <INVISIBLE>"));
  }

  @Test
  public void withEffectiveVisibility_invisible_mismatchDescription() {
    View view = new View(context);
    view.setVisibility(View.VISIBLE);
    assertThat(
        getMismatchDescription(withEffectiveVisibility(Visibility.INVISIBLE), view),
        is("neither view nor its ancestors have getVisibility() set to <INVISIBLE>"));
  }

  @Test
  public void withEffectiveVisibility_gone_mismatchDescription() {
    View view = new View(context);
    view.setVisibility(View.VISIBLE);
    assertThat(
        getMismatchDescription(withEffectiveVisibility(Visibility.GONE), view),
        is("neither view nor its ancestors have getVisibility() set to <GONE>"));
  }

  @Test
  public void isClickableTest() {
    View clickable = new View(context);
    clickable.setClickable(true);
    View notClickable = new View(context);
    notClickable.setClickable(false);
    assertTrue(isClickable().matches(clickable));
    assertTrue(isNotClickable().matches(notClickable));
    assertFalse(isClickable().matches(notClickable));
    assertFalse(isNotClickable().matches(clickable));
  }

  @Test
  public void isClickable_description() {
    assertThat(getDescription(isClickable()), is("view.isClickable() is <true>"));
  }

  @Test
  public void isClickable_mismatchDescription() {
    assertThat(
        getMismatchDescription(isClickable(), new View(context)),
        is("view.isClickable() was <false>"));
  }

  @Test
  public void isNotClickable_description() {
    assertThat(getDescription(isNotClickable()), is("view.isClickable() is <false>"));
  }

  @Test
  public void isNotClickable_mismatchDescription() {
    View view = new View(context);
    view.setClickable(true);
    assertThat(getMismatchDescription(isNotClickable(), view), is("view.isClickable() was <true>"));
  }

  @Test
  public void isEnabledTest() {
    View enabled = new View(context);
    enabled.setEnabled(true);
    View notEnabled = new View(context);
    notEnabled.setEnabled(false);
    assertTrue(isEnabled().matches(enabled));
    assertTrue(isNotEnabled().matches(notEnabled));
    assertFalse(isEnabled().matches(notEnabled));
    assertFalse(isNotEnabled().matches(enabled));
  }

  @Test
  public void isEnabled_description() {
    assertThat(getDescription(isEnabled()), is("view.isEnabled() is <true>"));
  }

  @Test
  public void isEnabled_mismatchDescription() {
    View view = new View(context);
    view.setEnabled(false);
    assertThat(getMismatchDescription(isEnabled(), view), is("view.isEnabled() was <false>"));
  }

  @Test
  public void isNotEnabled_description() {
    assertThat(getDescription(isNotEnabled()), is("view.isEnabled() is <false>"));
  }

  @Test
  public void isNotEnabled_mismatchDescription() {
    View view = new View(context);
    view.setEnabled(true);
    assertThat(getMismatchDescription(isNotEnabled(), view), is("view.isEnabled() was <true>"));
  }
}
