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

import static android.app.Activity.RESULT_OK;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.core.app.ActivityScenario.ActivityAction;
import androidx.test.core.app.testing.AsyncRecordingActivity;
import androidx.test.core.app.testing.FinishItselfActivity;
import androidx.test.core.app.testing.IntentActivity;
import androidx.test.core.app.testing.RecordingActivity;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.core.app.testing.RedirectingActivity;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for ActivityScenario's implementation. Verifies ActivityScenario APIs works consistently
 * across multiple different Android framework versions and Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioTest {

  @Before
  public void setUp() {
    RecordingActivity.clearCallbacks();
  }

  @Test
  public void launchedActivityShouldBeResumed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void finishItselfActivityShouldBeLaunchable() throws Exception {
    try (ActivityScenario<FinishItselfActivity> scenario =
        ActivityScenario.launch(FinishItselfActivity.class)) {
      // FinishItselfActivity calls #finish in its onCreate method. This triggers specialized
      // lifecycle transition and onDestroy is invoked immediately after onCreate. (onStart and
      // onResume are not invoked at all).
      assertThat(scenario.getState()).isEqualTo(State.DESTROYED);
    }
  }

  @Test
  public void redirectingActivityShouldBeLaunchable() throws Exception {
    try (ActivityScenario<RedirectingActivity> scenario =
        ActivityScenario.launch(RedirectingActivity.class)) {
      // RedirectingActivity starts RecreationRecordingActivity in its onCreate method so the
      // state can be one of RESUMED, STARTED, CREATED, or DESTROYED based on the timing.
      assertThat(scenario.getState()).isAtMost(State.RESUMED);
    }
  }

  @Test
  public void launchWithCustomIntent() throws Exception {
    Intent startActivityIntent =
        new Intent(ApplicationProvider.getApplicationContext(), RecreationRecordingActivity.class)
            .putExtra("MyIntentParameterKey", "MyIntentParameterValue");
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(startActivityIntent)) {
      scenario.onActivity(
          activity ->
              assertThat(activity.getIntent())
                  .extras()
                  .string("MyIntentParameterKey")
                  .isEqualTo("MyIntentParameterValue"));
    }
  }

  @Test
  public void fromResumedToDestroyed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.DESTROYED);
      assertThat(scenario.getState()).isEqualTo(State.DESTROYED);
    }
  }

  @Test
  public void fromResumedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      assertThat(scenario.getState()).isEqualTo(State.CREATED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromResumedToStarted() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      assertThat(scenario.getState()).isEqualTo(State.STARTED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromResumedToResumed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.RESUMED);
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromCreatedToDestroyed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.moveToState(State.DESTROYED);
      assertThat(scenario.getState()).isEqualTo(State.DESTROYED);
    }
  }

  @Test
  public void fromCreatedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.moveToState(State.CREATED);
      assertThat(scenario.getState()).isEqualTo(State.CREATED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromCreatedToStarted() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.moveToState(State.STARTED);
      assertThat(scenario.getState()).isEqualTo(State.STARTED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromCreatedToResumed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.moveToState(State.RESUMED);
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromStartedToDestroyed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.moveToState(State.DESTROYED);
      assertThat(scenario.getState()).isEqualTo(State.DESTROYED);
    }
  }

  @Test
  public void fromStartedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.moveToState(State.CREATED);
      assertThat(scenario.getState()).isEqualTo(State.CREATED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromStartedToStarted() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.moveToState(State.STARTED);
      assertThat(scenario.getState()).isEqualTo(State.STARTED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromStartedToResumed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.moveToState(State.RESUMED);
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void fromDestroyedToDestroyed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.DESTROYED);
      scenario.moveToState(State.DESTROYED);
      assertThat(scenario.getState()).isEqualTo(State.DESTROYED);
    }
  }

  @Test
  public void recreateCreatedActivity() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.recreate();
      assertThat(scenario.getState()).isEqualTo(State.CREATED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
          });
    }
  }

  @Test
  public void recreateStartedActivity() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.recreate();
      assertThat(scenario.getState()).isEqualTo(State.STARTED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
          });
    }
  }

  @Test
  public void recreateResumedActivity() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.recreate();
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
          });
    }
  }

  @Ignore // TODO(hoisie): re-enable once github uses new robolectric
  @Test
  public void recreateIsChangingConfigurations() {
    try (ActivityScenario<RecordingActivity> activityScenario =
        ActivityScenario.launch(RecordingActivity.class)) {
      activityScenario.recreate();

      activityScenario.onActivity(
          activity ->
              assertThat(activity.getCallbacks())
                  .containsAtLeast("onPause true", "onStop true", "onDestroy true")
                  .inOrder());
    }
  }

  @Test
  public void activityResultWithNoResultData() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launchActivityForResult(RecreationRecordingActivity.class)) {
      scenario.onActivity(
          activity -> {
            activity.setResult(RESULT_OK);
            activity.finish();
          });
      assertThat(scenario.getResult().getResultCode()).isEqualTo(RESULT_OK);
      assertThat(scenario.getResult().getResultData()).isNull();
    }
  }

  @Test
  public void activityResultWithResultData() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launchActivityForResult(RecreationRecordingActivity.class)) {
      scenario.onActivity(
          activity -> {
            activity.setResult(RESULT_OK, new Intent().setAction(Intent.ACTION_SEND));
            activity.finish();
          });
      assertThat(scenario.getResult().getResultCode()).isEqualTo(RESULT_OK);
      assertThat(scenario.getResult().getResultData()).hasAction(Intent.ACTION_SEND);
    }
  }

  @Test
  public void activityResultWithResultDataAfterRecreate() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launchActivityForResult(RecreationRecordingActivity.class)) {
      scenario.recreate();
      scenario.onActivity(
          activity -> {
            activity.setResult(RESULT_OK, new Intent().setAction(Intent.ACTION_SEND));
            activity.finish();
          });
      assertThat(scenario.getResult().getResultCode()).isEqualTo(RESULT_OK);
      assertThat(scenario.getResult().getResultData()).hasAction(Intent.ACTION_SEND);
    }
  }

  @Test
  public void scenarioResultAfterLaunch() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      IllegalStateException e = assertThrows(IllegalStateException.class, scenario::getResult);
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "You must start Activity first. Make sure you are using launchActivityForResult() to"
                  + " launch an Activity.");
    }
  }

  @Test
  public void launch_unknownActivity() {
    Intent intent = new Intent();
    intent.setClassName("idontexist", "IdontExistEither");
    try {
      ActivityScenario.launch(intent);
      fail("launching an intent for a non-existing activity did not throw");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test
  public void onActivityShouldBeCallableFromMainThread() {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      InstrumentationRegistry.getInstrumentation()
          .runOnMainSync(
              () ->
                  scenario.onActivity(
                      activity ->
                          assertThat(activity.getMainLooper()).isEqualTo(Looper.myLooper())));
    }
  }

  /**
   * Verify that onActivity main looper synchronization is consistent between on device and
   * robolectric.
   */
  @Test
  public void onActivity_sync() {
    final List<String> events = new ArrayList<>();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {

      mainHandler.post(() -> events.add("before onActivity"));
      scenario.onActivity(
          new ActivityAction<RecreationRecordingActivity>() {
            @Override
            public void perform(RecreationRecordingActivity activity) {
              events.add("in onActivity");
              // as expected, on device tests become flaky and fail deterministically on
              // Robolectric with this line, as onActivity does not drain the main looper
              // after runnable executes
              // mainHandler.post(() -> events.add("post from onActivity"));
            }
          });

      assertThat(events).containsExactly("before onActivity", "in onActivity").inOrder();
    }
  }

  @Test
  public void launch_callbackSequence()
      throws ExecutionException, InterruptedException, TimeoutException {
    try (ActivityScenario<RecordingActivity> activityScenario =
        ActivityScenario.launch(RecordingActivity.class)) {

      // windowFocus event is async, so wait a small amount of time for that
      CountDownLatch windowFocusLatch = new CountDownLatch(1);
      activityScenario.onActivity(
          activity -> activity.listenForEvent(windowFocusLatch, "onWindowFocusChanged true"));
      windowFocusLatch.await(1, TimeUnit.SECONDS);

      activityScenario.onActivity(
          activity ->
              assertThat(activity.getCallbacks())
                  .containsExactly(
                      "onCreate",
                      "onStart",
                      "onPostCreate",
                      "onResume",
                      "onPostResume",
                      "onAttachedToWindow",
                      "onWindowFocusChanged true")
                  .inOrder());
    }
  }

  @Test
  public void launch_postingCallbackSequence() throws Exception {
    try (ActivityScenario<AsyncRecordingActivity> activityScenario =
        ActivityScenario.launch(AsyncRecordingActivity.class)) {

      CountDownLatch windowFocusLatch = new CountDownLatch(1);
      activityScenario.onActivity(
          activity -> activity.listenForEvent(windowFocusLatch, "onWindowFocusChanged true"));
      windowFocusLatch.await(1, TimeUnit.SECONDS);

      // wait for windowFocus post
      Espresso.onIdle();

      activityScenario.onActivity(
          activity ->
              // just assert the first few events, The exact order for the full event sequence is
              // not deterministic
              assertThat(activity.getCallbacks())
                  .containsAtLeast(
                      "onCreate",
                      "onStart",
                      "onPostCreate",
                      "onResume",
                      "onPostResume",
                      "post from onCreate")
                  .inOrder());
    }
  }

  @Test
  @SdkSuppress(minSdkVersion = 28) // ActivityOptions is added in API 28.
  public void launch_withActivityOptionsBundle() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(
            RecreationRecordingActivity.class,
            ActivityOptions.makeCustomAnimation(ApplicationProvider.getApplicationContext(), 0, 0)
                .toBundle())) {
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
    }
  }

  @Test
  public void launch_intentWithAction() {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("scenario://test"));

    ActivityScenario<IntentActivity> activityScenario = ActivityScenario.launch(intent);
    assertThat(activityScenario).isNotNull();
  }

  @Test
  public void launch_intentWithPackageName() {
    Intent intent = new Intent("custom.actions.intent.EXAMPLE_INTENT");
    intent.setPackage("androidx.test.core.app");

    ActivityScenario<IntentActivity> activityScenario = ActivityScenario.launch(intent);
    assertThat(activityScenario).isNotNull();
  }

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
