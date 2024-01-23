/*
 * Copyright (C) 2024 The Android Open Source Project
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

package androidx.test.gradletests.espresso.device

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setScreenOrientation
import androidx.test.espresso.device.rules.ScreenOrientationRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EspressoDeviceTest {
  private val activityRule: ActivityScenarioRule<ScreenOrientationActivity> =
    ActivityScenarioRule(ScreenOrientationActivity::class.java)

  private val screenOrientationRule: ScreenOrientationRule =
    ScreenOrientationRule(ScreenOrientation.PORTRAIT)

  @get:Rule
  val ruleChain: RuleChain = RuleChain.outerRule(activityRule).around(screenOrientationRule)

  @Test
  fun onDevice_setScreenOrientationToLandscape() {
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("landscape")))
  }

  @Test
  fun onDevice_setScreenOrientationToLandscapeAndThenToPortrait() {
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("landscape")))

    onDevice().perform(setScreenOrientation(ScreenOrientation.PORTRAIT))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("portrait")))
  }

  @Test
  fun onDevice_clickAndThenSetScreenOrientationToLandscape() {
    onView(withId(R.id.current_screen_orientation)).perform(click())

    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("landscape")))
  }
}
