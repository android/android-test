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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Class that given a Request containing tests to run, wires up the test listeners and actually
 * executes the test using upstream JUnit
 */
public final class TestExecutor {
  private static final String LOG_TAG = "TestExecutor";

  private final List<RunListener> mListeners;
  private final Instrumentation mInstr;

  private TestExecutor(Builder builder) {
    mListeners = Checks.checkNotNull(builder.mListeners);
    mInstr = builder.mInstr;
  }

  /** Execute the tests */
  public Bundle execute(Request request) {
    Bundle resultBundle = new Bundle();
    Result junitResults = new Result();
    try {
      JUnitCore testRunner = new JUnitCore();
      setUpListeners(testRunner);
      junitResults = testRunner.run(request);
    } catch (Throwable t) {
      final String msg = "Fatal exception when running tests";
      Log.e(LOG_TAG, msg, t);
      junitResults.getFailures().add(new Failure(Description.createSuiteDescription(msg), t));
    } finally {
      ByteArrayOutputStream summaryStream = new ByteArrayOutputStream();
      // create the stream used to output summary data to the user
      PrintStream summaryWriter = new PrintStream(summaryStream);
      reportRunEnded(mListeners, summaryWriter, resultBundle, junitResults);
      summaryWriter.close();
      resultBundle.putString(
          Instrumentation.REPORT_KEY_STREAMRESULT, String.format("\n%s", summaryStream.toString()));
    }
    return resultBundle;
  }

  /** Initialize listeners and add them to the JUnitCore runner */
  private void setUpListeners(JUnitCore testRunner) {
    for (RunListener listener : mListeners) {
      Log.d(LOG_TAG, "Adding listener " + listener.getClass().getName());
      testRunner.addListener(listener);
      if (listener instanceof InstrumentationRunListener) {
        ((InstrumentationRunListener) listener).setInstrumentation(mInstr);
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
    private final List<RunListener> mListeners = new ArrayList<RunListener>();
    private final Instrumentation mInstr;

    public Builder(Instrumentation instr) {
      mInstr = instr;
    }

    /**
     * Adds a custom RunListener
     *
     * @param listener the listener to add
     * @return the {@link androidx.test.internal.runner.TestExecutor.Builder}
     */
    public Builder addRunListener(RunListener listener) {
      mListeners.add(listener);
      return this;
    }

    public TestExecutor build() {
      return new TestExecutor(this);
    }
  }
}
