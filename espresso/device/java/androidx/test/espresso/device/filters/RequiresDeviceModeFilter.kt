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
package androidx.test.espresso.device.filters

import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.util.Log
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.internal.runner.filters.ParentFilter
import androidx.test.platform.app.InstrumentationRegistry
import java.nio.charset.Charset
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresDeviceMode} when running on a device
 * that doesn't support the provided device mode.
 */
class RequiresDeviceModeFilter() : ParentFilter() {
  companion object {
    val TAG = RequiresDeviceModeFilter::class.java.getSimpleName()
  }

  override fun evaluateTest(description: Description): Boolean {
    val annotations: List<RequiresDeviceMode> = getAnnotationsForTest(description)
    Log.d(TAG, "number of annotations: ${annotations.size}")
    if (!annotations.isEmpty()) {
      for (requiresDeviceMode in annotations) {
        // annotation is present - check if device supports the provided device mode
        if (getSupportedDeviceModes().contains(requiresDeviceMode.mode)) {
          Log.d(TAG, "Required device mode is supported, run the test")
        } else {
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

  private fun getSupportedDeviceModes(): List<Int> {
    // TODO: move this to DeviceController
    val parcelFileDescriptor =
      InstrumentationRegistry.getInstrumentation()
        .getUiAutomation()
        .executeShellCommand("cmd device_state print-states")
    var modes: String
    AutoCloseInputStream(parcelFileDescriptor).use { inputStream ->
      modes = inputStream.readBytes().toString(Charset.defaultCharset())
    }
    Log.d(TAG, "modes: $modes")
    val supportedModes: ArrayList<Int> = ArrayList()
    if (modes.contains("HALF_OPENED")) {
      supportedModes.add(DeviceMode.TABLETOP.mode)
      supportedModes.add(DeviceMode.BOOK.mode)
    }
    if (modes.contains("OPENED")) {
      supportedModes.add(DeviceMode.FLAT.mode)
    }
    return supportedModes
  }

  private fun getAnnotationsForTest(description: Description): List<RequiresDeviceMode> {
    val annotations: Collection<Annotation> = description.getAnnotations()

    val requiresDeviceModeAnnotations: ArrayList<RequiresDeviceMode> = ArrayList()
    for (a in annotations) {
      if (a is RequiresDeviceMode) {
        requiresDeviceModeAnnotations.add(a)
      } else if (a is RequiresDeviceModes) {
        for (v in a.value) {
          requiresDeviceModeAnnotations.add(v)
        }
      }
    }

    if (requiresDeviceModeAnnotations.isEmpty()) {
      val testClass: Class<*>? = description.getTestClass()
      if (testClass != null) {
        // TODO: use getAnnotations() instead, to support lower APIs
        for (a in testClass.getAnnotations()) {
          if (a is RequiresDeviceMode) {
            requiresDeviceModeAnnotations.add(a)
          } else if (a is RequiresDeviceModes) {
            for (v in a.value) {
              requiresDeviceModeAnnotations.add(v)
            }
          }
        }
      }
    }
    return requiresDeviceModeAnnotations
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresDeviceMode if necessary"
  }
}
