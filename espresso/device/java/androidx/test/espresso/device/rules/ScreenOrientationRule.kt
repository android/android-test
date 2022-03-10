/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.res.Configuration
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setScreenOrientation
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Restores the screen orientation when a test case finishes.
 *
 * @param defaultOrientation: the screen orientation will be set to the specified value, or the one
 * that the test starts with if none is given.
 */
class ScreenOrientationRule(private val defaultOrientation: ScreenOrientation?) : TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        val orientationToRestore = defaultOrientation ?: getCurrentOrientation()
        statement.evaluate()
        onDevice().perform(setScreenOrientation(orientationToRestore))
      }
    }
  }

  private fun getCurrentOrientation(): ScreenOrientation {
    val currentOrientation =
      InstrumentationRegistry.getInstrumentation()
        .getTargetContext()
        .getResources()
        .getConfiguration()
        .orientation
    return if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
      ScreenOrientation.LANDSCAPE
    else ScreenOrientation.PORTRAIT
  }
}
