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

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.State;
import android.content.Intent;
import androidx.test.core.app.testing.FinishItselfActivity;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for ActivityScenario's implementation. Verifies ActivityScenario APIs works consistently
 * across multiple different Android framework versions and Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioTest {
  @Test
  public void launchedActivityShouldBeResumed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
          });
    }
  }

  @Test
  public void launchedFinishItSelfActivityShouldBeLaunchable() throws Exception {
    try (ActivityScenario<FinishItselfActivity> scenario =
        ActivityScenario.launch(FinishItselfActivity.class)) {}
    ;
  }

  @Test
  public void fromResumedToDestroyed() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.DESTROYED);
    }
  }

  @Test
  public void fromResumedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
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
    }
  }

  @Test
  public void fromCreatedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.moveToState(State.CREATED);
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
    }
  }

  @Test
  public void fromStartedToCreated() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.STARTED);
      scenario.moveToState(State.CREATED);
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
    }
  }

  @Test
  public void recreateCreatedActivity() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
      scenario.moveToState(State.CREATED);
      scenario.recreate();
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
      scenario.onActivity(
          activity -> {
            assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
            assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
          });
    }
  }

  @Test
  public void activityResultWithNoResultData() throws Exception {
    try (ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
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
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
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
        ActivityScenario.launch(RecreationRecordingActivity.class)) {
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

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
