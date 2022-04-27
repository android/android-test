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

import androidx.test.internal.runner.filters.ParentFilter
import org.junit.runner.Description

/**
 * Class that filters out tests annotated with {@link RequiresDeviceMode} when running on a device
 * that doesn't support the provided device mode.
 */
class RequiresDeviceModeFilter() : ParentFilter() {
  override fun evaluateTest(description: Description): Boolean {
    // TODO(b/230648826): Check supported device modes on test device and skip test if any required device
    // modes are not supported
    return true // run the test
  }

  override fun describe(): String {
    return "skip tests annotated with RequiresDeviceMode if necessary"
  }
}
