package androidx.test.espresso.matcher;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Core matcher for {@link Root} objects.
 *
 * <p>This class contains the minimum code needed to identify the root node
 * (RootMatchersCore.DEFAULT.matches()). It should have the as few dependencies as possible (e.g. no
 * Guava) because it will be used in the testrunner for non-Espresso tests.
 */
public final class RootMatchersCore {
  private static final String TAG = "RootMatchersCore";

  private RootMatchersCore() {}

  public static final Matcher<Root> DEFAULT =
      allOf(
          hasWindowLayoutParams(),
          allOf(
              anyOf(
                  allOf(isDialog(), withDecorView(hasWindowFocus())),
                  isSubwindowOfCurrentActivity()),
              isFocusable()));

  /** Matches {@link Root}s that can take window focus. */
  static Matcher<Root> isFocusable() {
    return new IsFocusable();
  }

  /**
   * Matches {@link Root}s that are dialogs (i.e. is not a window of the currently resumed
   * activity).
   */
  public static Matcher<Root> isDialog() {
    return new IsDialog();
  }

  /** Matches {@link Root}s with decor views that match the given view matcher. */
  public static Matcher<Root> withDecorView(final Matcher<View> decorViewMatcher) {
    if (decorViewMatcher == null) {
      throw new NullPointerException();
    }
    return new WithDecorView(decorViewMatcher);
  }

  private static Matcher<View> hasWindowFocus() {
    return new HasWindowFocus();
  }

  static Matcher<Root> hasWindowLayoutParams() {
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
    List<IBinder> tokens = new ArrayList<>();
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
      int flags = root.getWindowLayoutParams().flags;
      // return true if FLAG_NOT_FOCUSABLE flag is not set
      return (flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
          != WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
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
      int type = root.getWindowLayoutParams().type;
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
      return root.getWindowLayoutParams() != null;
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
