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

@file:JvmName("DeviceActions")

package androidx.test.espresso.device.action

import android.os.Handler
import android.os.Looper
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import java.util.concurrent.Executor

/** Entry point for device action operations. */
private val mainExecutor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }

/**
 * Set device screen to be folded with the hinge in the horizontal position. For details on foldable
 * postures, see
 * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
 *
 * This action is for foldable devices only. Currently only supported for tests run on Android
 * Emulators.
 *
 * @throws UnsupportedDeviceOperationException if used on a real device.
 * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
 */
@ExperimentalTestApi
fun setTabletopMode(): DeviceAction {
  return TabletopModeAction(mainExecutor)
}

/**
 * Set device screen to be folded with the hinge in the vertical position. For details on foldable
 * postures, see
 * https://developer.android.com/guide/topics/large-screens/learn-about-foldables#foldable_postures
 *
 * This action is for foldable devices only. Currently only supported for tests run on Android
 * Emulators.
 *
 * @throws UnsupportedDeviceOperationException if used on a real device.
 * @throws DeviceControllerOperationException when called on a non-foldable Emulator.
 */
@ExperimentalTestApi
fun setBookMode(): DeviceAction {
  return BookModeAction(mainExecutor)
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
fun setFlatMode(): DeviceAction {
  return FlatModeAction(mainExecutor)
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
fun setClosedMode(): DeviceAction {
  return ClosedModeAction(mainExecutor)
}

/**
 * Set device's screen orientation.
 *
 * @param orientation the orientation to set the device to (portait or landscape)
 */
@ExperimentalTestApi
fun setScreenOrientation(orientation: ScreenOrientation): DeviceAction {
  return ScreenOrientationAction(orientation)
}

/**
 * Set device's window size.
 *
 * @param widthSizeClass the window width to set the device to
 * @param heightSizeClass the window height to set the device to
 */
@ExperimentalTestApi
fun setDisplaySize(widthSizeClass: WidthSizeClass, heightSizeClass: HeightSizeClass): DeviceAction {
  return DisplaySizeAction(widthDisplaySize = widthSizeClass, heightDisplaySize = heightSizeClass)
}

/** Enum for screen orientations a device can be set to. */
@ExperimentalTestApi
enum class ScreenOrientation(val orientation: Int) {
  PORTRAIT(0),
  LANDSCAPE(1)
}
