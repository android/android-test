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

package androidx.test.espresso.device.controller

import androidx.test.espresso.EspressoException

/** An exception which indicates that an error occured during a device controller operation. */
class DeviceControllerOperationException : RuntimeException, EspressoException {
  constructor(description: String) : super(description)

  constructor(description: String, cause: Throwable) : super(description, cause)
}
