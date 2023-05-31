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
package androidx.test.espresso.device.filter

import android.os.Build
import android.util.Log
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.filters.AbstractFilter
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresDeviceMode} when running on a device
 * that doesn't support the provided device mode.
 */
internal class RequiresDeviceModeFilter() : AbstractFilter() {
  override fun evaluateTest(description: Description): Boolean {
    val annotations = getMethodAnnotations(description)
    annotations.addAll(getClassAnnotations(description))
    if (!annotations.isEmpty()) {
      val supportedDeviceModes = getSupportedDeviceModes()
      val deviceModesAnnotations = mutableListOf<RequiresDeviceMode>()
      for (annotation in annotations) {
        if (annotation is RequiresDeviceModes) {
          for (v: RequiresDeviceMode in annotation.value) {
            deviceModesAnnotations.add(v)
          }
        } else if (annotation is RequiresDeviceMode) {
          deviceModesAnnotations.add(annotation)
        }
      }
      for (annotation in deviceModesAnnotations) {
        if (!supportedDeviceModes.contains(annotation.mode)) {
          Log.d(TAG, "Required device mode is not supported, skip the test")
          return false
        }
      }
    }
    Log.d(
      TAG,
      "No requires device mode annotation or all required modes are supported, run the test"
    )
    return true
  }

  private fun getSupportedDeviceModes(): List<DeviceMode> {
    if (Build.VERSION.SDK_INT < 29) {
      // Foldable postures are not available on device running on API 29 and below.
      return emptyList()
    }

    val supportedModes = mutableListOf<DeviceMode>()
    if (Build.VERSION.SDK_INT == 30 || Build.VERSION.SDK_INT == 31) {
      // The "device_state print-states" shell command does not work on APIs 30-31. For these
      // devices, check if any folding feaures are present and assume that devices that have
      // folding features can be half-open and open.
      val displayFeatures = executeShellCommand("cmd settings get global display_features")
      if (displayFeatures.contains("fold") || displayFeatures.contains("hinge")) {
        supportedModes.add(DeviceMode.TABLETOP)
        supportedModes.add(DeviceMode.BOOK)
        supportedModes.add(DeviceMode.FLAT)
        supportedModes.add(DeviceMode.CLOSED)
      }
    } else { // API 32+
      // Example output on a foldable device:
      // "Supported states: [
      //  DeviceState{identifier=1, name='CLOSED'},
      //  DeviceState{identifier=2, name='HALF_OPENED'},
      //  DeviceState{identifier=3, name='OPENED'},
      // ]"
      val modes = executeShellCommand("cmd device_state print-states")
      if (modes.contains("HALF_OPENED")) {
        supportedModes.add(DeviceMode.TABLETOP)
        supportedModes.add(DeviceMode.BOOK)
      }
      if (modes.contains("OPENED")) {
        supportedModes.add(DeviceMode.FLAT)
      }
      if (modes.contains("CLOSED")) {
        supportedModes.add(DeviceMode.CLOSED)
      }
    }
    return supportedModes
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresDeviceMode if necessary"
  }

  companion object {
    private val TAG = RequiresDeviceModeFilter::class.java.getSimpleName()
  }
}
