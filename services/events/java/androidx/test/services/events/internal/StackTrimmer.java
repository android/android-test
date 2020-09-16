/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.test.services.events.internal;

import androidx.annotation.VisibleForTesting;
import android.util.Log;
import org.junit.runner.notification.Failure;

/** A utility for JUnit failure stack traces */
public final class StackTrimmer {

  private static final String TAG = "StackTrimmer";

  @VisibleForTesting static final int MAX_TRACE_SIZE = 64 * 1024;

  private StackTrimmer() {}

  /**
   * Returns the stack trace, trimming to remove frames from the test runner, and truncating if its
   * too large.
   */
  public static String getTrimmedStackTrace(Failure failure) {
    // TODO(b/128614857): switch to JUnit 4.13 Failure.getTrimmedTrace once its available
    String trace = Throwables.getTrimmedStackTrace(failure.getException());
    if (trace.length() > MAX_TRACE_SIZE) {
      // Since AJUR needs to report failures back to AM via a binder IPC, we need to make sure that
      // we don't exceed the Binder transaction limit - which is 1MB per process.
      Log.w(
          TAG,
          String.format("Stack trace too long, trimmed to first %s characters.", MAX_TRACE_SIZE));
      trace = trace.substring(0, MAX_TRACE_SIZE) + "\n";
    }
    return trace;
  }
}
