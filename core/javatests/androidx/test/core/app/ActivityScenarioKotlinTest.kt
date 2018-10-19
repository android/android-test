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
import android.arch.lifecycle.Lifecycle.State
import androidx.test.core.app.testing.RecreationRecordingActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

private fun lastLifeCycleTransition(activity: Activity): Stage {
  return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
}

/**
 * An example test with ActivityScenario in Kotlin.
 */
@RunWith(AndroidJUnit4::class)
class ActivityScenarioKotlinTest {
  @Test
  fun basicUseCase() {
    val scenario = ActivityScenario.launch(RecreationRecordingActivity::class.java)
    scenario.onActivity {
      assertThat(it.numberOfRecreations).isEqualTo(0)
      assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.RESUMED)
    }

    scenario.recreate()
    scenario.onActivity {
      assertThat(it.numberOfRecreations).isEqualTo(1)
      assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.RESUMED)
    }

    scenario.moveToState(State.STARTED)
    scenario.onActivity {
      assertThat(it.numberOfRecreations).isEqualTo(1)
      assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.PAUSED)
    }

    scenario.moveToState(State.CREATED)
    scenario.onActivity {
      assertThat(it.numberOfRecreations).isEqualTo(1)
      assertThat(lastLifeCycleTransition(it)).isEqualTo(Stage.STOPPED)
    }

    scenario.moveToState(State.DESTROYED)
  }
}
