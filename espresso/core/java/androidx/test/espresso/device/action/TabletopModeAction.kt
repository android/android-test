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

import android.app.Activity
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.util.Consumer
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.espresso.device.util.getResumedActivityOrNull
import androidx.test.platform.device.DeviceController
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor

/** Action to set the test device to be folded with the hinge in a horizontal position. */
internal class TabletopModeAction() : DeviceAction {
  companion object {
    private val TAG = "TabletopModeAction"

    private class WindowLayoutInfoConsumer(
      private val latch: CountDownLatch,
      private val activity: Activity,
      private val windowInfoTrackerCallbackAdapter: WindowInfoTrackerCallbackAdapter,
      private val context: ActionContext,
      private val deviceController: DeviceController
    ) : Consumer<WindowLayoutInfo> {
      override fun accept(windowLayoutInfo: WindowLayoutInfo) {
        Log.d(TAG, "windowLayoutInfo: $windowLayoutInfo")
        windowLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().forEach {
          if (it.state == FoldingFeature.State.HALF_OPENED) {
            if (it.orientation == FoldingFeature.Orientation.HORIZONTAL) {
              Log.d(TAG, "Device is in tabletop mode")
              windowInfoTrackerCallbackAdapter.removeWindowLayoutInfoListener(this)
              latch.countDown()
            } else {
              Log.d(TAG, "Device is half open with a vertical hinge")
              // Folding feature's orientation is vertical, rotate the device to the opposite
              // orientation.
              val screenOrientation =
                if (activity.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
                ) {
                  ScreenOrientation.PORTRAIT
                } else {
                  ScreenOrientation.LANDSCAPE
                }
              deviceController.setScreenOrientation(screenOrientation.orientation)
            }
          }
        }
      }
    }
  }

  override fun perform(context: ActionContext, deviceController: DeviceController) {
    // TODO(b/203801760): Check current device mode and return if already in tabletop mode.
    val activity = getResumedActivityOrNull() ?: return
    Log.d(TAG, "activity: $activity")
    val handler: Handler = Handler(Looper.getMainLooper())
    val executor: Executor = Executor { command -> handler.post(command) }
    val latch: CountDownLatch = CountDownLatch(1)
    val windowInfoTrackerCallbackAdapter =
      WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(activity))
    val windowLayoutInfoConsumer: Consumer<WindowLayoutInfo> =
      WindowLayoutInfoConsumer(
        latch,
        activity,
        windowInfoTrackerCallbackAdapter,
        context,
        deviceController
      )

    Log.d(TAG, "Adding WindowLayoutInfoListener")
    windowInfoTrackerCallbackAdapter.addWindowLayoutInfoListener(
      activity,
      executor,
      windowLayoutInfoConsumer
    )

    deviceController.setDeviceMode(DeviceMode.TABLETOP.mode)
    latch.await()
  }
}
