/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.rule;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.UiThreadTest;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import androidx.test.runner.MonitoringInstrumentation;
import androidx.test.runner.intercepting.SingleActivityFactory;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This rule provides functional testing of a single {@link Activity}. When {@code launchActivity}
 * is set to true in the constructor, the Activity under test will be launched before each test
 * annotated with <a href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code>
 * </a> and before methods annotated with <a
 * href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>, and it
 * will be terminated after the test is completed and methods annotated with <a
 * href="http://junit.source.net/javadoc/org/junit/After.html"><code>After
 * </code></a> are finished.
 *
 * <p>The Activity can be manually launched with {@link #launchActivity(Intent)}, and manually
 * finished with {@link #finishActivity()}. If the Activity is running at the end of the test, the
 * test rule will finish it.
 *
 * <p>During the duration of the test you will be able to manipulate your Activity directly using
 * the reference obtained from {@link #getActivity()}. If the Activity is finished and relaunched,
 * the reference returned by {@link #getActivity()} will always point to the current instance of the
 * Activity.
 *
 * @param <T> The Activity class under test
 */
public class ActivityTestRule<T extends Activity> implements TestRule {

  private static final String TAG = "ActivityTestRule";

  private static final int NO_FLAGS_SET = 0;
  private static final String FIELD_RESULT_CODE = "mResultCode";
  private static final String FIELD_RESULT_DATA = "mResultData";

  private final Class<T> mActivityClass;

  private final String mTargetPackage;

  private final int mLaunchFlags;

  private final ActivityLifecycleCallback mLifecycleCallback = new LifecycleCallback();

  private Instrumentation mInstrumentation;

  private boolean mInitialTouchMode = false;

  private boolean mLaunchActivity = false;

  private SingleActivityFactory<T> mActivityFactory;

  @VisibleForTesting volatile WeakReference<T> mActivity = makeWeakReference(null);

  private volatile ActivityResult mActivityResult;

  /**
   * Similar to {@link #ActivityTestRule(Class, boolean)} but with "touch mode" disabled.
   *
   * @param activityClass The activity under test. This must be a class in the instrumentation
   *     targetPackage specified in the AndroidManifest.xml
   * @see ActivityTestRule#ActivityTestRule(Class, boolean, boolean)
   */
  public ActivityTestRule(Class<T> activityClass) {
    this(activityClass, false);
  }

  /**
   * Similar to {@link #ActivityTestRule(Class, boolean, boolean)} but defaults to launch the
   * activity under test once per <a href="http://junit.org/javadoc/latest/org/junit/Test.html">
   * <code>Test</code></a> method. It is launched before the first <a
   * href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>
   * method, and terminated after the last <a
   * href="http://junit.source.net/javadoc/org/junit/After.html"><code>After</code></a> method.
   *
   * @param activityClass The activity under test. This must be a class in the instrumentation
   *     targetPackage specified in the AndroidManifest.xml
   * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
   * @see ActivityTestRule#ActivityTestRule(Class, boolean, boolean)
   */
  public ActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
    this(activityClass, initialTouchMode, true);
  }

  /**
   * Similar to {@link #ActivityTestRule(Class, String, int, boolean, boolean)} but defaults to
   * launch the Activity with the default target package name {@link
   * InstrumentationRegistry#getTargetContext()#getPackageName} and {@link
   * Intent#FLAG_ACTIVITY_NEW_TASK} launch flag.
   *
   * @param activityClass The activity under test. This must be a class in the instrumentation
   *     targetPackage specified in the AndroidManifest.xml
   * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
   * @param launchActivity true if the Activity should be launched once per <a
   *     href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code></a> method. It
   *     will be launched before the first <a
   *     href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>
   *     method, and terminated after the last <a
   *     href="http://junit.source.net/javadoc/org/junit/After.html"><code>After</code></a>
   *     method.
   */
  public ActivityTestRule(
      Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
    this(
        activityClass,
        InstrumentationRegistry.getTargetContext().getPackageName(),
        Intent.FLAG_ACTIVITY_NEW_TASK,
        initialTouchMode,
        launchActivity);
  }

  /**
   * Creates an {@link ActivityTestRule} for the Activity under test.
   *
   * @param activityFactory factory to be used for creating Activity instance
   * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
   * @param launchActivity true if the Activity should be launched once per <a
   *     href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code></a> method. It
   *     will be launched before the first <a
   *     href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>
   *     method, and terminated after the last <a
   *     href="http://junit.source.net/javadoc/org/junit/After.html"><code>After</code></a>
   *     method.
   */
  public ActivityTestRule(
      SingleActivityFactory<T> activityFactory, boolean initialTouchMode, boolean launchActivity) {
    this(activityFactory.getActivityClassToIntercept(), initialTouchMode, launchActivity);
    mActivityFactory = activityFactory;
  }

  /**
   * Creates an {@link ActivityTestRule} for the Activity under test.
   *
   * @param activityClass The activity under test. This must be a class in the instrumentation
   *     targetPackage specified in the AndroidManifest.xml
   * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
   * @param launchActivity true if the Activity should be launched once per <a
   *     href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code></a> method. It
   *     will be launched before the first <a
   *     href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>
   *     method, and terminated after the last <a
   *     href="http://junit.source.net/javadoc/org/junit/After.html"><code>After</code></a>
   *     method.
   * @param targetPackage The name of the target package that the Activity is started under. This
   *     value is passed down to the start Intent using {@link
   *     Intent#setClassName(android.content.Context, String)}. Can not be null.
   * @param launchFlags launch flags to start the Activity under test.
   */
  public ActivityTestRule(
      Class<T> activityClass,
      @NonNull String targetPackage,
      int launchFlags,
      boolean initialTouchMode,
      boolean launchActivity) {
    mInstrumentation = InstrumentationRegistry.getInstrumentation();
    mActivityClass = activityClass;
    mTargetPackage = checkNotNull(targetPackage, "targetPackage cannot be null!");
    mLaunchFlags = launchFlags;
    mInitialTouchMode = initialTouchMode;
    mLaunchActivity = launchActivity;
  }

  /**
   * Override this method to set up a custom Intent as if supplied to {@link
   * android.content.Context#startActivity}. Custom Intents provided by this method will take
   * precedence over default Intents that where created in the constructor but be overridden by any
   * Intents passed in through {@link #launchActivity(Intent)}.
   *
   * <p>The default Intent (if this method returns null or is not overwritten) is: action = {@link
   * Intent#ACTION_MAIN} flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK} All other intent fields are
   * null or empty.
   *
   * <p>If the custom Intent provided by this methods overrides any of the following fields:
   *
   * <ul>
   *   <li>componentName through {@link Intent#setClassName(String, String)}
   *   <li>launch flags through {@link Intent#setFlags(int)}
   * </ul>
   *
   * <p>These custom values will be used to start the Activity. However, if some of these values are
   * not set, the default values documented in {@link #ActivityTestRule(Class, String, int, boolean,
   * boolean)} are supplemented.
   *
   * @return The Intent as if supplied to {@link android.content.Context#startActivity}.
   */
  protected Intent getActivityIntent() {
    return null;
  }

  /**
   * Override this method to execute any code that should run before your {@link Activity} is
   * created and launched. This method is called before each test method, including any method
   * annotated with <a href="http://junit.source.net/javadoc/org/junit/Before.html"><code>
   * Before</code></a>.
   */
  protected void beforeActivityLaunched() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run after your {@link Activity} is
   * launched, but before any test code is run including any method annotated with <a
   * href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
   *
   * <p>Prefer <a href="http://junit.source.net/javadoc/org/junit/Before.html"><code>Before
   * </code></a> over this method. This method should usually not be overwritten directly in tests
   * and only be used by subclasses of ActivityTestRule to get notified when the activity is created
   * and visible but test runs.
   */
  protected void afterActivityLaunched() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run after the currently launched {@link
   * Activity} is finished. This method is called after each test method, including any method
   * annotated with <a href="http://junit.source.net/javadoc/org/junit/After.html"><code>After
   * </code></a>.
   *
   * <p>Prefer <a href="http://junit.source.net/javadoc/org/junit/After.html"><code>Before
   * </code></a> over this method. This method should usually not be overwritten directly in tests
   * and only be used by subclasses of ActivityTestRule to get notified when the activity is created
   * and visible but test runs.
   */
  protected void afterActivityFinished() {
    // empty by default
  }

  /**
   * Returns the reference to the activity under test.
   *
   * <p>The reference to the activity is assigned during the initial creation of the acivity and for
   * every sinlge {@link Activity#OnResumed()} lifecycle change.
   *
   * <p><b>Note:</b> Lifecycle changes happen on the UI thread (not the instrumenation thread where
   * this test code usually executes). Thus, the return value may vary depending on timing.
   *
   * <p>For example, if the activity is finished and relaunched, the reference returned by this
   * method will point to the new instance of the activity assuming {@link Activity#OnResumed()} was
   * called prior to calling this method.
   *
   * <p>If the activity wasn't created yet or already finished, {@code null} will be returned.
   *
   * <p><b>Note:</b> The activity reference is stored in a weak reference which means if the
   * activity under test is detroyed (ex. back button was pressed) then the system no longer holds a
   * strong reference to the acitivty and this refernce may get garbage collected.
   */
  public T getActivity() {
    T hardActivityRef = mActivity.get();
    if (hardActivityRef == null) {
      Log.w(TAG, "Activity wasn't created yet or already stopped");
    }
    return hardActivityRef;
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    return new ActivityStatement(base);
  }

  /**
   * Launches the Activity under test.
   *
   * <p>Don't call this method directly, unless you explicitly requested not to lazily launch the
   * Activity manually using the launchActivity flag in {@link #ActivityTestRule(Class, boolean,
   * boolean)}.
   *
   * <p>Usage:
   *
   * <pre>
   *    &#064;Test
   *    public void customIntentToStartActivity() {
   *        Intent intent = new Intent(Intent.ACTION_PICK);
   *        mActivity = mActivityRule.launchActivity(intent);
   *    }
   * </pre>
   *
   * Note: Custom start Intents provided through this method will take precedence over default
   * Intents that where created in the constructor and any Intent returned from {@link
   * #getActivityIntent()}. The same override rules documented in {@link #getActivityIntent()}
   * apply.
   *
   * @param startIntent The Intent that will be used to start the Activity under test. If {@code
   *     startIntent} is null, the Intent returned by {@link ActivityTestRule#getActivityIntent()}
   *     is used.
   * @return the Activity launched by this rule.
   */
  public T launchActivity(@Nullable Intent startIntent) {
    // set initial touch mode
    mInstrumentation.setInTouchMode(mInitialTouchMode);

    // inject custom intent, if provided
    if (null == startIntent) {
      startIntent = getActivityIntent();
      if (null == startIntent) {
        Log.w(
            TAG,
            "getActivityIntent() returned null using default: " + "Intent(Intent.ACTION_MAIN)");
        startIntent = new Intent(Intent.ACTION_MAIN);
      }
    }

    // Set target component if not set Intent
    if (null == startIntent.getComponent()) {
      startIntent.setClassName(mTargetPackage, mActivityClass.getName());
    }

    // Set launch flags where if not set Intent
    if (NO_FLAGS_SET == startIntent.getFlags()) {
      startIntent.addFlags(mLaunchFlags);
    }

    beforeActivityLaunched();
    // The following cast is correct because the activity we're creating is of the same type as
    // the one passed in
    T hardActivityRef = mActivityClass.cast(mInstrumentation.startActivitySync(startIntent));
    mActivity = makeWeakReference(hardActivityRef);

    mInstrumentation.waitForIdleSync();

    if (hardActivityRef != null) {
      // Notify that Activity was successfully launched
      ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(mLifecycleCallback);
      afterActivityLaunched();
    } else {
      // Log an error message to logcat/instrumentation, that the Activity failed to launch
      String errorMessage =
          String.format("Activity %s, failed to launch", startIntent.getComponent());
      Bundle bundle = new Bundle();
      bundle.putString(Instrumentation.REPORT_KEY_STREAMRESULT, TAG + " " + errorMessage);
      mInstrumentation.sendStatus(0, bundle);
      Log.e(TAG, errorMessage);
    }

    return hardActivityRef;
  }

  @VisibleForTesting
  void setInstrumentation(Instrumentation instrumentation) {
    mInstrumentation = checkNotNull(instrumentation, "instrumentation cannot be null!");
  }

  /**
   * Finishes the currently launched Activity.
   *
   * @throws IllegalStateException if the Activity is not running or failed to finish it.
   */
  public void finishActivity() {
    try {
      if (mActivity.get() != null) {
        callFinishOnMainSync();
      }
    } finally {
      mActivity = makeWeakReference(null);
      afterActivityFinished(); // TODO(b/72327935) move down to evaluate
    }
  }

  @VisibleForTesting
  void callFinishOnMainSync() {
    try {
      final T hardActivityRef = mActivity.get();
      runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              checkState(
                  hardActivityRef != null,
                  "Activity was not launched. If you manually finished it, you must launch it"
                      + " again before finishing it. ");
              hardActivityRef.finish();
              // If there is an activity result we save it
              setActivityResultForActivity(hardActivityRef);
            }
          });
      mInstrumentation.waitForIdleSync();
    } catch (Throwable throwable) {
      // Should never happen
      String msg = "Failed to execute activity.finish() on the main thread";
      Log.e(TAG, msg, throwable);
      throw new IllegalStateException(msg, throwable);
    }
  }

  /**
   * This method can be used to retrieve the {@link ActivityResult} of an Activity that has called
   * {@link Activity#setResult}. Usually, the result is handled in {@link Activity#onActivityResult}
   * of the parent Activity, that has called {@link Activity#startActivityForResult}.
   *
   * <p>This method must <b>not</b> be called before {@code Activity.finish} was called or after the
   * activity was already destroyed.
   *
   * <p>Note: This method assumes {@link Activity#setResult(int)} is called no later than in {@link
   * Activity#onPause()}.
   *
   * @return the {@link ActivityResult} that was set most recently
   * @throws IllegalStateException if the activity is not in finishing state.
   */
  public ActivityResult getActivityResult() {
    if (null == mActivityResult) {
      // This is required if users manually called .finish() on their activity instead of using
      // this.finishActivity(). Since .finish() is async there could be a case that our callback
      // wasn't called just yet.
      T hardActivityRef = mActivity.get();
      checkNotNull(hardActivityRef, "Activity wasn't created yet or already destroyed!");
      try {
        runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                checkState(hardActivityRef.isFinishing(), "Activity is not finishing!");
                setActivityResultForActivity(hardActivityRef);
              }
            });
      } catch (Throwable throwable) {
        throw new IllegalStateException(throwable);
      }
    }
    return mActivityResult;
  }

  private void setActivityResultForActivity(final T activity) {
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Must be called on the main thread!");
    checkNotNull(activity, "Activity wasn't created yet or already destroyed!");

    try {
      Field resultCodeField = Activity.class.getDeclaredField(FIELD_RESULT_CODE);
      resultCodeField.setAccessible(true);

      Field resultDataField = Activity.class.getDeclaredField(FIELD_RESULT_DATA);
      resultDataField.setAccessible(true);

      mActivityResult =
          new ActivityResult(
              (int) resultCodeField.get(activity), (Intent) resultDataField.get(activity));
    } catch (NoSuchFieldException e) {
      String msg =
          "Looks like the Android Activity class has changed its"
              + "private fields for mResultCode or mResultData. "
              + "Time to update the reflection code.";
      Log.e(TAG, msg, e);
      throw new RuntimeException(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Field mResultCode or mResultData is not accessible";
      Log.e(TAG, msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /**
   * Helper method for running part of a method on the UI thread, blocking until it is complete.
   *
   * <p>Note: In most cases it is simpler to annotate the test method with {@link UiThreadTest}.
   *
   * <p>Use this method if you need to switch in and out of the UI thread within your method.
   *
   * @param runnable runnable containing test code in the {@link Runnable#run()} method
   * @see androidx.test.annotation.UiThreadTest
   */
  public void runOnUiThread(final Runnable runnable) throws Throwable {
    UiThreadStatement.runOnUiThread(runnable);
  }

  /**
   * <a href="http://junit.org/apidocs/org/junit/runners/model/Statement.html"><code>Statement
   * </code></a> that finishes the activity after the test was executed
   */
  private class ActivityStatement extends Statement {

    private final Statement mBase;

    public ActivityStatement(Statement base) {
      mBase = base;
    }

    @Override
    public void evaluate() throws Throwable {
      MonitoringInstrumentation instrumentation =
          ActivityTestRule.this.mInstrumentation instanceof MonitoringInstrumentation
              ? (MonitoringInstrumentation) ActivityTestRule.this.mInstrumentation
              : null;
      try {
        if (mActivityFactory != null && instrumentation != null) {
          instrumentation.interceptActivityUsing(mActivityFactory);
        }
        if (mLaunchActivity) {
          launchActivity(getActivityIntent());
        }
        mBase.evaluate();
      } finally {
        if (instrumentation != null) {
          instrumentation.useDefaultInterceptingActivityFactory();
        }

        T hardActivityRef = mActivity.get();
        if (hardActivityRef != null) {
          finishActivity();
        }
        mActivityResult = null;
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(mLifecycleCallback);
      }
    }
  }

  @VisibleForTesting
  WeakReference<T> makeWeakReference(T activity) {
    return new WeakReference<T>(activity);
  }

  /**
   * Activity lifecycle callback which ensures to release a reference on the activity under test
   * after lifecycle changes. This is done to ensure that we don't leak the original Activity under
   * test and at the same time have a reference to the currently visible activity.
   *
   * <p>Note: this callback is run on the main thread!
   */
  private class LifecycleCallback implements ActivityLifecycleCallback {
    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
      if (mActivityClass.isInstance(activity)) {
        if (Stage.RESUMED == stage) {
          mActivity = makeWeakReference(mActivityClass.cast(activity));
        } else if (Stage.PAUSED == stage) {
          // If there is an activity result we save it
          if (activity.isFinishing() && mActivityResult != null) {
            setActivityResultForActivity(mActivityClass.cast(activity));
          }
        }
      }
    }
  }
}
