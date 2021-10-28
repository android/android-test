/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setScreenOrientation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EspressoDeviceRobolectricTest {

  @Test
  fun onDevice_setScreenOrientationToLandscape() {
    EspressoDevice.onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    assertEquals(
      Configuration.ORIENTATION_LANDSCAPE,
      ApplicationProvider.getApplicationContext<Context>()
        .getResources()
        .getConfiguration()
        .orientation
    )
  }

  @Test
  fun onDevice_setScreenOrientationToPortrait() {
    EspressoDevice.onDevice().setScreenOrientation(ScreenOrientation.PORTRAIT)

    assertEquals(
      Configuration.ORIENTATION_PORTRAIT,
      ApplicationProvider.getApplicationContext<Context>()
        .getResources()
        .getConfiguration()
        .orientation
    )
  }
}
