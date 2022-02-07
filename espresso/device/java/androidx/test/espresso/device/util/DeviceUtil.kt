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

@file:JvmName("DeviceUtil")

package androidx.test.espresso.device.util

import android.os.Build

/** Collection of utility methods for getting information about the test device. */

/**
 * Detects if the test is running on an emulator or a real device using some heuristics based on the
 * device properties.
 */
fun isTestDeviceAnEmulator(): Boolean {
  val qemu: String? = System.getProperty("ro.kernel.qemu", "?")
  return qemu.equals("1") ||
    Build.HARDWARE.contains("goldfish") ||
    Build.HARDWARE.contains("ranchu")
}

/**
 * Detects if the test is running on Robolectric using some heuristics based on the device
 * properties.
 */
fun isRobolectricTest(): Boolean {
  return Build.FINGERPRINT.equals("robolectric")
}
