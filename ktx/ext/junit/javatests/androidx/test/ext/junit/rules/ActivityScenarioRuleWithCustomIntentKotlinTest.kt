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
package androidx.test.ext.junit.rules

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.testing.RecreationRecordingActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private fun lastLifeCycleTransition(activity: Activity): Stage {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
}

/**
 * An example test with ActivityScenarioRule with custom intent using Kotlin extensions.
 */
@RunWith(AndroidJUnit4::class)
class ActivityScenarioRuleWithCustomIntentKotlinTest {
    @get:Rule val activityScenarioRule = activityScenarioRule<RecreationRecordingActivity>(
            Intent(getApplicationContext(), RecreationRecordingActivity::class.java).apply {
                putExtra("MyIntentParameterKey", "MyIntentParameterValue")
            }
    )

    @Test
    @Throws(Exception::class)
    fun activityShouldBeResumedAutomatically() {
        activityScenarioRule
                .scenario
                .onActivity { activity ->
                    assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED)
                    assertThat(activity.intent)
                            .extras()
                            .string("MyIntentParameterKey")
                            .isEqualTo("MyIntentParameterValue")
                }
    }
}
