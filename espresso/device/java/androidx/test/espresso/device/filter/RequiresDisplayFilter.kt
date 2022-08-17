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

import android.app.Instrumentation
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.filters.AbstractFilter
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresDisplay} when running on a device that
 * doesn't have the required display attributes.
 */
internal class RequiresDisplayFilter(
  private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) : AbstractFilter() {
  override fun evaluateTest(description: Description): Boolean {
    var annotations = getMethodAnnotations(description)
    if (annotations.isEmpty()) {
      annotations = getClassAnnotations(description)
    }

    if (!annotations.isEmpty() && annotations[0] is RequiresDisplay) {
      val requiresDisplay: RequiresDisplay = annotations[0] as RequiresDisplay
      return requiresDisplay.widthSizeClass ==
        WidthSizeClass.getEnum(WidthSizeClass.compute(getWidthDp())) &&
        requiresDisplay.heightSizeClass ==
          HeightSizeClass.getEnum(HeightSizeClass.compute(getHeightDp()))
    } else {
      return true // no RequiresDisplay annotation, run the test
    }
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresDisplay if necessary"
  }

  /** Returns the current screen width in dp */
  private fun getWidthDp(): Int {
    val displayMetrics = instrumentation.getTargetContext().getResources().displayMetrics
    return (displayMetrics.widthPixels / displayMetrics.density).toInt()
  }

  /** Returns the current screen height in dp */
  private fun getHeightDp(): Int {
    val displayMetrics = instrumentation.getTargetContext().getResources().displayMetrics
    return (displayMetrics.heightPixels / displayMetrics.density).toInt()
  }
}
