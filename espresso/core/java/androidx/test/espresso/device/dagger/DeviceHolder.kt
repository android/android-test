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

package androidx.test.espresso.device.dagger

import java.util.concurrent.atomic.AtomicReference

/** Holds Espresso's device graph. */
class DeviceHolder {
  companion object {
    private val instance = AtomicReference<DeviceHolder>(null)

    @JvmStatic
    fun deviceLayer(): DeviceLayerComponent {
      var instanceRef: DeviceHolder? = instance.get()
      if (null == instanceRef) {
        instanceRef = DeviceHolder(DaggerDeviceLayerComponent.create())
        if (instance.compareAndSet(null, instanceRef)) {
          return instanceRef.component
        } else {
          return instance.get().component
        }
      } else {
        return instanceRef.component
      }
    }
  }

  private val component: DeviceLayerComponent

  private constructor(component: DeviceLayerComponent) {
    this.component = component
  }
}
