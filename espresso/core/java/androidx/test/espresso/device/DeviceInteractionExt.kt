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
import androidx.test.espresso.device.action.setFlatMode as getSetFlatModeDeviceAction
import androidx.test.espresso.device.action.setScreenOrientation as getSetScreenOrientationDeviceAction
import androidx.test.espresso.device.action.setTabletopMode as getSetTabletopModeDeviceAction

/**
 * Set device screen to be completely flat, like a tablet. For details on foldable postures, see
 * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
 *
 * This action is for foldable devices only. Currently only supported for tests run on Android
 * Emulators.
 * @throws UnsupportedDeviceOperationException if used on a real device.
 * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
 */
fun DeviceInteraction.setFlatMode(): DeviceInteraction {
  perform(getSetFlatModeDeviceAction())
  return this
}

/**
 * Set device's screen orientation.
 * @param orientation the orientation to set the device to (portait or landscape)
 */
fun DeviceInteraction.setScreenOrientation(orientation: ScreenOrientation): DeviceInteraction {
  perform(getSetScreenOrientationDeviceAction(orientation))
  return this
}

/**
 * Set device screen to be folded with the hinge in the horizontal position. For details on foldable
 * postures, see
 * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
 *
 * This action is for foldable devices only. Currently only supported for tests run on Android
 * Emulators.
 * @throws UnsupportedDeviceOperationException if used on a real device.
 * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
 */
fun DeviceInteraction.setTabletopMode(): DeviceInteraction {
  perform(getSetTabletopModeDeviceAction())
  return this
}
