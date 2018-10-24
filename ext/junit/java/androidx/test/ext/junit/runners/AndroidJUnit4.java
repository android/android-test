/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.ext.junit.runners;

import java.lang.reflect.InvocationTargetException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * A cross environment JUnit4 runner for Android tests.
 *
 * <p>This implementation will delegate to the appropriate runner based on the build-system provided
 * value. A custom runner can be provided by specifying the full class name in a
 * 'android.junit.runner' system property.
 *
 * <p>Usage {@code @RunWith(AndroidJUnit4.class)}
 */
public final class AndroidJUnit4 extends Runner implements Filterable, Sortable {

  private static final String TAG = "AndroidJUnit4";

  private final Runner delegate;

  public AndroidJUnit4(Class<?> klass) throws InitializationError {
    delegate = loadRunner(klass);
  }

  private static Runner loadRunner(Class<?> testClass) throws InitializationError {
    String runnerClassName = getRunnerClassName();
    return loadRunner(testClass, runnerClassName);
  }

  private static String getRunnerClassName() {
    String runnerClassName = System.getProperty("android.junit.runner", null);
    if (runnerClassName == null) {
      // TODO: remove this logic when nitrogen is hooked up to always pass this property
      if (System.getProperty("java.runtime.name").toLowerCase().contains("android")) {
        return "androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner";
      } else {
        return "org.robolectric.RobolectricTestRunner";
      }
    }
    return runnerClassName;
  }

  private static Runner loadRunner(Class<?> testClass, String className)
      throws InitializationError {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Runner> runnerClass = (Class<? extends Runner>) Class.forName(className);
      return runnerClass.getConstructor(Class.class).newInstance(testClass);
    } catch (ClassNotFoundException e) {
      throwInitializationError(className, e);
    } catch (NoSuchMethodException e) {
      throwInitializationError(className, e);
    } catch (IllegalAccessException e) {
      throwInitializationError(className, e);
    } catch (InstantiationException e) {
      throwInitializationError(className, e);
    } catch (InvocationTargetException e) {
      throwInitializationError(className, e);
    }
    throw new IllegalStateException("Should never reach here");
  }

  private static void throwInitializationError(String delegateRunner, Throwable cause)
      throws InitializationError {
    // wrap the cause in a RuntimeException with a more detailed error message
    throw new InitializationError(
        new RuntimeException(
            String.format(
                "Delegate runner '%s' for AndroidJUnit4 could not be loaded.", delegateRunner),
            cause));
  }

  @Override
  public Description getDescription() {
    return delegate.getDescription();
  }

  @Override
  public void run(RunNotifier runNotifier) {
    delegate.run(runNotifier);
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    ((Filterable) delegate).filter(filter);
  }

  @Override
  public void sort(Sorter sorter) {
    ((Sortable) delegate).sort(sorter);
  }
}
