/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.core.app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.internal.platform.app.ActivityLifecycleTimeout;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * On-device {@link ActivityInvoker} implementation that drives Activity lifecycles using {@link
 * android.app.ActivityManager} indirectly via {@link Context#startActivity} and {@link
 * Activity#recreate}.
 *
 * <p>Some methods in this class are non-blocking API. It's caller's responsibility to wait for
 * activity state to be desired state.
 */
class InstrumentationActivityInvoker implements ActivityInvoker {
  /** A bundle key to retrieve an intent to start test target activity in extras bundle. */
  private static final String TARGET_ACTIVITY_INTENT_KEY =
      "androidx.test.core.app.InstrumentationActivityInvoker.START_TARGET_ACTIVITY_INTENT_KEY";

  /** A bundle key to retrieve an options bundle to start test target activity in extras bundle. */
  private static final String TARGET_ACTIVITY_OPTIONS_BUNDLE_KEY =
      "androidx.test.core.app.InstrumentationActivityInvoker.TARGET_ACTIVITY_OPTIONS_BUNDLE_KEY";

  /**
   * An intent action broadcasted by {@link BootstrapActivity} notifying the activity receives
   * activity result and passes payload back to the instrumentation process.
   */
  private static final String BOOTSTRAP_ACTIVITY_RESULT_RECEIVED =
      "androidx.test.core.app.InstrumentationActivityInvoker.BOOTSTRAP_ACTIVITY_RESULT_RECEIVED";

  /**
   * A bundle key to retrieve an activity result code from the extras bundle of {@link
   * #BOOTSTRAP_ACTIVITY_RESULT_RECEIVED} action.
   */
  private static final String BOOTSTRAP_ACTIVITY_RESULT_CODE_KEY =
      "androidx.test.core.app.InstrumentationActivityInvoker.BOOTSTRAP_ACTIVITY_RESULT_CODE_KEY";

  /**
   * A bundle key to retrieve an activity result data intent from the extras bundle of {@link
   * #BOOTSTRAP_ACTIVITY_RESULT_RECEIVED} action.
   */
  private static final String BOOTSTRAP_ACTIVITY_RESULT_DATA_KEY =
      "androidx.test.core.app.InstrumentationActivityInvoker.BOOTSTRAP_ACTIVITY_RESULT_DATA_KEY";

  /**
   * An intent action broadcasted by InstrumentActivityInvoker to clean up any {@link
   * ActivityResultWaiter}s that are still registered at the end
   */
  private static final String CANCEL_ACTIVITY_RESULT_WAITER =
      "androidx.test.core.app.InstrumentationActivityInvoker.CANCEL_ACTIVITY_RESULT_WAITER";

  /**
   * An intent action broadcasted by {@link EmptyActivity} notifying the activity becomes resumed
   * state.
   */
  private static final String EMPTY_ACTIVITY_RESUMED =
      "androidx.test.core.app.InstrumentationActivityInvoker.EMPTY_ACTIVITY_RESUMED";

  /**
   * An intent action broadcasted by {@link EmptyFloatingActivity} notifying the activity becomes
   * resumed state.
   */
  private static final String EMPTY_FLOATING_ACTIVITY_RESUMED =
      "androidx.test.core.app.InstrumentationActivityInvoker.EMPTY_FLOATING_ACTIVITY_RESUMED";

  /** An intent action to notify {@link BootstrapActivity} to be finished. */
  private static final String FINISH_BOOTSTRAP_ACTIVITY =
      "androidx.test.core.app.InstrumentationActivityInvoker.FINISH_BOOTSTRAP_ACTIVITY";

  /**
   * An intent action to notify {@link EmptyActivity} and {@link EmptyFloatingActivity} to be
   * finished.
   */
  private static final String FINISH_EMPTY_ACTIVITIES =
      "androidx.test.core.app.InstrumentationActivityInvoker.FINISH_EMPTY_ACTIVITIES";

  /**
   * BootstrapActivity starts a test target activity specified by the extras bundle with key {@link
   * #TARGET_ACTIVITY_INTENT_KEY} in the intent that starts this bootstrap activity. The target
   * activity is started by {@link Activity#startActivityForResult} when the bootstrap activity is
   * created. Upon an arrival of the activity result, the bootstrap activity forwards the result to
   * the instrumentation process by broadcasting the result and finishes itself. This activity also
   * finishes itself when it receives {@link #FINISH_BOOTSTRAP_ACTIVITY} action.
   */
  public static class BootstrapActivity extends Activity {
    private static final String TAG = BootstrapActivity.class.getName();
    private static final String IS_TARGET_ACTIVITY_STARTED_KEY = "IS_TARGET_ACTIVITY_STARTED_KEY";
    private final BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            finishActivity(/*requestCode=*/ 0);
            finish();
          }
        };

    private boolean isTargetActivityStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      registerReceiver(receiver, new IntentFilter(FINISH_BOOTSTRAP_ACTIVITY));

      isTargetActivityStarted =
          (savedInstanceState != null
              && savedInstanceState.getBoolean(IS_TARGET_ACTIVITY_STARTED_KEY, false));

      // disable starting animations
      overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
      super.finish();
      // disable closing animations
      overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
      super.onResume();

      if (!isTargetActivityStarted) {
        isTargetActivityStarted = true;
        PendingIntent startTargetActivityIntent =
            checkNotNull(getIntent().getParcelableExtra(TARGET_ACTIVITY_INTENT_KEY));
        Bundle options = getIntent().getBundleExtra(TARGET_ACTIVITY_OPTIONS_BUNDLE_KEY);
        try {
          if (options == null || Build.VERSION.SDK_INT < 16) {
            // Override and disable FLAG_ACTIVITY_NEW_TASK flag by flagsMask and flagsValue.
            // PendingIntentRecord#sendInner() will mask the original intent flag with the flagsMask
            // then override those bits with the new flagsValue specified here. This override is
            // necessary because if the activity is started as a new task ActivityStarter disposes
            // the originator information and the result is never be delivered. Instead you will get
            // an error "Activity is launching as a new task, so cancelling activity result." and
            // #onActivityResult() will be invoked immediately with result code
            // Activity#RESULT_CANCELED.
            startIntentSenderForResult(
                startTargetActivityIntent.getIntentSender(),
                /*requestCode=*/ 0,
                /*fillInIntent=*/ null,
                /*flagsMask=*/ Intent.FLAG_ACTIVITY_NEW_TASK,
                /*flagsValues=*/ 0,
                /*extraFlags=*/ 0);
          } else {
            startIntentSenderForResult(
                startTargetActivityIntent.getIntentSender(),
                /*requestCode=*/ 0,
                /*fillInIntent=*/ null,
                /*flagsMask=*/ Intent.FLAG_ACTIVITY_NEW_TASK,
                /*flagsValues=*/ 0,
                /*extraFlags=*/ 0,
                options);
          }
        } catch (IntentSender.SendIntentException e) {
          Log.e(TAG, "Failed to start target activity.", e);
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(IS_TARGET_ACTIVITY_STARTED_KEY, isTargetActivityStarted);
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      if (requestCode == 0) {
        Intent activityResultReceivedActionIntent = new Intent(BOOTSTRAP_ACTIVITY_RESULT_RECEIVED);
        activityResultReceivedActionIntent.putExtra(BOOTSTRAP_ACTIVITY_RESULT_CODE_KEY, resultCode);
        if (data != null) {
          activityResultReceivedActionIntent.putExtra(BOOTSTRAP_ACTIVITY_RESULT_DATA_KEY, data);
        }
        sendBroadcast(activityResultReceivedActionIntent);
        finish();
      }
    }
  }

  /**
   * ActivityResultWaiter listens broadcast messages and waits for {@link
   * #BOOTSTRAP_ACTIVITY_RESULT_RECEIVED} action. Upon the reception of that action, it retrieves
   * result code and data from the action and makes a local copy. Clients can access to the result
   * by {@link #getActivityResult()}.
   */
  private static class ActivityResultWaiter {

    private static final String TAG = ActivityResultWaiter.class.getName();
    private final CountDownLatch latch = new CountDownLatch(1);
    @Nullable private ActivityResult activityResult;

    /**
     * Constructs ActivityResultWaiter and starts listening to broadcast with the given context. It
     * keeps subscribing the event until it receives {@link #BOOTSTRAP_ACTIVITY_RESULT_RECEIVED}
     * action.
     */
    public ActivityResultWaiter(Context context) {
      BroadcastReceiver receiver =
          new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              // Stop listening to the broadcast once we get the result.
              context.unregisterReceiver(this);

              if (BOOTSTRAP_ACTIVITY_RESULT_RECEIVED.equals(intent.getAction())) {
                int resultCode =
                    intent.getIntExtra(
                        BOOTSTRAP_ACTIVITY_RESULT_CODE_KEY, Activity.RESULT_CANCELED);
                Intent resultData = intent.getParcelableExtra(BOOTSTRAP_ACTIVITY_RESULT_DATA_KEY);
                if (resultData != null) {
                  // Make a copy of resultData since the lifetime of the given intent is unknown.
                  resultData = new Intent(resultData);
                }
                activityResult = new ActivityResult(resultCode, resultData);
                latch.countDown();
              }
            }
          };
      IntentFilter intentFilter = new IntentFilter(BOOTSTRAP_ACTIVITY_RESULT_RECEIVED);
      intentFilter.addAction(CANCEL_ACTIVITY_RESULT_WAITER);
      context.registerReceiver(receiver, intentFilter);
    }

    /**
     * Waits for the activity result to be available until the timeout and returns the result.
     *
     * @throws NullPointerException if the result doesn't become available after the timeout
     * @return activity result of which {@link #startActivity} starts
     */
    public ActivityResult getActivityResult() {
      try {
        latch.await(ActivityLifecycleTimeout.getMillis(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Log.i(TAG, "Waiting activity result was interrupted", e);
      }
      checkNotNull(
          activityResult,
          "onActivityResult never be called after %d milliseconds",
          ActivityLifecycleTimeout.getMillis());
      return activityResult;
    }
  }

  /**
   * An empty activity with style "android:windowIsFloating = false". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>When this activity is resumed, it broadcasts {@link #EMPTY_ACTIVITY_RESUMED} action to
   * notify the state.
   *
   * <p>This activity finishes itself when it receives {@link #FINISH_EMPTY_ACTIVITIES} action.
   *
   * <p>This activity is used to send an arbitrary resumed Activity to stopped.
   */
  public static class EmptyActivity extends Activity {
    private final BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            finish();
          }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      registerReceiver(receiver, new IntentFilter(FINISH_EMPTY_ACTIVITIES));

      // disable starting animations
      overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
      super.finish();
      // disable closing animations
      overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
      super.onResume();
      sendBroadcast(new Intent(EMPTY_ACTIVITY_RESUMED));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);
    }
  }

  /**
   * An empty activity with style "android:windowIsFloating = true". The style is set by
   * AndroidManifest.xml via "android:theme".
   *
   * <p>When this activity is resumed, it broadcasts {@link #EMPTY_FLOATING_ACTIVITY_RESUMED} action
   * to notify the state.
   *
   * <p>This activity finishes itself when it receives {@link #FINISH_EMPTY_ACTIVITIES} action.
   *
   * <p>This activity is used to send an arbitrary resumed Activity to paused.
   */
  public static class EmptyFloatingActivity extends Activity {
    private final BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            finish();
          }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      registerReceiver(receiver, new IntentFilter(FINISH_EMPTY_ACTIVITIES));

      // disable starting animations
      overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
      super.finish();
      // disable closing animations
      overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
      super.onResume();
      sendBroadcast(new Intent(EMPTY_FLOATING_ACTIVITY_RESUMED));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);
    }
  }

  /** A waiter to observe activity result that is started by {@link #startActivity}. */
  @Nullable private ActivityResultWaiter activityResultWaiter;

  /** Starts an Activity using the given intent. */
  @Override
  public void startActivity(Intent intent, @Nullable Bundle activityOptions) {
    // make sure the intent can resolve an activity
    ActivityInfo ai = intent.resolveActivityInfo(getApplicationContext().getPackageManager(), 0);
    if (ai == null) {
      throw new RuntimeException("Unable to resolve activity for: " + intent);
    }
    // Close empty activities and bootstrap activity if it's running. This might happen if the
    // previous test crashes before it cleans up the state.
    getApplicationContext().sendBroadcast(new Intent(FINISH_BOOTSTRAP_ACTIVITY));
    getApplicationContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));

    activityResultWaiter = new ActivityResultWaiter(getApplicationContext());

    // Note: Instrumentation.startActivitySync(Intent) cannot be used here because BootstrapActivity
    // may start in different process. Also, we use PendingIntent because the target activity may
    // set "exported" attribute to false so that it prohibits starting the activity outside of their
    // package. With PendingIntent we delegate the authority to BootstrapActivity.
    Intent bootstrapIntent =
        getIntentForActivity(BootstrapActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(
                TARGET_ACTIVITY_INTENT_KEY,
                PendingIntent.getActivity(
                    getApplicationContext(),
                    /*requestCode=*/ 0,
                    intent,
                    /*flags=*/ PendingIntent.FLAG_UPDATE_CURRENT))
            .putExtra(TARGET_ACTIVITY_OPTIONS_BUNDLE_KEY, activityOptions);

    if (Build.VERSION.SDK_INT < 16) {
      // activityOptions not supported
      getApplicationContext().startActivity(bootstrapIntent);
    } else {
      getApplicationContext().startActivity(bootstrapIntent, activityOptions);
    }
  }

  @Override
  public void startActivity(Intent intent) {
    startActivity(intent, null);
  }

  @Override
  public ActivityResult getActivityResult() {
    return checkNotNull(activityResultWaiter, "You must start Activity first").getActivityResult();
  }

  /** Resumes the tested activity by finishing empty activities. */
  @Override
  public void resumeActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);
    getApplicationContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));
  }

  /**
   * Pauses the tested activity by starting {@link EmptyFloatingActivity} on top of the tested
   * activity.
   */
  @Override
  public void pauseActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED);
    startFloatingEmptyActivitySync();
  }

  private void startFloatingEmptyActivitySync() {
    CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            latch.countDown();
          }
        };
    getApplicationContext()
        .registerReceiver(receiver, new IntentFilter(EMPTY_FLOATING_ACTIVITY_RESUMED));

    // Starting an arbitrary Activity (android:windowIsFloating = true) forces the tested Activity
    // to the paused state.
    getApplicationContext()
        .startActivity(
            getIntentForActivity(EmptyFloatingActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    try {
      latch.await(ActivityLifecycleTimeout.getMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new AssertionError("Failed to pause activity", e);
    } finally {
      getApplicationContext().unregisterReceiver(receiver);
    }
  }

  /** Stops the tested activity by starting {@link EmptyActivity} on top of the tested activity. */
  @Override
  public void stopActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);
    startEmptyActivitySync();
  }

  private void startEmptyActivitySync() {
    CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            latch.countDown();
          }
        };
    getApplicationContext().registerReceiver(receiver, new IntentFilter(EMPTY_ACTIVITY_RESUMED));

    // Starting an arbitrary Activity (android:windowIsFloating = false) forces the tested Activity
    // to the stopped state.
    getApplicationContext()
        .startActivity(
            getIntentForActivity(EmptyActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    try {
      latch.await(ActivityLifecycleTimeout.getMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new AssertionError("Failed to stop activity", e);
    } finally {
      getApplicationContext().unregisterReceiver(receiver);
    }
  }

  /**
   * Recreates the Activity by {@link Activity#recreate}.
   *
   * <p>Note that {@link Activity#recreate}'s behavior differs by Android framework version. For
   * example, the version P brings Activity's lifecycle state to the original state after the
   * re-creation. A stopped Activity goes to stopped state after the re-creation in concrete.
   * Whereas the version O ignores {@link Activity#recreate} method call when the activity is in
   * stopped state. The version N re-creates stopped Activity but brings back to paused state
   * instead of stopped.
   *
   * <p>In short, make sure to set Activity's state to resumed before calling this method otherwise
   * the behavior is the framework version dependent.
   */
  @Override
  public void recreateActivity(Activity activity) {
    checkActivityStageIsIn(activity, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED);
    getInstrumentation().runOnMainSync(activity::recreate);
  }

  @Override
  public void finishActivity(Activity activity) {
    // Stop the activity before calling Activity#finish() as a workaround for the framework bug in
    // API level 15 to 19 where the framework may not call #onStop and #onDestroy if you call
    // Activity#finish() while it is resumed. The exact root cause is unknown but moving the
    // activity back-and-forth between foreground and background helps the finish operation to be
    // executed so here we try finishing the activity by several means. This hack is not necessary
    // for the API level above 19.
    startEmptyActivitySync();
    getInstrumentation().runOnMainSync(activity::finish);
    getApplicationContext().sendBroadcast(new Intent(FINISH_BOOTSTRAP_ACTIVITY));
    startEmptyActivitySync();
    getInstrumentation().runOnMainSync(activity::finish);
    getApplicationContext().sendBroadcast(new Intent(FINISH_EMPTY_ACTIVITIES));
    getApplicationContext().sendBroadcast(new Intent(CANCEL_ACTIVITY_RESULT_WAITER));
  }

  private static void checkActivityStageIsIn(Activity activity, Stage... expected) {
    checkActivityStageIsIn(activity, new HashSet<>(Arrays.asList(expected)));
  }

  private static void checkActivityStageIsIn(Activity activity, Set<Stage> expected) {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Stage stage =
                  ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
              checkState(
                  expected.contains(stage),
                  "Activity's stage must be %s but was %s",
                  expected,
                  stage);
            });
  }
}
