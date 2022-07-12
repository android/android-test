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

import android.app.Activity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Restores device attributes such as screen orientation and display size to their original values
 * after tests are run.
 *
 * @param activityScenarioRule: an ActivityScenarioRule that will be the outer rule of this
 * RuleChain
 */
class EspressoDeviceRule<T : Activity>(activityScenarioRule: ActivityScenarioRule<T>) : TestRule {
  private val espressoDeviceRule: RuleChain =
    RuleChain.outerRule(activityScenarioRule)
      .around(ScreenOrientationRule())
      .around(DisplaySizeRule())

  override fun apply(statement: Statement, description: Description): Statement {
    return espressoDeviceRule.apply(statement, description)
  }
}
