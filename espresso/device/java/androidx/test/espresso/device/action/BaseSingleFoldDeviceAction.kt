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

package androidx.test.espresso.device.action

import android.util.Log
import androidx.test.espresso.device.common.executeShellCommand
import androidx.test.espresso.device.common.getMapOfDeviceStateNamesToIdentifiers
import androidx.test.espresso.device.common.getResumedActivityOrNull
import androidx.test.espresso.device.common.isRobolectricTest
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceControllerOperationException
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.platform.device.DeviceController
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Action to set the test device to the provided device mode. */
internal open class BaseSingleFoldDeviceAction(
  private val deviceMode: DeviceMode,
  private val foldingFeatureState: FoldingFeature.State?,
  private val mainExecutor: Executor
) : DeviceAction {
  protected var foldingFeatureOrientation: FoldingFeature.Orientation? = null

  companion object {
    private val TAG = BaseSingleFoldDeviceAction::class.java.simpleName
  }

  @OptIn(androidx.window.core.ExperimentalWindowApi::class)
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    if (isRobolectricTest()) {
      deviceController.setDeviceMode(deviceMode.mode)
      return
    }
    val activity =
      getResumedActivityOrNull()
        ?: throw DeviceControllerOperationException(
          "Unable to set device mode because there are no activities in the resumed stage."
        )
    val latch: CountDownLatch = CountDownLatch(1)
    val windowInfoTracker = WindowInfoTracker.getOrCreate(activity)

    CoroutineScope(mainExecutor.asCoroutineDispatcher()).launch {
      windowInfoTracker.windowLayoutInfo(activity).distinctUntilChanged().collect {
        windowLayoutInfo: WindowLayoutInfo ->
        val foldingFeatures = windowLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
        if (deviceMode == DeviceMode.CLOSED && foldingFeatures.isEmpty()) {
          // When a device is in closed mode, WindowLayoutInfo returns an empty list of folding
          // features. If the device actually has no folding features and cannot be set to closed
          // mode (ie a non-foldable emulator), deviceController.setDeviceMode(DeviceMode.CLOSED)
          // will throw a DeviceControllerOperationException.
          Log.d(TAG, "Device is in the closed state.")
          latch.countDown()
        } else if (foldingFeatures.size != 1) {
          // TODO(b/218872245) It is currently possible that some devices will emit an empty list
          // before emitting a list of FoldingFeatures. Throw a DeviceControllerOperationException
          // once this issue is fixed.
          Log.w(
            TAG,
            "This device mode is only supported on devices with a single folding feature. " +
              "${foldingFeatures.size} were found."
          )
        } else {
          val foldingFeature = foldingFeatures.single()
          if (foldingFeatureState == foldingFeature.state) {
            Log.d(
              TAG,
              "FoldingFeature is in $foldingFeatureState state. WindowLayoutInfo: $windowLayoutInfo."
            )
            foldingFeatureOrientation = foldingFeature.orientation
            latch.countDown()
          }
        }
      }
    }

    deviceController.setDeviceMode(deviceMode.mode)
    latch.await(5, TimeUnit.SECONDS)

    if (latch.getCount() != 0L) {
      // If WindowLayoutInfo is not updated to the requested state within five seconds, check device
      // state and throw DeviceControllerOperationException if the device is not in the requested
      // state.
      val finalDeviceStateIdentifier = executeShellCommand("cmd device_state print-state").trim()
      val mapOfDeviceStateNamesToIdentifiers = getMapOfDeviceStateNamesToIdentifiers()
      val expectedDeviceState =
        when (foldingFeatureState) {
          FoldingFeature.State.FLAT -> "OPENED"
          FoldingFeature.State.HALF_OPENED -> "HALF_OPENED"
          else -> "CLOSED"
        }
      val currentDeviceState =
        mapOfDeviceStateNamesToIdentifiers
          .filterValues { finalDeviceStateIdentifier.trim().toString() == it }
          .keys
          .first()
      if (expectedDeviceState != currentDeviceState) {
        throw DeviceControllerOperationException(
          "Device could not be set to ${deviceMode} mode. Expected device state is ${expectedDeviceState}, actual state is ${currentDeviceState}."
        )
      }
    }
  }
}
