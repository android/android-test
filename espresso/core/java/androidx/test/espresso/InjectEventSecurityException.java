/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso;

import androidx.test.internal.platform.util.TestOutputEmitter;

/**
 * An checked {@link Exception} indicating that event injection failed with a {@link
 * SecurityException}.
 */
public final class InjectEventSecurityException
    extends androidx.test.platform.ui.InjectEventSecurityException implements EspressoException {

  public InjectEventSecurityException(String message) {
    super(message);
    dumpThreads();
  }

  public InjectEventSecurityException(Throwable cause) {
    super(cause);
    dumpThreads();
  }

  public InjectEventSecurityException(String message, Throwable cause) {
    super(message, cause);
    dumpThreads();
  }

  private void dumpThreads() {
    TestOutputEmitter.dumpThreadStates("ThreadState-InjectEventSecurityException.txt");
  }
}
