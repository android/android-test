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

import android.content.res.Configuration
import android.util.Log
import androidx.test.internal.runner.filters.ParentFilter
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresDisplay} when running on a device that
 * doesn't have the requested display.
 */
class RequiresDisplayFilter() : ParentFilter() {
  companion object {
    val TAG = RequiresDisplayFilter::class.java.getSimpleName()
  }

  override fun evaluateTest(description: Description): Boolean {
    val requiresDisplay: RequiresDisplay? = getAnnotationForTest(description)
    if (requiresDisplay != null) {
      // annotation is present
      if (getDeviceSizeCategory() == requiresDisplay.sizeCategory &&
          getDeviceScreenDensity() == requiresDisplay.screenDensity
      ) {
        return true // run the test
      }
      return false // don't run the test
    }
    return true // no requiresDisplay, run the test
  }

  private fun getAnnotationForTest(description: Description): RequiresDisplay? {
    val requiresDisplay = description.getAnnotation(RequiresDisplay::class.java)
    if (requiresDisplay != null) {
      return requiresDisplay
    }
    val testClass: Class<*>? = description.getTestClass()
    if (testClass != null) {
      return testClass.getAnnotation(RequiresDisplay::class.java)
    }
    return null
  }

  private fun getDeviceSizeCategory(): Int {
    val screenLayout =
      InstrumentationRegistry.getInstrumentation()
        .getTargetContext()
        .getResources()
        .getConfiguration()
        .screenLayout
    val screenLayoutSizeMask = (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK)
    when (screenLayoutSizeMask) {
      Configuration.SCREENLAYOUT_SIZE_SMALL -> {
        Log.d(TAG, "device screen is small")
        return SizeCategory.SMALL.value
      }
      Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
        Log.d(TAG, "device screen is normal")
        return SizeCategory.NORMAL.value
      }
      Configuration.SCREENLAYOUT_SIZE_LARGE -> {
        Log.d(TAG, "device screen is large")
        return SizeCategory.LARGE.value
      }
      Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
        Log.d(TAG, "device screen is extra large")
        return SizeCategory.XLARGE.value
      }
    }
    Log.d(TAG, "device is unknown size category")
    return -1
  }

  private fun getDeviceScreenDensity(): Int {
    return InstrumentationRegistry.getInstrumentation()
      .getTargetContext()
      .getResources()
      .getConfiguration()
      .densityDpi
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresDisplay if necessary"
  }
}
