/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.espresso.base;

import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.internal.util.Checks.checkState;
import static kotlin.collections.CollectionsKt.mutableListOf;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.test.espresso.EspressoException;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.NoMatchingRootException;
import androidx.test.espresso.Root;
import androidx.test.espresso.UiController;
import androidx.test.espresso.internal.inject.TargetContext;
import androidx.test.internal.platform.os.ControlledLooper;
import androidx.test.internal.util.LogUtil;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.Stage;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Provider;
import kotlin.collections.CollectionsKt;
import org.hamcrest.Matcher;

/**
 * Provides the root View of the top-most Window, with which the user can interact. View is
 * guaranteed to be in a stable state - i.e. not pending any updates from the application.
 *
 * <p>This provider can only be accessed from the main thread.
 */
@RootViewPickerScope
public final class RootViewPicker implements Provider<View> {
  private static final String TAG = RootViewPicker.class.getSimpleName();

  private static final List<Integer> CREATED_WAIT_TIMES = CollectionsKt.listOf(10, 50, 150, 250);
  private static final List<Integer> RESUMED_WAIT_TIMES =
      CollectionsKt.listOf(10, 50, 100, 500, 2000 /* 2sec */, 30000 /* 30sec */);

  private final UiController uiController;
  private final ActivityLifecycleMonitor activityLifecycleMonitor;
  private final AtomicReference<Boolean> needsActivity;
  private final RootResultFetcher rootResultFetcher;
  private final ControlledLooper controlledLooper;
  private final Context appContext;

  @Inject
  RootViewPicker(
      UiController uiController,
      RootResultFetcher rootResultFetcher,
      ActivityLifecycleMonitor activityLifecycleMonitor,
      AtomicReference<Boolean> needsActivity,
      ControlledLooper controlledLooper,
      @TargetContext Context appContext) {
    this.uiController = uiController;
    this.rootResultFetcher = rootResultFetcher;
    this.activityLifecycleMonitor = activityLifecycleMonitor;
    this.needsActivity = needsActivity;
    this.controlledLooper = controlledLooper;
    this.appContext = appContext;
  }

  @Override
  public View get() {
    checkState(Looper.getMainLooper().equals(Looper.myLooper()), "must be called on main thread.");

    // TODO(b/34663420): Move Activity waiting logic outside of this class. Not the responsibility
    // of RVP.
    if (needsActivity.get()) {
      waitForAtLeastOneActivityToBeResumed();
    }

    return pickRootView();
  }

  /**
   * Waits for a root to be ready. Ready here means the UI is no longer in flux if layout of the
   * root view is not being requested and the root view has window focus or is focusable.
   */
  private Root waitForRootToBeReady(Root pickedRoot) {
    long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10) /* 10 seconds */;
    BackOff rootReadyBackoff = new RootReadyBackoff();
    while (System.currentTimeMillis() <= timeout) {
      if (pickedRoot.isReady()) {
        return pickedRoot;
      } else {
        controlledLooper.simulateWindowFocus(pickedRoot.getDecorView());
        uiController.loopMainThreadForAtLeast(rootReadyBackoff.getNextBackoffInMillis());
      }
    }

