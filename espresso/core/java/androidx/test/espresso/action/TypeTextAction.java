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

package androidx.test.espresso.action;

import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SearchView;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.HumanReadables;
import java.util.Locale;
import javax.annotation.Nullable;
import org.hamcrest.Matcher;

/** Enables typing text on views. */
public final class TypeTextAction implements ViewAction {
  private static final String TAG = TypeTextAction.class.getSimpleName();

  @RemoteMsgField(order = 0)
  final String stringToBeTyped;

  @RemoteMsgField(order = 1)
  final boolean tapToFocus;

  // The click action to use when tapping to focus is needed before typing in text.
  @Nullable final GeneralClickAction clickAction;

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed). By default this action sends a tap event to the center of the view to
   * attain focus before typing.
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   */
  public TypeTextAction(String stringToBeTyped) {
    this(stringToBeTyped, true, defaultClickAction());
  }

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed).
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   * @param tapToFocus indicates whether a tap should be sent to the underlying view before typing.
   */
  @RemoteMsgConstructor
  public TypeTextAction(String stringToBeTyped, boolean tapToFocus) {
    this(stringToBeTyped, tapToFocus, null);
  }

  /**
   * Constructs {@link TypeTextAction} with given string. If the string is empty it results in no-op
   * (nothing is typed).
   *
   * @param stringToBeTyped String To be typed by {@link TypeTextAction}
   * @param tapToFocus indicates whether a tap should be sent to the underlying view before typing.
   * @param clickAction the click action instance to use when tapping to focus. Can be {@code null}
   *     if {@code tapToFocus} is false. If {@code tapToFocus} is true but no {@code clickAction} is
   *     specified, a default click action will be used for tapping.
   */
  public TypeTextAction(
      String stringToBeTyped, boolean tapToFocus, GeneralClickAction clickAction) {
    checkNotNull(stringToBeTyped);
    this.stringToBeTyped = stringToBeTyped;
    this.tapToFocus = tapToFocus;
    this.clickAction = clickAction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    Matcher<View> matchers = allOf(isDisplayed());
    if (!tapToFocus) {
      matchers = allOf(matchers, hasFocus());
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      return allOf(matchers, supportsInputMethods());
    } else {
      // SearchView does not support input methods itself (rather it delegates to an internal text
      // view for input).
      return allOf(matchers, anyOf(supportsInputMethods(), isAssignableFrom(SearchView.class)));
    }
  }

  @Override
  public void perform(UiController uiController, View view) {
    // No-op if string is empty.
    if (stringToBeTyped.length() == 0) {
      Log.w(TAG, "Supplied string is empty resulting in no-op (nothing is typed).");
      return;
    }

    if (tapToFocus) {
      // Perform a click.
      if (clickAction == null) {
        // Uses the default click action if none is specified.
        defaultClickAction().perform(uiController, view);
      } else {
        clickAction.perform(uiController, view);
      }
      uiController.loopMainThreadUntilIdle();
    }

    try {
      if (!uiController.injectString(stringToBeTyped)) {
        Log.e(TAG, "Failed to type text: " + stringToBeTyped);
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new RuntimeException("Failed to type text: " + stringToBeTyped))
            .build();
      }
    } catch (InjectEventSecurityException e) {
      Log.e(TAG, "Failed to type text: " + stringToBeTyped);
      throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(e)
          .build();
    }
  }

  @Override
  public String getDescription() {
    return String.format(Locale.ROOT, "type text(%s)", stringToBeTyped);
  }

  private static GeneralClickAction defaultClickAction() {
    return new GeneralClickAction(
        Tap.SINGLE,
        GeneralLocation.CENTER,
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY);
  }
}
