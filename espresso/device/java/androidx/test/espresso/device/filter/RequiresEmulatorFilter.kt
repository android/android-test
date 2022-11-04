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

import androidx.test.espresso.device.util.isTestDeviceAnEmulator
import androidx.test.filters.AbstractFilter
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresEmulator} when running on a device
 * that is not an emulator.
 */
internal class RequiresEmulatorFilter : AbstractFilter() {
  override fun evaluateTest(description: Description): Boolean {
    var annotations = getMethodAnnotations(description)
    if (annotations.isEmpty()) {
      annotations = getClassAnnotations(description)
    }

    if (!annotations.isEmpty() && annotations[0] is RequiresEmulator) {
      return isTestDeviceAnEmulator()
    } else {
      return true // no RequiresEmulator annotation, run the test
    }
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresEmulator if necessary"
  }
}
