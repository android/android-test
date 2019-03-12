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

package androidx.test.core.app

import android.app.Activity
import android.app.Activity.RESULT_OK
import androidx.lifecycle.Lifecycle.State
import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.testing.RecreationRecordingActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

private fun lastLifeCycleTransition(activity: Activity): Stage {
  return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
}

/**
 * An example test with ActivityScenario using Kotlin extensions.
 */
@RunWith(AndroidJUnit4::class)
class ActivityScenarioKotlinTest {
  @Test
  fun basicUseCase() {
    launchActivity<RecreationRecordingActivity>().use { scenario ->
      with(scenario) {
        assertThat(state).isEqualTo(State.RESUMED)
        onActivity {
          assertThat(it.numberOfRecreations).isEqualTo(0)
          assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.RESUMED)
        }

        recreate()
        assertThat(state).isEqualTo(State.RESUMED)
        onActivity {
          assertThat(it.numberOfRecreations).isEqualTo(1)
          assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.RESUMED)
        }

        moveToState(State.STARTED)
        assertThat(state).isEqualTo(State.STARTED)
        onActivity {
          assertThat(it.numberOfRecreations).isEqualTo(1)
          assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.PAUSED)
        }

        moveToState(State.CREATED)
        assertThat(state).isEqualTo(State.CREATED)
        onActivity {
          assertThat(it.numberOfRecreations).isEqualTo(1)
          assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.STOPPED)
        }

        moveToState(State.RESUMED)
        assertThat(state).isEqualTo(State.RESUMED)
        onActivity {
          it.setResult(RESULT_OK, Intent().setAction(Intent.ACTION_SEND))
          it.finish()
        }
        assertThat(result.resultCode).isEqualTo(RESULT_OK)
        assertThat(result.resultData).hasAction(Intent.ACTION_SEND)
      }
    }
  }

  @Test
  fun basicUseCaseWithCustomIntent() {
    val intent = Intent(getApplicationContext(), RecreationRecordingActivity::class.java).apply {
      putExtra("MyIntentParameterKey", "MyIntentParameterValue")
    }
    launchActivity<RecreationRecordingActivity>(intent).use { scenario ->
      scenario.onActivity { activity ->
        assertThat(activity.intent)
                .extras()
                .string("MyIntentParameterKey")
                .isEqualTo("MyIntentParameterValue")
      }
    }
  }
}
