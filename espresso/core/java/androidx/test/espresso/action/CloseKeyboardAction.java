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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.Matchers.any;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.Collection;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Matcher;

/** Closes soft keyboard. */
public final class CloseKeyboardAction implements ViewAction {

  private static final int NUM_RETRIES = 3;
  private static final String TAG = CloseKeyboardAction.class.getSimpleName();

  @RemoteMsgConstructor
  public CloseKeyboardAction() {}

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return any(View.class);
  }

  @Override
  public void perform(UiController uiController, View view) {
    // Retry in case of timeout exception to avoid flakiness in IMM.
    for (int i = 0; i < NUM_RETRIES; i++) {
      try {
        tryToCloseKeyboard(view, uiController);
        return;
      } catch (TimeoutException te) {
        Log.w(TAG, "Caught timeout exception. Retrying.");
        if (i == 2) {
          throw new PerformException.Builder()
              .withActionDescription(this.getDescription())
              .withViewDescription(HumanReadables.describe(view))
              .withCause(te)
              .build();
        }
      }
    }
  }

  private void tryToCloseKeyboard(View view, UiController uiController) throws TimeoutException {
    InputMethodManager imm =
        (InputMethodManager)
            getRootActivity(uiController).getSystemService(Context.INPUT_METHOD_SERVICE);

    CloseKeyboardIdlingResult idlingResult =
        new CloseKeyboardIdlingResult(new Handler(Looper.getMainLooper()));

    IdlingRegistry.getInstance().register(idlingResult);

    try {

      if (!imm.hideSoftInputFromWindow(view.getWindowToken(), 0, idlingResult)) {
        Log.w(TAG, "Attempting to close soft keyboard, while it is not shown.");
        return;
      }
      // set 2 second timeout
      idlingResult.scheduleTimeout(2000);
      uiController.loopMainThreadUntilIdle();
      if (idlingResult.timedOut) {
        throw new TimeoutException("Wait on operation result timed out.");
      }
    } finally {
      IdlingRegistry.getInstance().unregister(idlingResult);
    }

    if (idlingResult.result != InputMethodManager.RESULT_UNCHANGED_HIDDEN
        && idlingResult.result != InputMethodManager.RESULT_HIDDEN) {
      String error =
          "Attempt to close the soft keyboard did not result in soft keyboard to be hidden."
              + " resultCode = "
              + idlingResult.result;
      Log.e(TAG, error);
      throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(new RuntimeException(error))
          .build();
    }
  }

  private static Activity getRootActivity(UiController uiController) {
    Collection<Activity> resumedActivities =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      uiController.loopMainThreadUntilIdle();
      resumedActivities =
          ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    }
    checkState(
        resumedActivities.size() == 1,
        "More than one activity is in RESUMED stage."
            + " There may have been an error during the activity creation/startup process,"
            + " please check your logs.");
    return getOnlyElement(resumedActivities);
  }

  @Override
  public String getDescription() {
    return "close keyboard";
  }

  /** {@link IdlingResource} to help Espresso synchronize keyboard closure animations */
  private static class CloseKeyboardIdlingResult extends ResultReceiver implements IdlingResource {

    // all set|read on main thread
    private ResourceCallback resourceCallback;
    // indicates that we've received a response from the inputmethod manager
    private boolean receivedResult = false;
    // the result IMM gave us (only valid if receivedResult is true)
    private int result = -1;
    // indicates we've timed out.
    private boolean timedOut = false;
    // the idle value we report to espresso.
    private boolean idle = false;
    private final Handler handler;

    private CloseKeyboardIdlingResult(Handler h) {
      super(h);
      handler = h;
    }

    private void scheduleTimeout(long millis) {
      handler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              if (!receivedResult) {
                timedOut = true;
                if (null != resourceCallback) {
                  resourceCallback.onTransitionToIdle();
                }
              }
            }
          },
          millis);
    }

    private void notifyEspresso(long millis) {
      checkState(this.receivedResult);
      handler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              idle = true;
              if (null != resourceCallback) {
                resourceCallback.onTransitionToIdle();
              }
            }
          },
          millis);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      result = resultCode;
      receivedResult = true;
      // IMM responds to us first, before the messages are sent to the app that makes it draw
      // over the region that was previously obscured by the keyboard. Therefore stay busy for a
      // short period of time to allow for the redraw message to be sent to our app and processed.
      // The 300 millis number was chosen by running more than 100,000 tests and making sure there
      // were no flakiness.
      notifyEspresso(300);
    }

    @Override
    public String getName() {
      return "CloseKeyboardIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
      // Either IMM has responded to us or we've given up.
      return idle || timedOut;
    }

    /**
     * handle the race where IMM responds before espresso even gives us our callback. TimedOut being
     * true would be a big WTF, but we're idle too in that case.
     */
    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      resourceCallback = callback;
    }
  }
}
