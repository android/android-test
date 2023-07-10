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

import androidx.test.annotation.ExperimentalTestApi
import androidx.test.espresso.device.action.DeviceAction
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setBookMode as getSetBookModeDeviceAction
import androidx.test.espresso.device.action.setClosedMode as getSetClosedModeDeviceAction
import androidx.test.espresso.device.action.setDisplaySize as getSetDisplaySizeDeviceAction
import androidx.test.espresso.device.action.setFlatMode as getSetFlatModeDeviceAction
import androidx.test.espresso.device.action.setScreenOrientation as getSetScreenOrientationDeviceAction
import androidx.test.espresso.device.action.setTabletopMode as getSetTabletopModeDeviceAction
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.internal.util.Checks.checkNotMainThread
import androidx.test.platform.device.DeviceController
import javax.inject.Inject

/**
 * API surface for performing device-centric operations.
 *
 * <p>This API is experimental and subject to change.
 */
@ExperimentalTestApi
class DeviceInteraction
@Inject
constructor(private val context: ActionContext, private val deviceController: DeviceController) {

  /**
   * Performs the given action on the test device. This method should not be called on the main
   * thread. The calling thread is blocked until the requested device action is completed.
   *
   * @param action the DeviceAction to execute.
   * @return this interaction for further perform/verification calls.
   * @throws IllegalStateException when being invoked on the main thread.
   */
  fun perform(action: DeviceAction): DeviceInteraction {
    checkNotMainThread()
    action.perform(context, deviceController)
    return this
  }

  companion object {
    /**
     * Set device screen to be folded with the hinge in the horizontal position. For details on
     * foldable postures, see
     * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
     *
     * This action is for foldable devices only. Currently only supported for tests run on Android
     * Emulators.
     *
     * @throws UnsupportedDeviceOperationException if used on a real device.
     * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setTabletopMode(): DeviceInteraction {
      perform(getSetTabletopModeDeviceAction())
      return this
    }

    /**
     * Set device screen to be folded with the hinge in the vertical position. For details on
     * foldable postures, see
     * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
     *
     * This action is for foldable devices only. Currently only supported for tests run on Android
     * Emulators.
     *
     * @throws UnsupportedDeviceOperationException if used on a real device.
     * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setBookMode(): DeviceInteraction {
      perform(getSetBookModeDeviceAction())
      return this
    }

    /**
     * Set device screen to be completely flat, like a tablet. For details on foldable postures, see
     * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
     *
     * This action is for foldable devices only. Currently only supported for tests run on Android
     * Emulators.
     *
     * @throws UnsupportedDeviceOperationException if used on a real device.
     * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setFlatMode(): DeviceInteraction {
      perform(getSetFlatModeDeviceAction())
      return this
    }

    /**
     * Set device screen to be closed.
     *
     * This action is for foldable devices only. Currently only supported for tests run on Android
     * Emulators.
     *
     * @throws UnsupportedDeviceOperationException if used on a real device.
     * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setClosedMode(): DeviceInteraction {
      perform(getSetClosedModeDeviceAction())
      return this
    }

    /**
     * Set device's screen orientation.
     *
     * @param orientation the orientation to set the device to (portait or landscape)
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setScreenOrientation(orientation: ScreenOrientation): DeviceInteraction {
      perform(getSetScreenOrientationDeviceAction(orientation))
      return this
    }

    /**
     * Set device's display size.
     *
     * @param widthSizeClass the width to set the device display to
     * @param heightSizeClass the height to set the device display to
     */
    @ExperimentalTestApi
    fun DeviceInteraction.setDisplaySize(
      widthSizeClass: WidthSizeClass,
      heightSizeClass: HeightSizeClass
    ): DeviceInteraction {
      perform(getSetDisplaySizeDeviceAction(widthSizeClass, heightSizeClass))
      return this
    }
  }
}
