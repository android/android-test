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

package androidx.test.espresso.device

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setFlatMode
import androidx.test.espresso.device.action.setScreenOrientation
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ui.app.R
import androidx.test.ui.app.ScreenOrientationActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FoldableEspressoDeviceTest {
  @get:Rule
  val activityScenario: ActivityScenarioRule<ScreenOrientationActivity> =
    ActivityScenarioRule(ScreenOrientationActivity::class.java)

  @Test
  fun setFlatMode_returnsDeviceInteraction() {
    val deviceInteraction = EspressoDevice.onDevice().perform(setFlatMode())

    assertTrue(deviceInteraction is DeviceInteraction)
  }

  @Test
  fun setTabletopMode_returnsDeviceInteraction() {
    val deviceInteraction = EspressoDevice.onDevice().setTabletopMode()

    assertTrue(deviceInteraction is DeviceInteraction)
  }

  @Test
  fun onDevice_setScreenOrientationToLandscape() {
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("landscape")))
  }

  @Test
  fun onDevice_setScreenOrientationToPortrait() {
    onDevice().perform(setScreenOrientation(ScreenOrientation.PORTRAIT))

    onView(withId(R.id.current_screen_orientation)).check(matches(withText("portrait")))
  }
}
