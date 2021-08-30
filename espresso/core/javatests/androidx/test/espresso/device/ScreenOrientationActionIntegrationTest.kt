/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.device

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.test.espresso.device.EspressoDevice.onDevice
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.apps.common.testing.util.AndroidTestUtil.ScreenOrientation
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for {@link ScreenOrientationAction} on a device. */
@RunWith(AndroidJUnit4::class)
class ScreenOrientationActionIntegrationTest {
  @Test
  fun testSetScreenOrientation() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    onDevice().perform(setScreenOrientation(ScreenOrientation.PORTRAIT))
    assert(
      instrumentation
        .getTargetContext()
        .getResources()
        .getConfiguration()
        .orientation
        .equals(ORIENTATION_PORTRAIT)
    )
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))
    assert(
      instrumentation
        .getTargetContext()
        .getResources()
        .getConfiguration()
        .orientation
        .equals(ORIENTATION_LANDSCAPE)
    )
  }

  @Test
  fun testSetScreenOrientationWithTwoConsecutiveCalls() {
    onDevice()
      .perform(setScreenOrientation(ScreenOrientation.PORTRAIT))
      .perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))
    assert(
      InstrumentationRegistry.getInstrumentation()
        .getTargetContext()
        .getResources()
        .getConfiguration()
        .orientation
        .equals(ORIENTATION_LANDSCAPE)
    )
  }
}
