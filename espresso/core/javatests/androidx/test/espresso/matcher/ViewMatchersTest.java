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
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.hasImeAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasLinks;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isFocusable;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static androidx.test.espresso.matcher.ViewMatchers.thatMatchesFirst;
import static androidx.test.espresso.matcher.ViewMatchers.withAlpha;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.UiThreadTestRule;
import androidx.test.ui.app.R;
import com.google.common.collect.Lists;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Unit tests for {@link ViewMatchers}. */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ViewMatchersTest {

  private static final int UNRECOGNIZED_INPUT_TYPE = 999999;

  private Context context;

  @Rule public ExpectedException expectedException = none();

  @Rule public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

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
    expectedException.expect(NullPointerException.class);
    withContentDescription((Matcher<CharSequence>) null);
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
  public void withContentDescriptionFromResourceId() {
    View view = new View(context);
    view.setContentDescription(context.getString(R.string.something));
    assertFalse(withContentDescription(R.string.other_string).matches(view));
    assertTrue(withContentDescription(R.string.something).matches(view));
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
    assertThat(withId(5).toString(), is("with id: 5"));
  }

  @Test
  public void withId_describeWithFailedResourceLookup() {
    View view = new View(context);
    Matcher<View> matcher = withId(5);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(matcher.toString(), is("with id: 5 (resource name not found)"));
  }

  @Test
  public void withId_describeWithResourceLookup() {
    View view = new View(context);
    Matcher<View> matcher = withId(R.id.testId1);
    // Running matches will allow withId to grab resources from view Context
    matcher.matches(view);
    assertThat(matcher.toString(), containsString("id/testId1"));
  }

  @Test
  public void withTagNull() {
    try {
      withTagKey(0, null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }

    try {
      withTagValue(null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
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
  public void withTextNull() {
    try {
      withText((Matcher<String>) null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
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
  public void isClickableTest() {
    View clickable = new View(context);
    clickable.setClickable(true);
    View notClickable = new View(context);
    notClickable.setClickable(false);
    assertTrue(isClickable().matches(clickable));
    assertFalse(isClickable().matches(notClickable));
  }

  @Test
  public void isEnabledTest() {
    View enabled = new View(context);
    enabled.setEnabled(true);
    View notEnabled = new View(context);
    notEnabled.setEnabled(false);
    assertTrue(isEnabled().matches(enabled));
    assertFalse(isEnabled().matches(notEnabled));
  }

  @Test
  public void isFocusableTest() {
    View focusable = new View(context);
    focusable.setFocusable(true);
    View notFocusable = new View(context);
    notFocusable.setFocusable(false);
    assertTrue(isFocusable().matches(focusable));
    assertFalse(isFocusable().matches(notFocusable));
  }

  @Test
  public void isSelectedTest() {
    View selected = new View(context);
    selected.setSelected(true);
    View notSelected = new View(context);
    notSelected.setSelected(false);
    assertTrue(isSelected().matches(selected));
    assertFalse(isSelected().matches(notSelected));
  }

  @Test
  public void withTextResourceIdTest() {
    TextView textView = new TextView(context);
    textView.setText(R.string.something);
    assertTrue(withText(R.string.something).matches(textView));
    assertFalse(withText(R.string.other_string).matches(textView));
  }

  @Test
  public void withTextResourceId_charSequenceTest() {
    TextView textView = new TextView(context);
    String expectedText = context.getResources().getString(R.string.something);
    Spannable textSpan = Spannable.Factory.getInstance().newSpannable(expectedText);
    textSpan.setSpan(new ForegroundColorSpan(Color.RED), 0, expectedText.length() - 1, 0);
    textView.setText(textSpan);
    assertTrue(withText(R.string.something).matches(textView));
    assertFalse(withText(R.string.other_string).matches(textView));
  }

  @Test
  public void withSubstringTest() {
    TextView textView = new TextView(context);
    String testText = "test text!";
    textView.setText(testText);
    assertTrue(withText(containsString(testText)).matches(textView));
    assertTrue(withText(containsString("text")).matches(textView));
    assertFalse(withText(containsString("blah")).matches(textView));
  }

  @Test
  public void withHintStringTest() {
    TextView textView = new TextView(context);
    String testText = "test text!";
    textView.setHint(testText);
    assertTrue(withHint(is(testText)).matches(textView));
    assertFalse(withHint(is("blah")).matches(textView));
  }

  @Test
  public void withHintNullTest() {
    TextView textView = new TextView(context);
    textView.setHint(null);
    assertTrue(withHint(nullValue(String.class)).matches(textView));
    assertFalse(withHint("").matches(textView));
  }

  @Test
  public void withHintResourceIdTest() {
    TextView textView = new TextView(context);
    textView.setHint(R.string.something);
    assertTrue(withHint(R.string.something).matches(textView));
    assertFalse(withHint(R.string.other_string).matches(textView));
    // test the case of resource is not found, espresso should not crash
    assertFalse(withHint(R.string.other_string + 100).matches(textView));
  }

  @Test
  public void withHintResourceId_charSequenceTest() {
    TextView textView = new TextView(context);
    String expectedText = context.getResources().getString(R.string.something);
    Spannable textSpan = Spannable.Factory.getInstance().newSpannable(expectedText);
    textSpan.setSpan(new ForegroundColorSpan(Color.RED), 0, expectedText.length() - 1, 0);
    textView.setHint(textSpan);
    assertTrue(withHint(R.string.something).matches(textView));
    assertFalse(withHint(R.string.other_string).matches(textView));
  }

  @Test
  @SdkSuppress(minSdkVersion = 11)
  public void withAlphaTest() {
    View view = new TextView(context);

    view.setAlpha(0f);
    assertTrue(withAlpha(0f).matches(view));
    assertFalse(withAlpha(0.01f).matches(view));
    assertFalse(withAlpha(0.99f).matches(view));
    assertFalse(withAlpha(1f).matches(view));

    view.setAlpha(0.01f);
    assertFalse(withAlpha(0f).matches(view));
    assertTrue(withAlpha(0.01f).matches(view));
    assertFalse(withAlpha(0.99f).matches(view));
    assertFalse(withAlpha(1f).matches(view));

    view.setAlpha(1f);
    assertFalse(withAlpha(0f).matches(view));
    assertFalse(withAlpha(0.01f).matches(view));
    assertFalse(withAlpha(0.99f).matches(view));
    assertTrue(withAlpha(1f).matches(view));
  }

  @Test
  public void withAlpha_description() {
    assertThat(withAlpha(0.5f).toString(), is("has alpha: <0.5F>"));
  }

  @Test
  public void withParentTest() {
    View view1 = new TextView(context);
    View view2 = new TextView(context);
    View view3 = new TextView(context);
    ViewGroup tiptop = new RelativeLayout(context);
    ViewGroup secondLevel = new RelativeLayout(context);
    secondLevel.addView(view2);
    secondLevel.addView(view3);
    tiptop.addView(secondLevel);
    tiptop.addView(view1);
    assertTrue(withParent(is((View) tiptop)).matches(view1));
    assertTrue(withParent(is((View) tiptop)).matches(secondLevel));
    assertFalse(withParent(is((View) tiptop)).matches(view2));
    assertFalse(withParent(is((View) tiptop)).matches(view3));
    assertFalse(withParent(is((View) secondLevel)).matches(view1));

    assertTrue(withParent(is((View) secondLevel)).matches(view2));
    assertTrue(withParent(is((View) secondLevel)).matches(view3));

    assertFalse(withParent(is(view3)).matches(view3));
  }

  @Test
  public void withChildTest() {
    View view1 = new TextView(context);
    View view2 = new TextView(context);
    View view3 = new TextView(context);
    ViewGroup tiptop = new RelativeLayout(context);
    ViewGroup secondLevel = new RelativeLayout(context);
    secondLevel.addView(view2);
    secondLevel.addView(view3);
    tiptop.addView(secondLevel);
    tiptop.addView(view1);
    assertTrue(withChild(is(view1)).matches(tiptop));
    assertTrue(withChild(is((View) secondLevel)).matches(tiptop));
    assertFalse(withChild(is((View) tiptop)).matches(view1));
    assertFalse(withChild(is(view2)).matches(tiptop));
    assertFalse(withChild(is(view1)).matches(secondLevel));

    assertTrue(withChild(is(view2)).matches(secondLevel));

    assertFalse(withChild(is(view3)).matches(view3));
  }

  @Test
  public void isRootViewTest() {
    ViewGroup rootView =
        new ViewGroup(context) {
          @Override
          protected void onLayout(boolean changed, int l, int t, int r, int b) {}
        };

    View view = new View(context);
    rootView.addView(view);

    assertTrue(isRoot().matches(rootView));
    assertFalse(isRoot().matches(view));
  }

  @Test
  public void hasSiblingTest() {
    TextView v1 = new TextView(context);
    v1.setText("Bill Odama");
    Button v2 = new Button(context);
    View v3 = new View(context);
    ViewGroup parent = new LinearLayout(context);
    parent.addView(v1);
    parent.addView(v2);
    parent.addView(v3);
    assertTrue(hasSibling(withText("Bill Odama")).matches(v2));
    assertFalse(hasSibling(is(v3)).matches(parent));
  }

  @UiThreadTest
  @Test
  public void hasImeActionTest() {
    EditText editText = new EditText(context);
    assertFalse(hasImeAction(EditorInfo.IME_ACTION_GO).matches(editText));
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    assertFalse(hasImeAction(EditorInfo.IME_ACTION_GO).matches(editText));
    assertTrue(hasImeAction(EditorInfo.IME_ACTION_NEXT).matches(editText));
  }

  @Test
  public void hasImeActionNoInputConnection() {
    Button button = new Button(context);
    assertFalse(hasImeAction(0).matches(button));
  }

  @Test
  @UiThreadTest
  public void supportsInputMethodsTest() {
    Button button = new Button(context);
    EditText editText = new EditText(context);
    assertFalse(supportsInputMethods().matches(button));
    assertTrue(supportsInputMethods().matches(editText));
  }

  @Test
  public void hasLinksTest() {
    TextView viewWithLinks = new TextView(context);
    viewWithLinks.setText("Here is a www.google.com link");
    Linkify.addLinks(viewWithLinks, Linkify.ALL);
    assertTrue(hasLinks().matches(viewWithLinks));

    TextView viewWithNoLinks = new TextView(context);
    viewWithNoLinks.setText("Here is an unlikified www.google.com");
    assertFalse(hasLinks().matches(viewWithNoLinks));
  }

  @UiThreadTest
  @Test
  public void withSpinnerTextResourceId() {
    Spinner spinner = new Spinner(this.context);
    List<String> values = Lists.newArrayList();
    values.add(this.context.getString(R.string.something));
    values.add(this.context.getString(R.string.other_string));
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, values);
    spinner.setAdapter(adapter);
    spinner.setSelection(0);
    assertTrue(withSpinnerText(R.string.something).matches(spinner));
    assertFalse(withSpinnerText(R.string.other_string).matches(spinner));
  }

  @UiThreadTest
  @Test
  public void withSpinnerTextString() {
    Spinner spinner = new Spinner(this.context);
    List<String> values = Lists.newArrayList();
    values.add("Hello World");
    values.add("Goodbye!!");
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, values);
    spinner.setAdapter(adapter);
    spinner.setSelection(0);
    spinner.setTag("spinner");
    assertTrue(withSpinnerText(is("Hello World")).matches(spinner));
    assertFalse(withSpinnerText(is("Goodbye!!")).matches(spinner));
    assertFalse(withSpinnerText(is("")).matches(spinner));
  }

  @Test
  public void withSpinnerTextNull() {
    try {
      withSpinnerText((Matcher<String>) null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
  }

  @Test
  @UiThreadTest
  public void hasErrorTextReturnsTrue_WithCorrectErrorString() {
    EditText editText = new EditText(context);
    editText.setError("TEST");
    assertTrue(hasErrorText("TEST").matches(editText));
  }

  @Test
  @UiThreadTest
  public void hasErrorTextNullTest() {
    EditText editText = new EditText(context);
    editText.setError(null);
    assertTrue(hasErrorText(nullValue(String.class)).matches(editText));
    assertFalse(hasErrorText("").matches(editText));
  }

  @Test
  @UiThreadTest
  public void hasErrorTextReturnsFalse_WithDifferentErrorString() {
    EditText editText = new EditText(context);
    editText.setError("TEST");
    assertFalse(hasErrorText("TEST1").matches(editText));
  }

  @Test
  public void hasErrorTextShouldFail_WithNullString() {
    try {
      hasErrorText((Matcher<String>) null);
      fail("Should of thrown NPE");
    } catch (NullPointerException e) {
      // Good, this is expected.
    }
  }

  @Test
  // TODO(b/117557353): investigate failures on API 28
  @SdkSuppress(minSdkVersion = 16, maxSdkVersion = 27)
  public void hasBackgroundTest() {
    View viewWithBackground = new View(context);
    viewWithBackground.setBackground(context.getResources().getDrawable(R.drawable.drawable_1));

    assertTrue(hasBackground(R.drawable.drawable_1).matches(viewWithBackground));
  }


  @Test
  @UiThreadTest
  public void withInputType_ReturnsTrueIf_CorrectInput() {
    EditText editText = new EditText(context);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    assertTrue(withInputType(InputType.TYPE_CLASS_NUMBER).matches(editText));
  }

  @Test
  @UiThreadTest
  public void withInputType_ReturnsFalseIf_IncorrectInput() {
    EditText editText = new EditText(context);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    assertFalse(withInputType(InputType.TYPE_CLASS_TEXT).matches(editText));
  }

  @Test
  @UiThreadTest
  public void withInputType_ShouldNotCrashIf_InputTypeIsNotRecognized() {
    EditText editText = new EditText(context);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    assertFalse(withInputType(UNRECOGNIZED_INPUT_TYPE).matches(editText));
  }

  @Test
  public void withParentIndex_twoChildren() {
    LinearLayout linearLayout = new LinearLayout(context);
    View view0 = new View(context);
    View view1 = new View(context);
    View view2 = new View(context);
    linearLayout.addView(view0);
    linearLayout.addView(view1);

    assertTrue(withParentIndex(0).matches(view0));
    assertTrue(withParentIndex(1).matches(view1));

    assertFalse(withParentIndex(1).matches(view0));
    assertFalse(withParentIndex(0).matches(view1));
    assertFalse(withParentIndex(0).matches(view2));
    assertFalse(withParentIndex(1).matches(view2));
    assertFalse(withParentIndex(2).matches(view2));
  }

  @Test
  public void withParentIndex_noChildren() {
    View view0 = new View(context);

    assertFalse(withParentIndex(0).matches(view0));
    assertFalse(withParentIndex(1).matches(view0));
  }

  @Test
  public void hasChildCount_twoChildren() {
    LinearLayout linearLayout = new LinearLayout(context);
    linearLayout.addView(new View(context));
    linearLayout.addView(new View(context));

    assertFalse(hasChildCount(0).matches(linearLayout));
    assertFalse(hasChildCount(1).matches(linearLayout));
    assertTrue(hasChildCount(2).matches(linearLayout));
    assertFalse(hasChildCount(3).matches(linearLayout));
  }

  @Test
  public void hasChildCount_noChildren() {
    View linearLayout = new LinearLayout(context);

    assertTrue(hasChildCount(0).matches(linearLayout));
    assertFalse(hasChildCount(1).matches(linearLayout));
  }

  @Test
  public void hasMinimumChildCount_twoChildren() {
    LinearLayout linearLayout = new LinearLayout(context);
    linearLayout.addView(new View(context));
    linearLayout.addView(new View(context));

    assertTrue(hasMinimumChildCount(0).matches(linearLayout));
    assertTrue(hasMinimumChildCount(1).matches(linearLayout));
    assertTrue(hasMinimumChildCount(2).matches(linearLayout));
    assertFalse(hasMinimumChildCount(3).matches(linearLayout));
  }

  @Test
  public void hasMinimumChildCount_noChildren() {
    View linearLayout = new LinearLayout(context);

    assertTrue(hasMinimumChildCount(0).matches(linearLayout));
    assertFalse(hasMinimumChildCount(1).matches(linearLayout));
  }

  @Test
  public void withResourceNameTest() {
    View view = new View(context);
    view.setId(R.id.testId1);
    assertTrue(withResourceName("testId1").matches(view));
    assertFalse(withResourceName("testId2").matches(view));
    assertFalse(withResourceName("3o1298756").matches(view));
  }

  @NonNull
  private View createViewWithId(int viewId) {
    View view = new View(context);
    view.setId(viewId);
    return view;
  }
}
