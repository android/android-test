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
package androidx.test.internal.runner.listener;

import android.util.Log;
import androidx.tracing.Trace;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/** A JUnit RunListener that reports {@link android.os.Trace} info for each test case */
public class TraceRunListener extends RunListener {

  private Thread startedThread = null;

  @Override
  public void testStarted(Description description) throws Exception {
    startedThread = Thread.currentThread();
    String testClassName =
        description.getTestClass() != null ? description.getTestClass().getSimpleName() : "None";
    String methodName = description.getMethodName() != null ? description.getMethodName() : "None";
    Trace.beginSection(testClassName + "#" + methodName);
  }

  @Override
  public void testFinished(Description description) throws Exception {
    if (Thread.currentThread().equals(startedThread)) {
      Trace.endSection();
    } else {
      // Trace expects the begin/end section calls to be on same thread.
      // Listeners should always be invoked on test/instrumentation thread,
      // but log an error in case this changes.
      Log.e("TraceRunListener", "testFinished called on different thread than testStarted");
    }
    startedThread = null;
  }
}
