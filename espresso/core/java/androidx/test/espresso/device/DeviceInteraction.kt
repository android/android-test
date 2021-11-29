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

import androidx.test.annotation.ExperimentalTestApi
import androidx.test.espresso.device.action.DeviceAction
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import androidx.test.internal.util.Checks.checkNotMainThread
import javax.inject.Inject

/**
 * API surface for performing device-centric operations.
 *
 * <p>This API is experimental and subject to change.
 */
@ExperimentalTestApi
class DeviceInteraction
@Inject
constructor(private val context: ActionContext, private val deviceController: DeviceController) {

  /**
   * Performs the given action on the test device. This method should not be called on the main
   * thread.
   *
   * @param action the DeviceAction to execute.
   * @return this interaction for further perform/verification calls.
   * @throws IllegalStateException when being invoked on the main thread.
   */
  fun perform(action: DeviceAction): DeviceInteraction {
    checkNotMainThread()
    action.perform(context, deviceController)
    return this
  }
}
