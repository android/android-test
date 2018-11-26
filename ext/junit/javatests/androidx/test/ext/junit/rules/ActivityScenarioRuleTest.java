/*
 * Copyright 2018 The Android Open Source Project
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
package androidx.test.ext.junit.rules;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ActivityScenarioRule}. */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioRuleTest {
  @Rule
  public ActivityScenarioRule<RecreationRecordingActivity> activityScenarioRule =
      new ActivityScenarioRule<>(RecreationRecordingActivity.class);

  @Test
  public void activityShouldBeResumedAutomatically() throws Exception {
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
              assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
            });
  }

  @Test
  public void recreateActivityShouldWork() throws Exception {
    activityScenarioRule.getScenario().recreate();
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
              assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
            });
  }

  @Test
  public void activityCanBeDestroyedManually() throws Exception {
    activityScenarioRule.getScenario().moveToState(Lifecycle.State.DESTROYED);
  }

  @Test
  public void activityCanBeClosedManually() throws Exception {
    activityScenarioRule.getScenario().close();
  }

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
