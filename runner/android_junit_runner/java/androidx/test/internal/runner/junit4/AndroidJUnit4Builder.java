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

package androidx.test.internal.runner.junit4;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import java.lang.reflect.Method;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * A {@link RunnerBuilder} that will build customized runners needed to handle the ability to skip
 * test execution if needed.
 */
public class AndroidJUnit4Builder extends JUnit4Builder {

  private static final String TAG = "AndroidJUnit4Builder";

  private final AndroidRunnerParams androidRunnerParams;
  private final boolean scanningPath;

  /**
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   */
  public AndroidJUnit4Builder(AndroidRunnerParams runnerParams, boolean scanningPath) {
    androidRunnerParams = runnerParams;
    this.scanningPath = scanningPath;
  }

  /**
   * @deprecated Provided temporarily for backwards compatibility. Use {@link
   *     AndroidJUnit4Builder#AndroidJUnit4Builder(AndroidRunnerParams, boolean) instead}.
   */
  @Deprecated
  public AndroidJUnit4Builder(AndroidRunnerParams runnerParams) {
    this(runnerParams, false);
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    try {
      // If scanning the path then make sure that it has at least one test method before
      // trying to run it.
      if (scanningPath && !hasTestMethods(testClass)) {
        return null;
      }

      return new AndroidJUnit4ClassRunner(testClass, androidRunnerParams);
    } catch (Throwable e) {
      // log error message including stack trace before throwing to help with debugging.
      Log.e(TAG, "Error constructing runner", e);
      throw e;
    }
  }

  private static boolean hasTestMethods(Class<?> testClass) {
    boolean hasTestMethods = false;
    try {
      for (Method testMethod : testClass.getMethods()) {
        if (testMethod.isAnnotationPresent(org.junit.Test.class)) {
          hasTestMethods = true;
          break;
        }
      }
    } catch (Throwable t) {
      // Defensively catch everything - Will throw runtime exception if it cannot
      // load methods.
      //
      // For earlier versions of Android (Pre-ICS), Dalvik might try to initialize a class
      // during getMethods(), fail to do so, hide the error and throw a NoSuchMethodException.
      // Since the java.lang.Class.getMethods does not declare such an exception, resort to a
      // generic catch all.
      // For ICS+, Dalvik will throw a NoClassDefFoundException.
      Log.w(TAG, String.format("%s in hasTestMethods for %s", t.toString(), testClass.getName()));
      return false;
    }

    return hasTestMethods;
  }
}
