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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.TestRequestBuilder
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequiresEmulatorTest {
  class SampleRequiresEmulator {
    @RequiresEmulator @Test fun skipThisIfNotOnEmulator() {}

    @Test fun alwaysRunThis() {}
  }

  @Test
  fun testRequiresEmulator() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(SampleRequiresEmulator::class.java.name).build()
    val result: Result = testRunner.run(request)

    assertEquals(2, result.getRunCount())
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
