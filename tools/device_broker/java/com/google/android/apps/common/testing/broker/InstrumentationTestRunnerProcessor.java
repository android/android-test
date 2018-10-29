/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.broker.ExecutedTest.Status;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.LineProcessor;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link LineProcessor} for instrumentation test runner output.
 *
 * <p>This {@link LineProcessor} works specifically for the output of the following command: {@code
 * adb shell am instrument -w -r ...}. See: https://developer.android.com/studio/test/command-line
 * for more information on instrumentation testing with adb.
 *
 * <p>Required Flags:
 *
 * <ul>
 *   <li>{@code -w}: Forces {@code am instrument} to wait for the instrumentation to finish before
 *       terminating.
 *   <li>{@code -r}: Enables verbose status logging, allowing the LineProcessor to gather more
 *       information about the test run.
 * </ul>
 */
public class InstrumentationTestRunnerProcessor implements LineProcessor<List<ExecutedTest>> {
  public static final String INSTRUMENTATION_CODE = "INSTRUMENTATION_CODE:";
  public static final String INSTRUMENTATION_STATUS = "INSTRUMENTATION_STATUS:";
  public static final String INSTRUMENTATION_PREFIX = "INSTRUMENTATION_";
  public static final String INSTRUMENTATION_RESULT = "INSTRUMENTATION_RESULT:";
  public static final String RESULT_STREAM = "INSTRUMENTATION_RESULT: stream=";
  public static final String STATUS_CLASS = "INSTRUMENTATION_STATUS: class=";
  public static final String STATUS_CODE = "INSTRUMENTATION_STATUS_CODE: ";
  public static final String STATUS_CURRENT = "INSTRUMENTATION_STATUS: current=";
  public static final String STATUS_ID = "INSTRUMENTATION_STATUS: id=";
  public static final String STATUS_NUMTESTS = "INSTRUMENTATION_STATUS: numtests=";
  public static final String STATUS_STACK = "INSTRUMENTATION_STATUS: stack=";
  public static final String STATUS_STREAM = "INSTRUMENTATION_STATUS: stream=";
  public static final String STATUS_TEST = "INSTRUMENTATION_STATUS: test=";
  private ExecutedTest.Builder currentTest = null;
  private final List<ExecutedTest> executedTests = Lists.newArrayList();
  private boolean isInResultsStream = false;
  private boolean isInStack = false;
  private boolean isInStatusStream = false;
  private static final Logger logger =
      Logger.getLogger(InstrumentationTestRunnerProcessor.class.getName());
  private final EventBus eventBus;

  public InstrumentationTestRunnerProcessor(EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
  }

  @Override
  public List<ExecutedTest> getResult() {
    return executedTests;
  }

  @Override
  public boolean processLine(String line) {
    if (null == currentTest) {
      currentTest = ExecutedTest.builder();
    }
    if ((isInStack || isInStatusStream || isInResultsStream) &&
        line.startsWith(INSTRUMENTATION_PREFIX)) {
      if (isInStack) {
        isInStack = false;
      }
      if (isInStatusStream) {
        isInStatusStream = false;
      }
      if (isInResultsStream) {
        isInResultsStream = false;
      }
    }

    if (line.startsWith(STATUS_ID)) {
      currentTest.setId(line.replace(STATUS_ID, ""));
      currentTest.appendAllLines(line);
      return true;
    }

    currentTest.appendAllLines(line);

    if (isInStack || line.startsWith(STATUS_STACK)) {
      currentTest.appendStackTrace(line.replace(STATUS_STACK, ""));
      isInStack = true;
      return true;
    }

    if (isInStatusStream || line.startsWith(STATUS_STREAM)) {
      currentTest.appendStatusStream(line.replace(STATUS_STREAM, ""));
      isInStatusStream = true;
      return true;
    }

    if (isInResultsStream || line.startsWith(RESULT_STREAM)) {
      currentTest.appendResultStream(line.replace(RESULT_STREAM, ""));
      isInResultsStream = true;
      return true;
    }

    if (line.startsWith(INSTRUMENTATION_RESULT)) {
      currentTest.appendResultStream(line.replace(INSTRUMENTATION_RESULT, ""));
      return true;
    }

    if (line.startsWith(STATUS_CODE)) {
      String statusCode = line.replace(STATUS_CODE, "").trim();
      int statusInt = Integer.parseInt(statusCode);

      try {
        switch (statusInt) {
          case 1:
            currentTest.setStatus(Status.STARTED);
            onTestStart(currentTest.build());
            break;
          case 0:
            currentTest.setStatus(Status.PASSED);
            break;
          case -1:
            currentTest.setStatus(Status.ERROR);
            break;
          case -2:
            currentTest.setStatus(Status.FAILED);
            break;
          case -4:
            currentTest.setStatus(Status.ASSUMPTION_FAILURE);
            break;
          default:
            throw new IllegalArgumentException(
                String.format("Illegal test instrumentation code: \"%s\"", statusCode));
        }
      } finally {
        onTestFinished(currentTest.build());
        currentTest = null;
      }

      return true;
    }

    if (line.startsWith(STATUS_NUMTESTS)) {
      currentTest.setNumTests(line.replace(STATUS_NUMTESTS, ""));
      return true;
    }

    if (line.startsWith(STATUS_CLASS)) {
      currentTest.setTestClass(line.replace(STATUS_CLASS, ""));
      return true;
    }

    if (line.startsWith(STATUS_CURRENT)) {
      currentTest.setCurrentTest(line.replace(STATUS_CURRENT, ""));
      return true;
    }

    if (line.startsWith(STATUS_TEST)) {
      currentTest.setTestMethod(line.replace(STATUS_TEST, ""));
      return true;
    }

    if (line.startsWith(INSTRUMENTATION_CODE)) {
      return true;
    }

    if (line.contains("Killed")) {
      // shh: b/73514868
      // We kill the shell executor to ensure that it doesn't end up hanging in atexit.
      return true;
    }
    logger.severe("Line not handled by the parser:\n" + line);

    return true;
  }

  private void onTestFinished(ExecutedTest executedTest) {
    executedTests.add(executedTest);
    eventBus.post(executedTest);
  }

  private void onTestStart(ExecutedTest executedTest) {
    eventBus.post(executedTest);
  }

  /**
   * Registers all subscriber methods on object to receive events from the internal EventBus. These
   * objects will need to receive ExecutedTest objects as part of their @Subscribe methods. see:
   * https://google.github.io/guava/releases/22.0/api/docs/com/google/common/eventbus/EventBus.html
   * for information on EventBus.
   *
   * @param object - an object to subscribed to the internal EventBus.
   */
  public void register(Object object) {
    eventBus.register(object);
  }

  /**
   * Unregisters all subscriber methods on a registered object from the internal EventBus. see:
   * https://google.github.io/guava/releases/22.0/api/docs/com/google/common/eventbus/EventBus.html
   * for information on EventBus.
   *
   * @param object - the object that has subscribed to the internal EventBus.
   */
  public void unregister(Object object) {
    eventBus.unregister(object);
  }
}
