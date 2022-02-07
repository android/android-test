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

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setFlatMode
import androidx.test.espresso.device.action.setScreenOrientation
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ui.app.R
import androidx.test.ui.app.ScreenOrientationActivity
import androidx.test.ui.app.ScreenOrientationWithoutOnConfigurationChangedActivity
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EspressoDeviceTest {
  @get:Rule
  val activityScenario: ActivityScenarioRule<ScreenOrientationActivity> =
    ActivityScenarioRule(ScreenOrientationActivity::class.java)

  @Test
  fun setFlatModeOnNonFoldableDevice_throwsException() {
    assertThrows(DeviceControllerOperationException::class.java) {
      EspressoDevice.onDevice().setFlatMode()
    }
  }

  @Test
  fun setTabletopModeOnNonFoldableDevice_throwsException() {
    assertThrows(DeviceControllerOperationException::class.java) {
      EspressoDevice.onDevice().setTabletopMode()
    }
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

  @Test
  fun onDevice_setScreenOrientationToLandscapeThenClick() {
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.current_screen_orientation)).perform(click())
    onView(withId(R.id.current_screen_orientation)).check(matches(withText("landscape")))
  }

  @Test
  fun onDevice_throwsFromScenarioOnActivity() {
    activityScenario
      .getScenario()
      .onActivity({ activity: ScreenOrientationActivity ->
        assertThrows(IllegalStateException::class.java) {
          onDevice().setScreenOrientation(ScreenOrientation.PORTRAIT)
        }
      })
  }

  @Test
  fun onDevice_setScreenOrientationToLandscapeAndThenToPortraitWithoutConfigurationHandling() {
    ActivityScenario.launch(ScreenOrientationWithoutOnConfigurationChangedActivity::class.java)
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.screen_orientation)).check(matches(withText("landscape")))

    onDevice().perform(setScreenOrientation(ScreenOrientation.PORTRAIT))

    onView(withId(R.id.screen_orientation)).check(matches(withText("portrait")))
  }

  @Test
  fun onDevice_clickAndThenSetScreenOrientationToLandscapeWithoutConfigurationHandling() {
    ActivityScenario.launch(ScreenOrientationWithoutOnConfigurationChangedActivity::class.java)
    onView(withId(R.id.screen_orientation)).perform(click())

    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.screen_orientation)).check(matches(withText("landscape")))
  }

  @Test
  fun onDevice_setScreenOrientationToLandscapeThenClickWithoutConfigurationHandling() {
    ActivityScenario.launch(ScreenOrientationWithoutOnConfigurationChangedActivity::class.java)
    onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE))

    onView(withId(R.id.screen_orientation)).perform(click())
    onView(withId(R.id.screen_orientation)).check(matches(withText("landscape")))
  }
}
