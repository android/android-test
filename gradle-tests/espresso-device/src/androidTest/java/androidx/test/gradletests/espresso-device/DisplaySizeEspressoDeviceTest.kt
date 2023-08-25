/*
 * Copyright (C) 2023 The Android Open Source Project
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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.setDisplaySize
import androidx.test.espresso.device.rules.DisplaySizeRule
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DisplaySizeEspressoDeviceTest {
  private val activityRule: ActivityScenarioRule<DisplaySizeActivity> =
    ActivityScenarioRule(DisplaySizeActivity::class.java)
  private val displaySizeRule: DisplaySizeRule = DisplaySizeRule()

  @get:Rule val ruleChain: RuleChain = RuleChain.outerRule(activityRule).around(displaySizeRule)

  @Test
  fun onDevice_setDisplaySizeToCompactWidthAndHeight() {
    onDevice().perform(setDisplaySize(WidthSizeClass.COMPACT, HeightSizeClass.COMPACT))

    onView(withId(R.id.screen_width_display_size)).check(matches(withText("Compact width")))
    onView(withId(R.id.screen_height_display_size)).check(matches(withText("Compact height")))
  }

  @Test
  fun onDevice_setDisplaySizeToMediumWidthAndHeight() {
    onDevice().perform(setDisplaySize(WidthSizeClass.MEDIUM, HeightSizeClass.MEDIUM))

    onView(withId(R.id.screen_width_display_size)).check(matches(withText("Medium width")))
    onView(withId(R.id.screen_height_display_size)).check(matches(withText("Medium height")))
  }

  @Test
  fun onDevice_setDisplaySizeToCompactWidthAndMediumHeight() {
    onDevice().perform(setDisplaySize(WidthSizeClass.COMPACT, HeightSizeClass.MEDIUM))

    onView(withId(R.id.screen_width_display_size)).check(matches(withText("Compact width")))
    onView(withId(R.id.screen_height_display_size)).check(matches(withText("Medium height")))
  }

  @Test
  fun onDevice_setDisplaySizeToMediumWidthAndCompactHeight() {
    onDevice().perform(setDisplaySize(WidthSizeClass.MEDIUM, HeightSizeClass.COMPACT))

    onView(withId(R.id.screen_width_display_size)).check(matches(withText("Medium width")))
    onView(withId(R.id.screen_height_display_size)).check(matches(withText("Compact height")))
  }

  @Test
  fun onDevice_setDisplaySizeToExpandedWidthAndHeight() {
    onDevice().perform(setDisplaySize(WidthSizeClass.EXPANDED, HeightSizeClass.EXPANDED))

    onView(withId(R.id.screen_width_display_size)).check(matches(withText("Expanded width")))
    onView(withId(R.id.screen_height_display_size)).check(matches(withText("Expanded height")))
  }
}
