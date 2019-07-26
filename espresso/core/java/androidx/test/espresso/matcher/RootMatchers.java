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

import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.is;

import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import androidx.test.espresso.Root;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of matchers for {@link Root} objects. */
public final class RootMatchers {
  private static final String TAG = "RootMatchers";

  private RootMatchers() {}

  /** Espresso's default {@link Root} matcher. */
  @SuppressWarnings("unchecked")
  public static final Matcher<Root> DEFAULT = RootMatchersCore.DEFAULT;

  /** Matches {@link Root}s that can take window focus. */
  public static Matcher<Root> isFocusable() {
    return RootMatchersCore.isFocusable();
  }

  /** Matches {@link Root}s that can receive touch events. */
  public static Matcher<Root> isTouchable() {
    return new IsTouchable();
  }

  /**
   * Matches {@link Root}s that are dialogs (i.e. is not a window of the currently resumed
   * activity).
   */
  public static Matcher<Root> isDialog() {
    return RootMatchersCore.isDialog();
  }

  /**
   * Matches {@link Root}s that are system alert windows i.e. shown on top of all other applications
   * and is not a window of the currently resumed activity
   *
   * <p>Apps using this type of windows require the following permission: <code>
   * android.permission.SYSTEM_ALERT_WINDOW</code>
   */
  public static Matcher<Root> isSystemAlertWindow() {
    return new IsSystemAlertWindow();
  }

  /**
   * Matches {@link Root}s that are popups - like autocomplete suggestions or the actionbar spinner.
   */
  public static Matcher<Root> isPlatformPopup() {
    return new IsPlatformPopup();
  }

  /** Matches {@link Root}s with decor views that match the given view matcher. */
  public static Matcher<Root> withDecorView(final Matcher<View> decorViewMatcher) {
    return RootMatchersCore.withDecorView(decorViewMatcher);
  }

  public static Matcher<Root> hasWindowLayoutParams() {
    return RootMatchersCore.hasWindowLayoutParams();
  }


  static final class IsTouchable extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsTouchable() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is touchable");
    }

    @Override
    public boolean matchesSafely(Root root) {
      int flags = root.getWindowLayoutParams().flags;
      // return true if FLAG_NOT_TOUCHABLE flag is not set
      return (flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
          != WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }
  }

  static final class IsSystemAlertWindow extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsSystemAlertWindow() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is system alert window");
    }

    @Override
    public boolean matchesSafely(Root root) {
      int type = root.getWindowLayoutParams().type;
      // System-specific window types live between FIRST_SYSTEM_WINDOW and LAST_SYSTEM_WINDOW
      if ((WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW < type
          && WindowManager.LayoutParams.LAST_SYSTEM_WINDOW > type)) {
        IBinder windowToken = root.getDecorView().getWindowToken();
        IBinder appToken = root.getDecorView().getApplicationWindowToken();
        if (windowToken == appToken) {
          // windowToken == appToken means this window isn't contained by any other windows.
          // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
          // therefore it must be a dialog box.
          return true;
        }
      }
      return false;
    }
  }

  static final class IsPlatformPopup extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsPlatformPopup() {}

    @Override
    public boolean matchesSafely(Root item) {
      String popupClassName = "android.widget.PopupWindow$PopupViewContainer";
      if (Build.VERSION.SDK_INT >= 23) {
        popupClassName = "android.widget.PopupWindow$PopupDecorView";
      }
      return RootMatchersCore.withDecorView(withClassName(is(popupClassName))).matches(item);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with decor view of type PopupWindow$PopupViewContainer");
    }
  }

}
