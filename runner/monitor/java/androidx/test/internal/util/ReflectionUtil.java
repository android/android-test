/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.internal.util;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Utility methods for invoking calls via reflection. */
public class ReflectionUtil {

  private static final String TAG = "ReflectionUtil";

  /**
   * Helper function for various runners who extend from this class to be able to reflectively
   * invoke the class#method that was passed in via "remoteMethod" runner argument.
   *
   * @param className the fully qualified class name to invoke (cannot be null).
   * @param methodName the method to invoke (cannot be null).
   */
  public static void reflectivelyInvokeRemoteMethod(
      final String className, final String methodName) {
    Checks.checkNotNull(className);
    Checks.checkNotNull(methodName);
    Log.i(TAG, "Attempting to reflectively call: " + methodName);
    try {
      Class<?> c = Class.forName(className);
      Method m = c.getDeclaredMethod(methodName);
      m.setAccessible(true);
      m.invoke(null);
    } catch (ClassNotFoundException
        | InvocationTargetException
        | IllegalAccessException
        | NoSuchMethodException e) {
      Log.e(TAG, "Reflective call failed: ", e);
    }
  }
}
