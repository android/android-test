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

/** Entry point for device action operations. */

/**
 * Set device's screen orientation.
 * @param orientation the orientation to set the device to (portait or landscape)
 */
fun setScreenOrientation(orientation: ScreenOrientation): DeviceAction {
  return ScreenOrientationAction(orientation)
}

/** Enum for screen orientations a device can be set to. */
enum class ScreenOrientation(val orientation: Int) {
  PORTRAIT(0),
  LANDSCAPE(1)
}
