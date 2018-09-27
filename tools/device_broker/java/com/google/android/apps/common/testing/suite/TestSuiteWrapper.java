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

package com.google.android.apps.common.testing.suite;

import java.util.Enumeration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A TestSuite that delegates all its operations to another TestSuite. This class is meant to be
 * overridden to modify some behaviors.
 */
public abstract class TestSuiteWrapper extends TestSuite {

  /** the underlying TestSuite to which operations are delegated. */
  protected TestSuite suite;

  public TestSuiteWrapper(TestSuite suite) {
    this.suite = suite;
  }

  /** if this constructor is used then most methods will fail until {@link #suite} is set. */
  public TestSuiteWrapper() {}

  public TestSuite getSuite() {
    return suite;
  }

  @Override
  public void addTest(Test test) {
    suite.addTest(test);
  }

  @Override
  public void addTestSuite(Class<? extends TestCase> testClass) {
    suite.addTestSuite(testClass);
  }

  @Override
  public int countTestCases() {
    return suite.countTestCases();
  }

  @Override
  public String getName() {
    return suite.getName();
  }

  @Override
  public void run(TestResult result) {
    suite.run(result);
  }

  @Override
  public void runTest(Test test, TestResult result) {
    suite.runTest(test, result);
  }

  @Override
  public void setName(String name) {
    suite.setName(name);
  }

  @Override
  public Test testAt(int index) {
    return suite.testAt(index);
  }

  @Override
  public int testCount() {
    return suite.testCount();
  }

  @Override
  public Enumeration<Test> tests() {
    return suite.tests(); // TestSuite not generified
  }
}
