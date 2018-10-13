/*
 * Copyright (C) 2014 The Android Open Source Project
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
package androidx.test.internal.runner.junit3;

import java.util.Enumeration;
import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;

/** A {@link TestResult} that delegates all calls to another {@link TestResult}. */
class DelegatingTestResult extends TestResult {

  private TestResult wrappedResult;

  DelegatingTestResult(TestResult wrappedResult) {
    this.wrappedResult = wrappedResult;
  }

  @Override
  public void addError(Test test, Throwable t) {
    wrappedResult.addError(test, t);
  }

  @Override
  public void addFailure(Test test, AssertionFailedError t) {
    wrappedResult.addFailure(test, t);
  }

  @Override
  public void addListener(TestListener listener) {
    wrappedResult.addListener(listener);
  }

  @Override
  public void removeListener(TestListener listener) {
    wrappedResult.removeListener(listener);
  }

  @Override
  public void endTest(Test test) {
    wrappedResult.endTest(test);
  }

  @Override
  public int errorCount() {
    return wrappedResult.errorCount();
  }

  @Override
  public Enumeration<TestFailure> errors() {
    return wrappedResult.errors();
  }

  @Override
  public int failureCount() {
    return wrappedResult.failureCount();
  }

  @Override
  public Enumeration<TestFailure> failures() {
    return wrappedResult.failures();
  }

  @Override
  public int runCount() {
    return wrappedResult.runCount();
  }

  @Override
  public void runProtected(final Test test, Protectable p) {
    wrappedResult.runProtected(test, p);
  }

  @Override
  public boolean shouldStop() {
    return wrappedResult.shouldStop();
  }

  @Override
  public void startTest(Test test) {
    wrappedResult.startTest(test);
  }

  @Override
  public void stop() {
    wrappedResult.stop();
  }

  @Override
  public boolean wasSuccessful() {
    return wrappedResult.wasSuccessful();
  }
}
