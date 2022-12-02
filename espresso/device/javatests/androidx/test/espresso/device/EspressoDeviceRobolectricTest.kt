/*
 * Copyright (C) 2022 The Android Open Source Project
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

import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setDisplaySize
import androidx.test.espresso.device.action.setScreenOrientation
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.AssumptionViolatedException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EspressoDeviceRobolectricTest {
  @Test
  fun onDevice_setScreenOrientationThrowsAssumptionViolatedException() {
    val e: AssumptionViolatedException =
      assertThrows(AssumptionViolatedException::class.java) {
        onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))
      }

    assertThat(e.message)
      .isEqualTo("Setting screen orientation is not currently supported on Robolectric tests.")
  }

  @Test
  fun onDevice_setFlatModeThrowsAssumptionViolatedException() {
    val e: AssumptionViolatedException =
      assertThrows(AssumptionViolatedException::class.java) { onDevice().setFlatMode() }

    assertThat(e.message)
      .isEqualTo("Setting a device mode is not currently supported on Robolectric tests.")
  }

  @Test
  fun onDevice_setDisplaySizeThrowsAssumptionViolatedException() {
    val e: AssumptionViolatedException =
      assertThrows(AssumptionViolatedException::class.java) {
        onDevice().perform(setDisplaySize(WidthSizeClass.EXPANDED, HeightSizeClass.EXPANDED))
      }

    assertThat(e.message)
      .isEqualTo("Setting display size is not currently supported on Robolectric tests.")
  }
}
