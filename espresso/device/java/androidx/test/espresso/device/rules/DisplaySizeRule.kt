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
package androidx.test.espresso.device.rules

import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getDeviceApiLevel
import androidx.test.platform.device.UnsupportedDeviceOperationException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** Test rule for restoring device to its starting display size when a test case finishes */
class DisplaySizeRule : TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        if (getDeviceApiLevel() < 24) {
          throw UnsupportedDeviceOperationException(
            "DisplaySizeRule is not supported on devices with APIs below 24."
          )
        }

        try {
          statement.evaluate()
        } finally {
          // Always reset the display size to it's original size
          executeShellCommand("wm size reset")
        }
      }
    }
  }

  companion object {
    private val TAG = DisplaySizeRule::class.java.simpleName
  }
}
