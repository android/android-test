/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.runner.Result;

/**
 * A test <a href="http://junit.org/javadoc/latest/org/junit/runner/notification/RunListener.html">
 * <code>RunListener</code></a> that generates EMMA code coverage.
 */
public class CoverageListener extends InstrumentationRunListener {
  private static final String TAG = "CoverageListener";

  private String coverageFilePath;

  /**
   * If included in the status or final bundle sent to an IInstrumentationWatcher, this key
   * identifies the path to the generated code coverage file.
   */
  private static final String REPORT_KEY_COVERAGE_PATH = "coverageFilePath";
  // Default file name for code coverage
  private static final String DEFAULT_COVERAGE_FILE_NAME = "coverage.ec";

  private static final String EMMA_RUNTIME_CLASS = "com.vladium.emma.rt.RT";


  /**
   * Creates a {@link CoverageListener).
   *
   * @param customCoverageFilePath an optional user specified path for the coverage file
   *         If null, file will be generated in test app's file directory.
   */
  public CoverageListener(String customCoverageFilePath) {
    coverageFilePath = customCoverageFilePath;
  }

  @Override
  public void setInstrumentation(Instrumentation instr) {
    super.setInstrumentation(instr);
    if (coverageFilePath == null) {
      coverageFilePath =
          instr.getTargetContext().getFilesDir().getAbsolutePath()
              + File.separator
              + DEFAULT_COVERAGE_FILE_NAME;
    }
  }

  @Override
  public void instrumentationRunFinished(PrintStream writer, Bundle results, Result junitResults) {
    generateCoverageReport(writer, results);
  }

  private void generateCoverageReport(PrintStream writer, Bundle results) {
    // use reflection to call emma dump coverage method, to avoid
    // always statically compiling against emma jar
    java.io.File coverageFile = new java.io.File(coverageFilePath);
    try {
      // In case the target and instrumentation contexts are different,
      // prioritize coverage from the target context. If the target
      // context classloader implements a delegate-first strategy
      // for org.jacoco.agent.rt and com.vladium.emma.rt, it will still
      // be possible to get coverage from either, or both, contexts.
      Class<?> emmaRTClass;
      try {
        emmaRTClass =
            Class.forName(
                EMMA_RUNTIME_CLASS, true, getInstrumentation().getTargetContext().getClassLoader());
      } catch (ClassNotFoundException e) {
        emmaRTClass =
            Class.forName(
                EMMA_RUNTIME_CLASS, true, getInstrumentation().getContext().getClassLoader());
        String msg = "Generating coverage for alternate test context.";
        Log.w(TAG, msg);
        writer.format("\nWarning: %s", msg);
      }

      Method dumpCoverageMethod =
          emmaRTClass.getMethod(
              "dumpCoverageData", coverageFile.getClass(), boolean.class, boolean.class);

      dumpCoverageMethod.invoke(null, coverageFile, false, false);

      // output path to generated coverage file so it can be parsed by a test harness if
      // needed
      results.putString(REPORT_KEY_COVERAGE_PATH, coverageFilePath);
      // also output a more user friendly msg
      writer.format("\nGenerated code coverage data to %s", coverageFilePath);
    } catch (ClassNotFoundException e) {
      reportEmmaError(writer, "Is Emma/JaCoCo jar on classpath?", e);
    } catch (SecurityException e) {
      reportEmmaError(writer, e);
    } catch (NoSuchMethodException e) {
      reportEmmaError(writer, e);
    } catch (IllegalArgumentException e) {
      reportEmmaError(writer, e);
    } catch (IllegalAccessException e) {
      reportEmmaError(writer, e);
    } catch (InvocationTargetException e) {
      reportEmmaError(writer, e);
    }
  }

  private void reportEmmaError(PrintStream writer, Exception e) {
    reportEmmaError(writer, "", e);
  }

  private void reportEmmaError(PrintStream writer, String hint, Exception e) {
    String msg = "Failed to generate Emma/JaCoCo coverage. " + hint;
    Log.e(TAG, msg, e);
    writer.format("\nError: %s", msg);
  }
}
