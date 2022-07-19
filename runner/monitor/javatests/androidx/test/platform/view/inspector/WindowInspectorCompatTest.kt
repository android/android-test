/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.platform.view.inspector

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.view.inspector.fixtures.ActivityWithDialog
import androidx.test.platform.view.inspector.fixtures.SimpleActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WindowInspectorCompatTest {

  @Test
  fun getGlobalWindowViews_empty() {
    InstrumentationRegistry.getInstrumentation()
      .runOnMainSync(
        Runnable { assertThat(WindowInspectorCompat.getGlobalWindowViews()).isEmpty() }
      )
  }

  @Test
  fun getGlobalWindowViews_notMainThread() {
    assertThrows(IllegalStateException::class.java) { WindowInspectorCompat.getGlobalWindowViews() }
  }

  @Test
  fun getGlobalWindowViews_activity() {
    ActivityScenario.launch<SimpleActivity>(SimpleActivity::class.java).use { scenario ->
      assertThat(WindowInspectorCompat.getGlobalWindowViews()).hasSize(1)
    }
  }

  @Test
  fun getGlobalWindowViews_activityDialog() {
    ActivityScenario.launch<ActivityWithDialog>(ActivityWithDialog::class.java).use { scenario ->
      assertThat(WindowInspectorCompat.getGlobalWindowViews()).hasSize(2)
    }
  }
}