    throw new RootViewWithoutFocusException(
        String.format(
            Locale.ROOT,
            "Waited for the root of the view hierarchy to have "
                + "window focus and not request layout for 10 seconds. If you specified a non "
                + "default root matcher, it may be picking a root that never takes focus. "
                + "Root:\n%s",
            pickedRoot));
  }

  private Root pickARoot() {
    long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60) /* 60 seconds */;
    RootResults rootResults = rootResultFetcher.fetch();
    BackOff noActiveRootsBackoff = new NoActiveRootsBackoff();
    BackOff noMatchingRootBackoff = new NoMatchingRootBackoff();
    while (System.currentTimeMillis() <= timeout) {
      switch (rootResults.getState()) {
        case ROOTS_PICKED:
          return rootResults.getPickedRoot();
        case NO_ROOTS_PRESENT:
          // no active roots yet, but should appear soon.
          uiController.loopMainThreadForAtLeast(noActiveRootsBackoff.getNextBackoffInMillis());
          break;
        case NO_ROOTS_PICKED:
          // a root which satisfies the matcher should show up eventually.
          uiController.loopMainThreadForAtLeast(noMatchingRootBackoff.getNextBackoffInMillis());
          break;
      }
      rootResults = rootResultFetcher.fetch();
    }

    if (RootResults.State.ROOTS_PICKED == rootResults.getState()) {
      return rootResults.getPickedRoot();
    }

    throw NoMatchingRootException.create(rootResults.rootSelector, rootResults.allRoots);
  }

  private View pickRootView() {
    return waitForRootToBeReady(pickARoot()).getDecorView();
  }

  private void waitForAtLeastOneActivityToBeResumed() {
    Collection<Activity> resumedActivities =
        activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      uiController.loopMainThreadUntilIdle();
      resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    }
    if (resumedActivities.isEmpty()) {
      List<Activity> activities = getAllActiveActivities();
      if (activities.isEmpty()) {
        for (long waitTime : CREATED_WAIT_TIMES) {
          // wait for Activities to be scheduled by the platform before assuming there are none
          // and failing the test.
          Log.w(TAG, "No activities found - waiting: " + waitTime + "ms for one to appear.");
          uiController.loopMainThreadForAtLeast(waitTime);
          activities = getAllActiveActivities();
          if (!activities.isEmpty()) {
            // found at least one activity in the pipeline
            break;
          }
        }
      }
      if (activities.isEmpty()) {
        throw new NoActivityResumedException(
            "No activities found. Did you forget to launch the activity "
                + "by calling getActivity() or startActivitySync or similar?");
      }
      // well at least there are some activities in the pipeline - lets see if they resume.

      for (long waitTime : RESUMED_WAIT_TIMES) {
        Log.w(
            TAG, "No activity currently resumed - waiting: " + waitTime + "ms for one to appear.");
        uiController.loopMainThreadForAtLeast(waitTime);
        resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
        if (!resumedActivities.isEmpty()) {
          return; // one of the pending activities has resumed
        }
      }
      throw new NoActivityResumedException(
          "No activities in stage RESUMED. Did you forget to "
              + "launch the activity. (test.getActivity() or similar)?");
    }

    // Wait for configuration changes to finish on the activity if there are any in progress.
    Activity currentActivity = (Activity) resumedActivities.toArray()[0];
    ConfigurationSynchronizationUtils.waitForConfigurationChangesOnActivity(
        currentActivity, uiController, appContext);
  }

  /** Returns the list of all non-destroyed activities. */
  private List<Activity> getAllActiveActivities() {
    List<Activity> activities = mutableListOf();
    for (Stage s : EnumSet.range(Stage.PRE_ON_CREATE, Stage.RESTARTED)) {
      activities.addAll(activityLifecycleMonitor.getActivitiesInStage(s));
    }
    return activities;
  }

  private static class RootResults {
    private final List<Root> allRoots;
    private final List<Root> pickedRoots;
    private final Matcher<Root> rootSelector;

    private RootResults(List<Root> allRoots, List<Root> pickedRoots, Matcher<Root> rootSelector) {
      this.allRoots = allRoots;
      this.pickedRoots = pickedRoots;
      this.rootSelector = rootSelector;
    }

    private static boolean isTopmostRoot(Root topMostRoot, Root root) {
      return root.getWindowLayoutParams().get().type
          > topMostRoot.getWindowLayoutParams().get().type;
    }

    public State getState() {
      if (allRoots.isEmpty()) {
        return State.NO_ROOTS_PRESENT;
      }
      if (pickedRoots.isEmpty()) {
        return State.NO_ROOTS_PICKED;
      }
      if (pickedRoots.size() >= 1) {
        return State.ROOTS_PICKED;
      }
      return State.NO_ROOTS_PICKED;
    }

    /**
     * If there are multiple roots, pick one root window to interact with. By default we try to
     * select the top most window, except for dialogs were we return the dialog window.
     *
     * <p>Multiple roots only occur:
     *
     * <ul>
     *   <li>When multiple activities are in some state of their lifecycle in the application. We
     *       don't care about this, since we only want to interact with the RESUMED activity, all
     *       other Activities windows are not visible to the user so, out of scope.
     *   <li>When a {@link android.widget.PopupWindow} or {@link
     *       android.support.v7.widget.PopupMenu} is used. This is a case where we definitely want
     *       to consider the top most window, since it probably has the most useful info in it.
     *   <li>When an {@link android.app.Dialog} is shown. Again, this is getting all the users
     *       attention, so it gets the test attention too.
     * </ul>
     */
    private Root getRootFromMultipleRoots() {
      Root topMostRoot = pickedRoots.get(0);
      if (pickedRoots.size() >= 1) {
        for (Root currentRoot : pickedRoots) {
          if (isDialog().matches(currentRoot)) {
            return currentRoot;
          }
          if (isTopmostRoot(topMostRoot, currentRoot)) {
            topMostRoot = currentRoot;
          }
        }
      }
      return topMostRoot;
    }

    public Root getPickedRoot() {
      if (pickedRoots.size() > 1) {
        LogUtil.logDebugWithProcess(TAG, "Multiple root windows detected: %s", pickedRoots);
        return getRootFromMultipleRoots();
      }
      return pickedRoots.get(0);
    }

    enum State {
      NO_ROOTS_PRESENT,
      NO_ROOTS_PICKED,
      ROOTS_PICKED,
    }
  }

  static class RootResultFetcher {
    private final Matcher<Root> selector;
    private final ActiveRootLister activeRootLister;

    @Inject
    public RootResultFetcher(
        ActiveRootLister activeRootLister, AtomicReference<Matcher<Root>> rootMatcherRef) {
      this.activeRootLister = activeRootLister;
      this.selector = rootMatcherRef.get();
    }

    public RootResults fetch() {
      List<Root> allRoots = activeRootLister.listActiveRoots();
      List<Root> pickedRoots = mutableListOf();

      for (Root root : allRoots) {
        if (selector.matches(root)) {
          pickedRoots.add(root);
        }
      }
      return new RootResults(allRoots, pickedRoots, selector);
    }
  }

  private abstract static class BackOff {
    private final List<Integer> backoffTimes;
    private final TimeUnit timeUnit;
    private int numberOfAttempts = 0;

    public BackOff(List<Integer> backoffTimes, TimeUnit timeUnit) {
      this.backoffTimes = backoffTimes;
      this.timeUnit = timeUnit;
    }

    protected abstract long getNextBackoffInMillis();

    protected final long getBackoffForAttempt() {
      if (numberOfAttempts >= backoffTimes.size()) {
        return backoffTimes.get(backoffTimes.size() - 1 /* don't further increase backoff */);
      }
      int backoffTime = backoffTimes.get(numberOfAttempts);
      numberOfAttempts++;
      return timeUnit.toMillis(backoffTime);
    }
  }

  private static final class NoActiveRootsBackoff extends BackOff {
    private static final List<Integer> NO_ACTIVE_ROOTS_BACKOFF =
        CollectionsKt.listOf(10, 10, 20, 30, 50, 80, 130, 210, 340);

    public NoActiveRootsBackoff() {
      super(NO_ACTIVE_ROOTS_BACKOFF, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getNextBackoffInMillis() {
      long waitTime = getBackoffForAttempt();
      LogUtil.logDebugWithProcess(
          TAG, "No active roots available - waiting: %sms for one to appear.", waitTime);
      return waitTime;
    }
  }

  private static final class NoMatchingRootBackoff extends BackOff {
    private static final List<Integer> NO_MATCHING_ROOT_BACKOFF =
        CollectionsKt.listOf(10, 20, 200, 400, 1000, 2000 /* 2sec */);

    public NoMatchingRootBackoff() {
      super(NO_MATCHING_ROOT_BACKOFF, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getNextBackoffInMillis() {
      long waitTime = getBackoffForAttempt();
      Log.d(
          TAG,
          String.format(
              Locale.ROOT,
              "No matching root available - waiting: %sms for one to appear.",
              waitTime));
      return waitTime;
    }
  }

  private static final class RootReadyBackoff extends BackOff {
    private static final List<Integer> ROOT_READY_BACKOFF =
        CollectionsKt.listOf(10, 25, 50, 100, 200, 400, 800, 1000 /* 1sec */);

    public RootReadyBackoff() {
      super(ROOT_READY_BACKOFF, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getNextBackoffInMillis() {
      long waitTime = getBackoffForAttempt();
      Log.d(
          TAG,
          String.format(
              Locale.ROOT, "Root not ready - waiting: %sms for one to appear.", waitTime));
      return waitTime;
    }
  }

  private static final class RootViewWithoutFocusException extends RuntimeException
      implements EspressoException {

    private RootViewWithoutFocusException(String message) {
      super(message);
    }
  }
}
