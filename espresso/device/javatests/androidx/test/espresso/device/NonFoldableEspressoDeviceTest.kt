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
import androidx.test.espresso.device.action.setBookMode
import androidx.test.espresso.device.action.setFlatMode
import androidx.test.espresso.device.action.setTabletopMode
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.foldable.app.FoldableActivity
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NonFoldableEspressoDeviceTest {
  @get:Rule
  val activityRule: ActivityTestRule<FoldableActivity> =
    ActivityTestRule(FoldableActivity::class.java)

  @Test
  fun setTabletopModeOnNonFoldableDevice_throwsException() {
    assertThrows(DeviceControllerOperationException::class.java) {
      EspressoDevice.onDevice().setTabletopMode()
    }
  }

  @Test
  fun setBookModeOnNonFoldableDevice_throwsException() {
    assertThrows(DeviceControllerOperationException::class.java) {
      EspressoDevice.onDevice().setBookMode()
    }
  }

  @Test
  fun setFlatModeOnNonFoldableDevice_throwsException() {
    assertThrows(DeviceControllerOperationException::class.java) {
      EspressoDevice.onDevice().setFlatMode()
    }
  }
}
