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
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import android.app.Activity;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.test.espresso.Root;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of matchers for {@link Root} objects. */
public final class RootMatchers {
  private static final String TAG = "RootMatchers";

  private RootMatchers() {}

  /** Espresso's default {@link Root} matcher. */
  @SuppressWarnings("unchecked")
  public static final Matcher<Root> DEFAULT =
      allOf(
          hasWindowLayoutParams(),
          allOf(
              anyOf(
                  allOf(isDialog(), withDecorView(hasWindowFocus())),
                  isSubwindowOfCurrentActivity()),
              isFocusable()));

  /** Matches {@link Root}s that can take window focus. */
  public static Matcher<Root> isFocusable() {
    return new IsFocusable();
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
    return new IsDialog();
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
    checkNotNull(decorViewMatcher);
    return new WithDecorView(decorViewMatcher);
  }

  private static Matcher<View> hasWindowFocus() {
    return new HasWindowFocus();
  }

  public static Matcher<Root> hasWindowLayoutParams() {
    return new HasWindowLayoutParams();
  }

  private static Matcher<Root> isSubwindowOfCurrentActivity() {
    return new IsSubwindowOfCurrentActivity();
  }

  private static List<IBinder> getResumedActivityTokens() {
    ActivityLifecycleMonitor activityLifecycleMonitor =
        ActivityLifecycleMonitorRegistry.getInstance();
    Collection<Activity> resumedActivities =
        activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      Log.w(
          TAG,
          "suppressed: NoActivityResumedException(\"At least one activity should"
              + " be in RESUMED stage.\"");
    }
    List<IBinder> tokens = Lists.newArrayList();
    for (Activity activity : resumedActivities) {
      tokens.add(activity.getWindow().getDecorView().getApplicationWindowToken());
    }
    return tokens;
  }

  static final class IsFocusable extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsFocusable() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is focusable");
    }

    @Override
    public boolean matchesSafely(Root root) {
      int flags = root.getWindowLayoutParams().get().flags;
      // return true if FLAG_NOT_FOCUSABLE flag is not set
      return (flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
          != WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
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
      int flags = root.getWindowLayoutParams().get().flags;
      // return true if FLAG_NOT_TOUCHABLE flag is not set
      return (flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
          != WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }
  }

  static final class IsDialog extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsDialog() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is dialog");
    }

    @Override
    public boolean matchesSafely(Root root) {
      int type = root.getWindowLayoutParams().get().type;
      if ((type != WindowManager.LayoutParams.TYPE_BASE_APPLICATION
          && type < WindowManager.LayoutParams.LAST_APPLICATION_WINDOW)) {
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

  static final class IsSystemAlertWindow extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsSystemAlertWindow() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is system alert window");
    }

    @Override
    public boolean matchesSafely(Root root) {
      int type = root.getWindowLayoutParams().get().type;
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
      return withDecorView(withClassName(is(popupClassName))).matches(item);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with decor view of type PopupWindow$PopupViewContainer");
    }
  }

  static final class WithDecorView extends TypeSafeMatcher<Root> {
    @RemoteMsgField(order = 0)
    private final Matcher<View> decorViewMatcher;

    @RemoteMsgConstructor
    public WithDecorView(final Matcher<View> decorViewMatcher) {
      this.decorViewMatcher = decorViewMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with decor view ");
      decorViewMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(Root root) {
      return decorViewMatcher.matches(root.getDecorView());
    }
  }

  static final class HasWindowFocus extends TypeSafeMatcher<View> {
    @RemoteMsgConstructor
    public HasWindowFocus() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("has window focus");
    }

    @Override
    public boolean matchesSafely(View view) {
      return view.hasWindowFocus();
    }
  }

  static final class HasWindowLayoutParams extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public HasWindowLayoutParams() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("has window layout params");
    }

    @Override
    public boolean matchesSafely(Root root) {
      return root.getWindowLayoutParams().isPresent();
    }
  }

  static final class IsSubwindowOfCurrentActivity extends TypeSafeMatcher<Root> {
    @RemoteMsgConstructor
    public IsSubwindowOfCurrentActivity() {}

    @Override
    public void describeTo(Description description) {
      description.appendText("is subwindow of current activity");
    }

    @Override
    public boolean matchesSafely(Root root) {
      return getResumedActivityTokens().contains(root.getDecorView().getApplicationWindowToken());
    }
  }
}
