/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */
package androidx.test.espresso.action;

import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.view.KeyEvent;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.proto.action.ViewActions.CloseKeyboardActionProto;
import androidx.test.espresso.proto.action.ViewActions.EditorActionProto;
import androidx.test.espresso.proto.action.ViewActions.KeyEventActionProto;
import androidx.test.espresso.proto.action.ViewActions.ReplaceTextActionProto;
import androidx.test.espresso.proto.action.ViewActions.SwipeViewActionProto;
import androidx.test.espresso.proto.action.ViewActions.TypeTextActionProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all actions under {@link ViewActions} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteViewActionsTest {
  private static final String TEXT_TO_SET = "Cortado";
  private static final int KEY_CODE = KeyEvent.KEYCODE_0;
  private static final int META_STATE = 0 | KeyEvent.META_SHIFT_ON;

  @Before
  public void registerViewActionsWithRegistry() {
    RemoteViewActions.init(RemoteDescriptorRegistry.getInstance());
  }

  @Test
  public void closeKeyboardAction_transformationToProto() {
    ViewAction closeKeyboardAction = closeSoftKeyboard();
    CloseKeyboardActionProto closeKeyboardActionProto =
        (CloseKeyboardActionProto) new GenericRemoteMessage(closeKeyboardAction).toProto();
    assertThat(closeKeyboardActionProto, notNullValue());
  }

  @Test
  public void closeKeyboardAction_transformationFromProto() {
    ViewAction closeKeyboardAction = closeSoftKeyboard();
    CloseKeyboardActionProto closeKeyboardActionProto =
        (CloseKeyboardActionProto) new GenericRemoteMessage(closeKeyboardAction).toProto();

    CloseKeyboardAction closeKeyboardActionFromProto =
        (CloseKeyboardAction) GenericRemoteMessage.FROM.fromProto(closeKeyboardActionProto);
    assertThat(closeKeyboardActionFromProto, notNullValue());
  }

  @Test
  public void editorAction_transformationToProto() {
    ViewAction editorAction = pressImeActionButton();
    EditorActionProto editorActionActionProto =
        (EditorActionProto) new GenericRemoteMessage(editorAction).toProto();
    assertThat(editorActionActionProto, notNullValue());
  }

  @Test
  public void editorAction_transformationFromProto() {
    ViewAction editorAction = pressImeActionButton();
    EditorActionProto editorActionActionProto =
        (EditorActionProto) new GenericRemoteMessage(editorAction).toProto();

    EditorAction editorActionFromProto =
        (EditorAction) GenericRemoteMessage.FROM.fromProto(editorActionActionProto);
    assertThat(editorActionFromProto, notNullValue());
  }

  @Test
  public void replaceTextAction_transformationToProto() {
    ViewAction replaceTextAction = replaceText(TEXT_TO_SET);
    ReplaceTextActionProto replaceTextActionProto =
        (ReplaceTextActionProto) new GenericRemoteMessage(replaceTextAction).toProto();
    assertThat(replaceTextActionProto, notNullValue());
  }

  @Test
  public void replaceTextAction_transformationFromProto() {
    ViewAction replaceTextAction = replaceText(TEXT_TO_SET);
    ReplaceTextActionProto replaceTextActionProto =
        (ReplaceTextActionProto) new GenericRemoteMessage(replaceTextAction).toProto();

    ReplaceTextAction replaceTextActionFromProto =
        (ReplaceTextAction) GenericRemoteMessage.FROM.fromProto(replaceTextActionProto);
    assertThat(replaceTextActionFromProto.stringToBeSet, equalTo(TEXT_TO_SET));
  }

  @Test
  public void typeTextAction_transformationToProto() {
    ViewAction typeTextAction = typeText(TEXT_TO_SET);
    TypeTextActionProto typeTextActionProto =
        (TypeTextActionProto) new GenericRemoteMessage(typeTextAction).toProto();
    assertThat(typeTextActionProto, notNullValue());
  }

  @Test
  public void typeTextAction_transformationFromProto() {
    ViewAction typeTextAction = typeTextIntoFocusedView(TEXT_TO_SET);
    TypeTextActionProto typeTextActionProto =
        (TypeTextActionProto) new GenericRemoteMessage(typeTextAction).toProto();

    TypeTextAction typeTextActionFromProto =
        (TypeTextAction) GenericRemoteMessage.FROM.fromProto(typeTextActionProto);
    assertThat(typeTextActionFromProto.stringToBeTyped, equalTo(TEXT_TO_SET));
    assertThat(typeTextActionFromProto.tapToFocus, is(false));
  }

  @Test
  public void keyEventAction_transformationToProto() {
    EspressoKey espressoKey =
        new EspressoKey.Builder().withKeyCode(KEY_CODE).withShiftPressed(true).build();
    KeyEventAction keyEventAction = (KeyEventAction) pressKey(espressoKey);
    KeyEventActionProto keyEventActionProto =
        (KeyEventActionProto) new GenericRemoteMessage(keyEventAction).toProto();
    assertThat(keyEventActionProto, notNullValue());
  }

  @Test
  public void keyEventAction_transformationFromProto() {
    EspressoKey espressoKey =
        new EspressoKey.Builder().withKeyCode(KEY_CODE).withShiftPressed(true).build();
    KeyEventAction keyEventAction = (KeyEventAction) pressKey(espressoKey);
    KeyEventActionProto keyEventActionProto =
        (KeyEventActionProto) new GenericRemoteMessage(keyEventAction).toProto();

    KeyEventAction keyEventActionFromProto =
        (KeyEventAction) GenericRemoteMessage.FROM.fromProto(keyEventActionProto);

    assertThat(keyEventActionFromProto.espressoKey.getKeyCode(), equalTo(KEY_CODE));
    assertThat(keyEventActionFromProto.espressoKey.getMetaState(), equalTo(META_STATE));
  }

  @Test
  public void swipeAction_transformationToProto() {
    SwipeViewActionProto swipeDownActionProto =
        new GeneralSwipeActionRemoteMessage((GeneralSwipeAction) swipeUp()).toProto();
    assertThat(swipeDownActionProto, notNullValue());
  }

  @Test
  public void swipeAction_transformationFromProto() {
    TranslatedCoordinatesProvider expectedTCP =
        new TranslatedCoordinatesProvider(GeneralLocation.BOTTOM_CENTER, 0, -0.083f);
    SwipeViewActionProto swipeDownActionProto =
        new GeneralSwipeActionRemoteMessage((GeneralSwipeAction) swipeUp()).toProto();

    GeneralSwipeAction swipeAction =
        (GeneralSwipeAction) GeneralSwipeActionRemoteMessage.FROM.fromProto(swipeDownActionProto);
    assertThat(swipeAction.swiper, equalTo(Swipe.FAST));
    assertThat(
        ((TranslatedCoordinatesProvider) swipeAction.startCoordinatesProvider).dx,
        equalTo(expectedTCP.dx));
    assertThat(
        ((TranslatedCoordinatesProvider) swipeAction.startCoordinatesProvider).dy,
        equalTo(expectedTCP.dy));
    assertThat(swipeAction.endCoordinatesProvider, equalTo(GeneralLocation.TOP_CENTER));
    assertThat(swipeAction.precisionDescriber, equalTo(Press.FINGER));
  }
}
