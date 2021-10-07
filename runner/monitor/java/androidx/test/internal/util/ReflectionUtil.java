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
import androidx.annotation.RestrictTo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility methods for invoking calls via reflection.
 *
 * @deprecated use {@link androidx.test.platform.reflect.ReflectiveMethod} instead.
 * @hide
 */
@Deprecated
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // used by runner 1.4.0
public class ReflectionUtil {
  private static final String TAG = "ReflectionUtil";
  /** Data class for reflective method call parameters. */
  public static class ReflectionParams {
    final Class<?> type;
    final Object value;

    public ReflectionParams(Class<?> type, Object value) {
      this.type = type;
      this.value = value;
    }

    public static Class<?>[] getTypes(ReflectionParams[] params) {
      Class<?>[] types = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
        types[i] = params[i].type;
      }
      return types;
    }

    public static Object[] getValues(ReflectionParams[] params) {
      Object[] values = new Object[params.length];
      for (int i = 0; i < params.length; i++) {
        values[i] = params[i].value;
      }
      return values;
    }
  }
  /**
   * Thrown when there was a failure making a reflective call.
   *
   * @hide
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // used by runner 1.4.0
  public static class ReflectionException extends Exception {
    ReflectionException(Exception cause) {
      super("Reflective call failed", cause);
    }
  }
  /**
   * Reflectively call the specified static method.
   *
   * @param className the fully qualified name of the class
   * @param methodName the full name of the method
   * @param params the list of parameter types and values for parameters
   * @return the result from the method
   * @throws ReflectionException if the call could not be performed
   */
  public static Object callStaticMethod(
      String className, String methodName, ReflectionParams... params) throws ReflectionException {
    try {
      return callStaticMethod(Class.forName(className), methodName, params);
    } catch (ClassNotFoundException e) {
      throw new ReflectionException(e);
    }
  }
  /**
   * Reflectively call the specified static method.
   *
   * @param clazz the Class that defines the method
   * @param methodName the full name of the method
   * @param params the list of parameter types and values for parameters
   * @return the result from the method
   * @throws ReflectionException if the call could not be performed
   */
  public static Object callStaticMethod(
      Class<?> clazz, String methodName, ReflectionParams... params) throws ReflectionException {
    Log.d(TAG, "Attempting to reflectively call: " + methodName);
    try {
      Class<?>[] types = ReflectionParams.getTypes(params);
      Object[] values = ReflectionParams.getValues(params);
      Method m = clazz.getDeclaredMethod(methodName, types);
      m.setAccessible(true);
      return m.invoke(null, values);
    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
      throw new ReflectionException(e);
    }
  }
}
