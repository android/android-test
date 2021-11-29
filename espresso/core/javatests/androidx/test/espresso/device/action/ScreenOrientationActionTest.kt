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

package androidx.test.espresso.device.action

import androidx.test.espresso.device.context.InstrumentationTestActionContext
import androidx.test.espresso.device.controller.DeviceController
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(JUnit4::class)
class ScreenOrientationActionTest {
  @Test
  fun perform_doesNotCallDeviceControllerWhenOrientationDoesNotNeedToBeChanged() {
    val deviceController: DeviceController = mock(DeviceController::class.java)
    val action: ScreenOrientationAction = ScreenOrientationAction(ScreenOrientation.PORTRAIT)

    action.perform(InstrumentationTestActionContext(), deviceController)

    verify(deviceController, never()).setScreenOrientation(ScreenOrientation.PORTRAIT.orientation)
  }
}
