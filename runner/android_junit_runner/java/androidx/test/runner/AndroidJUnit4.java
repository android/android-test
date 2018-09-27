/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.runner;

import android.util.Log;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.internal.util.AndroidRunnerParams;
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
 * Aliases the current default Android JUnit 4 class runner, for future-proofing. If future versions
 * of JUnit change the default Runner class, they will also change the definition of this class.
 * Developers wanting to explicitly tag a class as an Android JUnit 4 class should use
 * {@code @RunWith(AndroidJUnit4.class)}
 *
 * @deprecated use androidx.test.ext.junit.runners.AndroidJUnit4 instead.
 */
@Deprecated
public final class AndroidJUnit4 extends Runner implements Filterable, Sortable {

  private static final String TAG = "AndroidJUnit4";

  private final Runner delegate;

  /** Constructs a new instance of the default runner */
  public AndroidJUnit4(Class<?> klass, AndroidRunnerParams runnerParams)
      throws InitializationError {
    // this is expected to be called when in Android environment.
    delegate = new AndroidJUnit4ClassRunner(klass, runnerParams);
  }

  /**
   * Used when executed with standard junit runner. Will attempt to delegate to
   * RobolectricTestRunner or delegate provided by android.junit.runner system property.
   */
  public AndroidJUnit4(Class<?> klass) throws InitializationError {
    delegate = loadRunner(klass);
  }

  private static Runner loadRunner(Class<?> testClass) throws InitializationError {
    String runnerClassName =
        System.getProperty("android.junit.runner", "org.robolectric.RobolectricTestRunner");
    return loadRunner(testClass, runnerClassName);
  }

  private static Runner loadRunner(Class<?> testClass, String className)
      throws InitializationError {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Runner> runnerClass = (Class<? extends Runner>) Class.forName(className);
      return runnerClass.getConstructor(Class.class).newInstance(testClass);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, className + " could not be loaded", e);
      // fall through
    } catch (NoSuchMethodException e) {
      Log.e(TAG, className + " could not be loaded", e);
      // fall through
    } catch (IllegalAccessException e) {
      Log.e(TAG, className + " could not be loaded", e);
      // fall through
    } catch (InstantiationException e) {
      Log.e(TAG, className + " could not be loaded", e);
      // fall through
    } catch (InvocationTargetException e) {
      Log.e(TAG, className + " could not be loaded", e);
      // fall through
    }
    throw new InitializationError(
        String.format(
            "Attempted to use AndroidJUnit4 with standard JUnit runner and delegate runner '%s'"
            + "could not be loaded. Check your build configuration.", className));
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
