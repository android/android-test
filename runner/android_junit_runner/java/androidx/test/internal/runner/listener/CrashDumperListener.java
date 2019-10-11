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
package androidx.test.internal.runner.listener;

import android.util.Log;
import androidx.test.internal.platform.util.TestOutputEmitter;
import org.junit.runner.notification.Failure;

/**
 * A <a href="http://junit.org/javadoc/latest/org/junit/runner/notification/RunListener.html"><code>
 * RunListener</code></a> that dumps the stack trace information if failure occurred in the test.
 */
public class CrashDumperListener extends InstrumentationRunListener {

  private static final String TAG = CrashDumperListener.class.getSimpleName();

  @Override
  public void testFailure(Failure failure) throws Exception {
    Log.i(TAG, "Dumping the stack trace for " + failure);
    Throwable t = failure.getException();
    TestOutputEmitter.dumpStackTrace("stacktrace.pb", t);
    TestOutputEmitter.dumpStackTrace("stacktrace.txt", t);
  }
}
