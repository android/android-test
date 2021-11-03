/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.internal.platform.reflect;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class for making more performant reflection method invocations.
 *
 * <p>Lazy initializes and caches Method object to attempt to reduce reflection overhead.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class ReflectiveMethod<T> {
  private final String className;
  private final String methodName;
  private final Class<?>[] paramTypes;

  // lazy init
  private boolean initialized = false;
  private Method method;

  /**
   * Creates a ReflectiveMethod.
   *
   * @param className the fully qualified class name that defines the method
   * @param methodName the method name to call
   * @param paramTypes the list of types of the method parameters, in order.
   */
  public ReflectiveMethod(String className, String methodName, Class<?>... paramTypes) {
    this.className = className;
    this.paramTypes = paramTypes;
    this.methodName = methodName;
  }

  /**
   * Invoke the instance method.
   *
   * <p>See {@link java.lang.reflect.Method#invoke(Object, Object...)}
   *
   * @param object the object the underlying method is invoked from
   * @param paramValues the arguments used for the method call
   * @return the return value of the method
   * @throws ReflectionException if call could not be completed
   */
  public T invoke(Object object, Object... paramValues) throws ReflectionException {
    try {
      initIfNecessary();
      return (T) method.invoke(object, paramValues);
    } catch (ClassNotFoundException
        | InvocationTargetException
        | IllegalAccessException
        | NoSuchMethodException e) {
      throw new ReflectionException(e);
    }
  }

  /**
   * Invoke th static method.
   *
   * <p>See {@link java.lang.reflect.Method#invoke(Object, Object...)}
   *
   * @param paramValues the arguments used for the method call
   * @return the return value of the method
   * @throws ReflectionException if call could not be completed
   */
  public T invokeStatic(Object... paramValues) throws ReflectionException {
    return invoke(null, paramValues);
  }

  private synchronized void initIfNecessary() throws ClassNotFoundException, NoSuchMethodException {
    if (initialized) {
      return;
    }
    method = Class.forName(className).getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);
    initialized = true;
  }
}
