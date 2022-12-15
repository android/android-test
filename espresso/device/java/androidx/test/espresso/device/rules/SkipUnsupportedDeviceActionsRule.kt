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

import androidx.test.platform.device.UnsupportedDeviceOperationException
import org.junit.Assume.assumeNoException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Rule for skipping tests when a device action is called on an unsupported device.
 *
 * DeviceActions throw UnsupportedDeviceOperationExceptions when actions cannot be performed on the
 * test device. This rule catches those exceptions and skips the tests where they are thrown.
 * Without this rule, tests will fail when a device action is performed on an unsupported device.
 *
 * Example usage:
 *
 * ```
 * class SampleTest {
 *   @get:Rule
 *   val rule = SkipUnsupportedDeviceActionsRule()
 *
 *   @Test
 *   fun testDeviceActions() {
 *     onDevice().setFlatMode()
 *   }
 * }
 * ```
 */
class SkipUnsupportedDeviceActionsRule() : TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        try {
          statement.evaluate()
        } catch (e: UnsupportedDeviceOperationException) {
          assumeNoException(e.message, e)
        }
      }
    }
  }
}
