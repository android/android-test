/*
 * Copyright (C) 2015 The Android Open Source Project
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
package androidx.test.internal.runner;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import androidx.test.internal.runner.listener.InstrumentationRunListener;
import androidx.test.internal.util.Checks;
import androidx.tracing.Trace;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * Class that given a Request containing tests to run, wires up the test listeners and actually
 * executes the test using upstream JUnit
 */
public final class TestExecutor {
  private static final String LOG_TAG = "TestExecutor";

  private final List<RunListener> listeners;
  private final Instrumentation instr;

  private TestExecutor(Builder builder) {
    listeners = Checks.checkNotNull(builder.listeners);
    instr = builder.instr;
  }

  /**
   * Execute the tests and report the test results.
   *
   * <p>If an error occurred during the test execution, the exception will be thrown, and it's the
   * caller's responsibility to handle the exception properly.
   */
  public Bundle execute(Request request) throws UnsupportedEncodingException {
    Trace.beginSection("execute tests");
    try {
      return execute(new JUnitCore(), request);
    } finally {
      Trace.endSection();
    }
  }

  Bundle execute(JUnitCore junitRunner, Request request) throws UnsupportedEncodingException {
    Bundle resultBundle = new Bundle();
    setUpListeners(junitRunner);
    Result junitResults = junitRunner.run(request);

    ByteArrayOutputStream summaryStream = new ByteArrayOutputStream();
    // create the stream used to output summary data to the user
    try (PrintStream summaryWriter = new PrintStream(summaryStream)) {
      reportRunEnded(listeners, summaryWriter, resultBundle, junitResults);
    }
    resultBundle.putString(
        Instrumentation.REPORT_KEY_STREAMRESULT,
        String.format("\n%s", summaryStream.toString("UTF_8")));
    return resultBundle;
  }

  /** Initialize listeners and add them to the JUnitCore runner */
  private void setUpListeners(JUnitCore testRunner) {
    for (RunListener listener : listeners) {
      Log.d(LOG_TAG, "Adding listener " + listener.getClass().getName());
      testRunner.addListener(listener);
      if (listener instanceof InstrumentationRunListener) {
        ((InstrumentationRunListener) listener).setInstrumentation(instr);
      }
    }
  }

  private void reportRunEnded(
      List<RunListener> listeners,
      PrintStream summaryWriter,
      Bundle resultBundle,
      Result jUnitResults) {
    for (RunListener listener : listeners) {
      if (listener instanceof InstrumentationRunListener) {
        ((InstrumentationRunListener) listener)
            .instrumentationRunFinished(summaryWriter, resultBundle, jUnitResults);
      }
    }
  }

  public static class Builder {
    private final List<RunListener> listeners = new ArrayList<RunListener>();
    private final Instrumentation instr;

    public Builder(Instrumentation instr) {
      this.instr = instr;
    }

    /**
     * Adds a custom RunListener
     *
     * @param listener the listener to add
     * @return the {@link androidx.test.internal.runner.TestExecutor.Builder}
     */
    public Builder addRunListener(RunListener listener) {
      listeners.add(listener);
      return this;
    }

    public TestExecutor build() {
      return new TestExecutor(this);
    }
  }
}
