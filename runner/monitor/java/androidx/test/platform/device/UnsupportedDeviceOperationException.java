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

package androidx.test.platform.device;

import androidx.test.platform.TestFrameworkException;

/**
 * An exception which indicates that the device selected does not support an operation called on it.
 */
public class UnsupportedDeviceOperationException extends RuntimeException
    implements TestFrameworkException {
  public UnsupportedDeviceOperationException(String description) {
    super(description);
  }

  public UnsupportedDeviceOperationException(String description, Throwable cause) {
    super(description, cause);
  }
}
