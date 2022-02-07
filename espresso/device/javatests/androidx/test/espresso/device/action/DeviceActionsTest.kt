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

package androidx.test.espresso.device.action

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeviceActionsTest {
  @Test
  fun setBookMode_returnsBookModeAction() {
    val action = setBookMode()

    assertTrue(action is BookModeAction)
  }

  @Test
  fun setFlatMode_returnsFlatModeAction() {
    val action = setFlatMode()

    assertTrue(action is FlatModeAction)
  }

  @Test
  fun setScreenOrientation_returnsScreenOrientationActionWithOrientationSet() {
    val action = setScreenOrientation(ScreenOrientation.PORTRAIT) as ScreenOrientationAction

    assertEquals(action.screenOrientation, ScreenOrientation.PORTRAIT)
  }
}
