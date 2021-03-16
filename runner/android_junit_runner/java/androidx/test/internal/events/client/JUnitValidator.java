/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.internal.events.client;

import androidx.annotation.NonNull;
import org.junit.runner.Description;

/** JUnit has bugs that can lead to generating invalid data. */
final class JUnitValidator {
  private static final String INIT_ERROR_METHOD_NAME = "initializationError";

  private JUnitValidator() {}

  /**
   * If JUnit fails to create a class it still creates a Description for it in the form of
   * foo.bar.EmptyTestClass.initializationError.
   */
  static boolean validateDescription(@NonNull Description description) {
    if (INIT_ERROR_METHOD_NAME.equals(description.getMethodName())) {
      return false;
    }

    return true;
  }
}
