/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.device.dagger

import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import dagger.Component
import javax.inject.Singleton

/** Dagger component for device classes. */
@Singleton
@Component(modules = [DeviceControllerModule::class])
interface DeviceLayerComponent {
  fun actionContext(): ActionContext

  fun deviceController(): DeviceController
}
