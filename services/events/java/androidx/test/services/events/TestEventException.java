/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.events;

import androidx.annotation.NonNull;

/** A checked {@link Exception} indicating that a test event operation failed. */
public class TestEventException extends Exception {
  /** Creates a new {@link TestEventException} with the given message. */
  public TestEventException(@NonNull String message) {
    super(message);
  }

  /** Creates a new {@link TestEventException} with the given message and original exception. */
  public TestEventException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
