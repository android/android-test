/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.rule.logging

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/** Simple integration test for [AtraceLogger]. */
@RunWith(AndroidJUnit4::class)
class AtraceLoggerTest {

  /**
   * Simple test that just exercises the happy path of starting and stopping a trace, and ensures
   * there are no crashes.
   */
  @Test
  fun trace() {
    val logger = AtraceLogger.getAtraceLoggerInstance(getInstrumentation())
    assertThat(logger).isNotNull()
    val categories = HashSet<String>()
    categories.add("am")
    logger.atraceStart(categories, 1024, 10, getInstrumentation().targetContext.cacheDir, null)
    logger.atraceStop()
  }
}
