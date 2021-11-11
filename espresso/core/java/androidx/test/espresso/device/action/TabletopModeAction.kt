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

import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import androidx.test.espresso.device.controller.DeviceMode

/** Action to set the test device to be folded with the hinge in a horizontal position. */
internal class TabletopModeAction() : DeviceAction {
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    // TODO(b/203801760): Check current device mode and return if already in tabletop mode.

    deviceController.setDeviceMode(DeviceMode.TABLETOP.mode)

    // TODO(b/203801783): Synchronize device controller call.
  }
}
