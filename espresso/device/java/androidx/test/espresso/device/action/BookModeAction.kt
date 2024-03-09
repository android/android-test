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

import android.content.res.Configuration
import android.util.Log
import androidx.test.espresso.device.common.isTestDeviceAnEmulator
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.device.DeviceController
import androidx.window.layout.FoldingFeature

/** Action to set the test device to be folded with the hinge in the vertical position. */
internal class BookModeAction() :
  BaseSingleFoldDeviceAction(DeviceMode.BOOK, FoldingFeature.State.HALF_OPENED) {
  companion object {
    private val TAG = BookModeAction::class.java.simpleName
  }

  override fun perform(deviceController: DeviceController) {
    // TODO(b/203801760): Check current device mode and return if already in book mode.
    super.perform(deviceController)

    if (super.foldingFeatureOrientation == null) {
      throw DeviceControllerOperationException(
        "Failed to retrieve the orientation of the folding feature."
      )
    } else if (super.foldingFeatureOrientation != FoldingFeature.Orientation.VERTICAL) {
      Log.d(TAG, "FoldingFeature orientation needs to be rotated.")
      // TODO(b/296910911) On physical devices, changing screen orientation requires the device to
      // be in FLAT mode. Open the fold, update orientation, and then restore the half-folded state.
      Log.d(TAG, "Temporarily setting device to flat mode so that device can be rotated.")
      BaseSingleFoldDeviceAction(DeviceMode.FLAT, FoldingFeature.State.FLAT)
        .perform(deviceController)

      val orientationToRotateTo =
        if (
          InstrumentationRegistry.getInstrumentation()
            .getTargetContext()
            .getResources()
            .getConfiguration()
            .orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
          ScreenOrientation.LANDSCAPE
        } else {
          ScreenOrientation.PORTRAIT
        }
      ScreenOrientationAction(orientationToRotateTo).perform(deviceController)

      if (!isTestDeviceAnEmulator()) {
        super.perform(deviceController)
      }
    }
  }
}
