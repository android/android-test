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

import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setFlatMode
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EspressoDeviceTest {

  @Test
  fun setFlatModeWithPerform_returnsDeviceInteraction() {
    val deviceInteraction = EspressoDevice.onDevice().perform(setFlatMode())

    assertTrue(deviceInteraction is DeviceInteraction)
  }

  @Test
  fun setFlatMode_returnsDeviceInteraction() {
    val deviceInteraction = EspressoDevice.onDevice().setFlatMode()

    assertTrue(deviceInteraction is DeviceInteraction)
  }

  @Test
  fun setScreenOrientation_returnsDeviceInteraction() {
    val deviceInteraction =
      EspressoDevice.onDevice().setScreenOrientation(ScreenOrientation.PORTRAIT)

    assertTrue(deviceInteraction is DeviceInteraction)
  }
}
