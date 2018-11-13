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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Intent;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ActivityScenarioRule} using custom intent to start activity. */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioRuleWithCustomIntentTest {
  @Rule
  public ActivityScenarioRule<RecreationRecordingActivity> activityScenarioRule =
      new ActivityScenarioRule<>(
          new Intent(getApplicationContext(), RecreationRecordingActivity.class)
              .putExtra("MyIntentParameterKey", "MyIntentParameterValue"));

  @Test
  public void activityShouldBeResumedAutomatically() throws Exception {
    activityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
              assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
              assertThat(activity.getIntent())
                  .extras()
                  .string("MyIntentParameterKey")
                  .isEqualTo("MyIntentParameterValue");
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
              assertThat(activity.getIntent())
                  .extras()
                  .string("MyIntentParameterKey")
                  .isEqualTo("MyIntentParameterValue");
            });
  }

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
