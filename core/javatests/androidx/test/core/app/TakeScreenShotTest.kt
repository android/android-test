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

package androidx.test.core.app

import androidx.test.core.app.testing.ActivityWithDialog
import androidx.test.core.app.testing.UiActivity
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

/** Simple test for takeScreenshot */
@RunWith(AndroidJUnit4::class)
class TakeScreenShotTest {

  @get:Rule val name = TestName()

  @Test
  fun takeScreenshot_blank() {
    val bitmap = takeScreenshot()

    assertThat(bitmap).isNotNull()
    // arbitrary check to ensure bitmap is non empty. Contents need to be manually validated for now
    assertThat(bitmap.byteCount).isGreaterThan(100)

    bitmap.writeToTestStorage(name.methodName)
  }

  @Test
  fun takeScreenshot_activity() {
    launchActivity<UiActivity>().use {
      val bitmap = takeScreenshot()

      assertThat(bitmap).isNotNull()
      // arbitrary check to ensure bitmap is non empty. Contents need to be manually validated for
      // now
      assertThat(bitmap.byteCount).isGreaterThan(100)

      bitmap.writeToTestStorage(name.methodName)
    }
  }

  @Test
  fun takeScreenshot_activityWithDialog() {
    launchActivity<ActivityWithDialog>().use {
      val bitmap = takeScreenshot()

      assertThat(bitmap).isNotNull()
      // arbitrary check to ensure bitmap is non empty. Contents need to be manually validated for
      // now
      assertThat(bitmap.byteCount).isGreaterThan(100)

      bitmap.writeToTestStorage(name.methodName)
    }
  }
}
