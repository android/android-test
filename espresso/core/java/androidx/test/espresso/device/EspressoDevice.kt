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
import androidx.test.espresso.device.dagger.DeviceHolder
import androidx.test.espresso.device.dagger.DeviceLayerComponent
import androidx.test.internal.util.Checks.checkNotMainThread

/** Entry point for device centric operations */
class EspressoDevice private constructor() {
  companion object {
    private val BASE: DeviceLayerComponent = DeviceHolder.deviceLayer()

    /**
     * Starts a {@link DeviceInteraction} fluent API call. This method is used to invoke operations
     * that are device-centric in scope. This method should not be called on the main thread.
     * @throws IllegalStateException when being invoked on the main thread.
     *
     * <p>This API is experimental and subject to change or removal.
     */
    @ExperimentalTestApi
    fun onDevice(): DeviceInteraction {
      checkNotMainThread()
      return DeviceInteraction(BASE.actionContext(), BASE.deviceController())
    }
  }
}
