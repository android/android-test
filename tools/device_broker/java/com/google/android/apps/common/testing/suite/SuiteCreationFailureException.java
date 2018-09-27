/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.apps.common.testing.suite;

import javax.annotation.Nullable;

/** An exception class for test suite creation failure. */
public class SuiteCreationFailureException extends RuntimeException {

  private boolean isInfrastructureError;
  private String message;
  private Exception cause;

  public SuiteCreationFailureException(
      boolean isInfrastructureError, String message, Exception cause) {
    super(message);

    this.isInfrastructureError = isInfrastructureError;
    this.message = message;
    this.cause = cause;
  }

  public boolean isInfrastructureError() {
    return isInfrastructureError;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  @Nullable
  public Exception getCause() {
    return cause;
  }
}
