/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.runner.junit3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import junit.extensions.TestDecorator;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

// this is a virtually identical copy of org.junit.internal.runner.JUnit38ClassRunner
// from junit 4.12-snapshot. Copied here so androidx.test can work
// with JUnit 4.10+ and pick up the annotation filtering fixes made in 4.12.
public class JUnit38ClassRunner extends Runner implements Filterable, Sortable {
  private static final class OldTestClassAdaptingListener implements TestListener {
    private final RunNotifier fNotifier;
    // android-changed - cache description and test - since building description can be
    // slightly memory intensive
    private Test currentTest = null;
    private Description description = null;
    // end android-changed

    private OldTestClassAdaptingListener(RunNotifier notifier) {
      fNotifier = notifier;
    }

    @Override
    public void endTest(Test test) {
      fNotifier.fireTestFinished(asDescription(test));
    }

    @Override
    public void startTest(Test test) {
      fNotifier.fireTestStarted(asDescription(test));
    }

    // Implement junit.framework.TestListener
    @Override
    public void addError(Test test, Throwable t) {
      Failure failure = new Failure(asDescription(test), t);
      fNotifier.fireTestFailure(failure);
    }

    private Description asDescription(Test test) {
      // android-changed - cache description
      if (currentTest != null && currentTest.equals(test) && description != null) {
        return description;
      }
      currentTest = test;
      if (test instanceof Describable) {
        Describable facade = (Describable) test;
        description = facade.getDescription();
      } else if (test instanceof TestCase) {
        description = makeDescription(test);
      } else {
        description = Description.createTestDescription(getEffectiveClass(test), test.toString());
      }
      return description;
      // end android-changed
    }

    private Class<? extends Test> getEffectiveClass(Test test) {
      return test.getClass();
    }

    @Override
    public void addFailure(Test test, AssertionFailedError t) {
      addError(test, t);
    }
  }

  private volatile Test fTest;

  public JUnit38ClassRunner(Class<?> klass) {
    this(new TestSuite(klass.asSubclass(TestCase.class)));
  }

  public JUnit38ClassRunner(Test test) {
    super();
    setTest(test);
  }

  @Override
  public void run(RunNotifier notifier) {
    TestResult result = new TestResult();
    result.addListener(createAdaptingListener(notifier));
    getTest().run(result);
  }

  public TestListener createAdaptingListener(final RunNotifier notifier) {
    return new OldTestClassAdaptingListener(notifier);
  }

  @Override
  public Description getDescription() {
    return makeDescription(getTest());
  }

  // android-changed - change from private so it can be accessed when filtering AndroidTestSuites
  static Description makeDescription(Test test) {
    if (test instanceof TestCase) {
      TestCase tc = (TestCase) test;
      return Description.createTestDescription(tc.getClass(), tc.getName(), getAnnotations(tc));
    } else if (test instanceof TestSuite) {
      TestSuite ts = (TestSuite) test;
      String name = ts.getName() == null ? createSuiteDescription(ts) : ts.getName();
      Description description = Description.createSuiteDescription(name);
      int n = ts.testCount();
      for (int i = 0; i < n; i++) {
        Description made = makeDescription(ts.testAt(i));
        description.addChild(made);
      }
      return description;
    } else if (test instanceof Describable) {
      Describable adapter = (Describable) test;
      return adapter.getDescription();
    } else if (test instanceof TestDecorator) {
      TestDecorator decorator = (TestDecorator) test;
      return makeDescription(decorator.getTest());
    } else {
      // This is the best we can do in this case
      return Description.createSuiteDescription(test.getClass());
    }
  }

  /**
   * Get the annotations associated with given TestCase.
   *
   * @param test the TestCase.
   */
  private static Annotation[] getAnnotations(TestCase test) {
    try {
      Method m = test.getClass().getMethod(test.getName());
      return m.getDeclaredAnnotations();
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    return new Annotation[0];
  }

  private static String createSuiteDescription(TestSuite ts) {
    int count = ts.countTestCases();
    String example = count == 0 ? "" : String.format(" [example: %s]", ts.testAt(0));
    return String.format("TestSuite with %s tests%s", count, example);
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    if (getTest() instanceof Filterable) {
      Filterable adapter = (Filterable) getTest();
      adapter.filter(filter);
    } else if (getTest() instanceof TestSuite) {
      TestSuite suite = (TestSuite) getTest();
      TestSuite filtered = new TestSuite(suite.getName());
      int n = suite.testCount();
      for (int i = 0; i < n; i++) {
        Test test = suite.testAt(i);
        if (filter.shouldRun(makeDescription(test))) {
          filtered.addTest(test);
        }
      }
      setTest(filtered);
      if (filtered.testCount() == 0) {
        throw new NoTestsRemainException();
      }
    }
  }

  @Override
  public void sort(Sorter sorter) {
    if (getTest() instanceof Sortable) {
      Sortable adapter = (Sortable) getTest();
      adapter.sort(sorter);
    }
  }

  private void setTest(Test test) {
    fTest = test;
  }

  private Test getTest() {
    return fTest;
  }
}
