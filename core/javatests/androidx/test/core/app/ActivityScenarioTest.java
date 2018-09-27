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

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.State;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
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
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromResumedToCreated() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.CREATED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromResumedToStarted() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.STARTED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromResumedToResumed() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.RESUMED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromCreatedToCreated() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.CREATED);
    scenario.moveToState(State.CREATED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromCreatedToStarted() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.CREATED);
    scenario.moveToState(State.STARTED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromCreatedToResumed() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.CREATED);
    scenario.moveToState(State.RESUMED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromStartedToCreated() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.STARTED);
    scenario.moveToState(State.CREATED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromStartedToStarted() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.STARTED);
    scenario.moveToState(State.STARTED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void fromStartedToResumed() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.STARTED);
    scenario.moveToState(State.RESUMED);
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
        });
  }

  @Test
  public void recreateCreatedActivity() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.CREATED);
    scenario.recreate();
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.STOPPED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
        });
  }

  @Test
  @SdkSuppress(minSdkVersion = 16)
  // TODO: It seems a bug in Android Framework in API level 15 where an Intent to re-order Activity
  // to front can be ignored under some conditions. Figure out the cause and write workaround.
  public void recreateStartedActivity() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.moveToState(State.STARTED);
    scenario.recreate();
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.PAUSED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
        });
  }

  @Test
  public void recreateResumedActivity() throws Exception {
    ActivityScenario<RecreationRecordingActivity> scenario =
        ActivityScenario.launch(RecreationRecordingActivity.class);
    scenario.recreate();
    scenario.runOnActivity(
        activity -> {
          assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
          assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
        });
  }

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
