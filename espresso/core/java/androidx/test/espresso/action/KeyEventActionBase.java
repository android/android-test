/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.espresso.action;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.util.ActivityLifecycles.hasForegroundActivities;
import static androidx.test.espresso.util.ActivityLifecycles.hasTransitioningActivities;
import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.Collection;
import java.util.Locale;
import kotlin.collections.CollectionsKt;
import org.hamcrest.Matcher;

/** Enables pressing KeyEvents on views. */
class KeyEventActionBase implements ViewAction {
  private static final String TAG = "KeyEventActionBase";

  public static final int BACK_ACTIVITY_TRANSITION_MILLIS_DELAY = 150;
  public static final int CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS = 4;
  public static final int CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY = 150;

  // TODO(b/35108759): move away from manually registering this field and use annotation instead
  final EspressoKey espressoKey;

  KeyEventActionBase(EspressoKey espressoKey) {
    this.espressoKey = checkNotNull(espressoKey);
  }

  @Override
  public Matcher<View> getConstraints() {
    return isDisplayed();
  }

  @Override
  public String getDescription() {
    return String.format(Locale.ROOT, "send %s key event", this.espressoKey);
  }

  @Override
  public void perform(UiController uiController, View view) {
    try {
      if (!sendKeyEvent(uiController)) {
        Log.e(TAG, "Failed to inject espressoKey event: " + this.espressoKey);
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(
                new RuntimeException("Failed to inject espressoKey event " + this.espressoKey))
            .build();
      }
    } catch (InjectEventSecurityException e) {
      Log.e(TAG, "Failed to inject espressoKey event: " + this.espressoKey);
      throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(e)
          .build();
    }
  }

  private boolean sendKeyEvent(UiController controller) throws InjectEventSecurityException {

    boolean injected = false;
    long eventTime = SystemClock.uptimeMillis();
    for (int attempts = 0; !injected && attempts < 4; attempts++) {
      injected =
          controller.injectKeyEvent(
              new KeyEvent(
                  eventTime,
                  eventTime,
                  KeyEvent.ACTION_DOWN,
                  this.espressoKey.getKeyCode(),
                  0,
                  this.espressoKey.getMetaState()));
    }

    if (!injected) {
      // it is not a transient failure... :(
      return false;
    }

    injected = false;
    eventTime = SystemClock.uptimeMillis();
    for (int attempts = 0; !injected && attempts < 4; attempts++) {
      injected =
          controller.injectKeyEvent(
              new KeyEvent(
                  eventTime,
                  eventTime,
                  KeyEvent.ACTION_UP,
                  this.espressoKey.getKeyCode(),
                  0,
                  this.espressoKey.getMetaState()));
    }

    return injected;
  }

  static Activity getCurrentActivity() {
    Collection<Activity> resumedActivities =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    return CollectionsKt.single(resumedActivities);
  }

  static void waitForStageChangeInitialActivity(UiController controller, Activity initialActivity) {
    if (isActivityResumed(initialActivity)) {
      // The activity transition hasn't happened yet, wait for it.
      controller.loopMainThreadForAtLeast(BACK_ACTIVITY_TRANSITION_MILLIS_DELAY);
      if (isActivityResumed(initialActivity)) {
        Log.i(
            TAG,
            "Back was pressed but there was no Activity stage transition in "
                + BACK_ACTIVITY_TRANSITION_MILLIS_DELAY
                + "ms. Pressing back may trigger an activity stage transition if the activity is"
                + " finished as a result. However, the activity may handle the back behavior in"
                + " any number of other ways internally as well, such as popping the fragment back"
                + " stack, dismissing a dialog, otherwise manually transacting fragments, etc.");
      }
    }
  }

  private static boolean isActivityResumed(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
        == Stage.RESUMED;
  }

  static void waitForPendingForegroundActivities(UiController controller, boolean conditional) {
    ActivityLifecycleMonitor activityLifecycleMonitor =
        ActivityLifecycleMonitorRegistry.getInstance();
    boolean pendingForegroundActivities = false;
    for (int attempts = 0; attempts < CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS; attempts++) {
      controller.loopMainThreadUntilIdle();
      pendingForegroundActivities = hasTransitioningActivities(activityLifecycleMonitor);
      if (pendingForegroundActivities) {
        controller.loopMainThreadForAtLeast(CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY);
      } else {
        break;
      }
    }

    // Pressing back can kill the app: log a warning.
    if (!hasForegroundActivities(activityLifecycleMonitor)) {
      if (conditional) {
        throw new NoActivityResumedException("Pressed back and killed the app");
      }
      Log.w(TAG, "Pressed back and hopped to a different process or potentially killed the app");
    }

    if (pendingForegroundActivities) {
      Log.w(
          TAG,
          "Back was pressed and left the application in an inconsistent state even after "
              + (CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY
                  * CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS)
              + "ms.");
    }
  }
}
