/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */
package androidx.test.espresso.remote;

import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.LogUtil.logDebug;

import android.util.LruCache;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

/** Reflectively invokes a method of a declared instance. */
final class MethodInvocation {
  private static final String TAG = "MethodInvocation";
  private static final LruCache<MethodKey, Method> methodCache =
      new LruCache<>(256 /* LRU eviction after max size exceeded */);

  private final Class<?> clazz;
  @Nullable private final Object instance;
  private final String methodName;
  private final Class<?>[] parameterTypes;

  /**
   * Creates a new {@link MethodInvocation}.
   *
   * @param clazz the class which declares the method
   * @param instance an instance of the declared class. To call a static method pass {@code null}
   *     here.
   * @param methodName name of the method to invoke
   * @param parameterTypes array of parameter types to identify a method on the declared class. The
   *     declared order of parameter types must match the order of the constructor parameters passed
   *     into {@link #invokeMethod(Object...)}, {@link #invokeDeclaredMethod(Object...)}.
   */
  public MethodInvocation(
      @NonNull Class<?> clazz,
      @Nullable Object instance,
      @NonNull String methodName,
      Class<?>... parameterTypes) {
    this.clazz = checkNotNull(clazz, "clazz cannot be null!");
    this.instance = instance;
    checkArgument(
        methodName != null && !methodName.isEmpty(), "methodName cannot be null or empty");
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
  }

  private static Method getMethod(MethodKey methodKey) throws NoSuchMethodException {
    return getMethodInternal(methodKey, false);
  }

  private static Method getDeclaredMethod(MethodKey methodKey) throws NoSuchMethodException {
    return getMethodInternal(methodKey, true);
  }

  private static Method getMethodInternal(MethodKey methodKey, boolean declaredMethod)
      throws NoSuchMethodException {
    Method method = methodCache.get(methodKey);
    if (null == method) {
      logDebug(
          TAG,
          "Cache miss for method: %s#%s(%s). Loading into cache.",
          methodKey.type.getSimpleName(),
          methodKey.methodName,
          Arrays.toString(methodKey.parameterTypes));
      if (declaredMethod) {
        method = methodKey.type.getDeclaredMethod(methodKey.methodName, methodKey.parameterTypes);
      } else {
        method = methodKey.type.getMethod(methodKey.methodName, methodKey.parameterTypes);
      }
      methodCache.put(methodKey, method);
    } else {
      logDebug(
          TAG,
          "Cache hit for method: %s#%s(%s).",
          methodKey.type.getSimpleName(),
          methodKey.methodName,
          Arrays.toString(methodKey.parameterTypes));
    }
    return method;
  }

  @VisibleForTesting
  public static void invalidateCache() {
    methodCache.evictAll();
  }

  /**
   * Invokes a method declared in the target class
   *
   * @param methodParams array of objects to be passed as arguments to the method
   * @return the method return value, if any
   */
  public Object invokeDeclaredMethod(Object... methodParams) {
    try {
      return invokeMethodExplosively(
          getDeclaredMethod(new MethodKey(clazz, methodName, parameterTypes)), methodParams);
    } catch (NoSuchMethodException nsme) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "No method: %s(%s) found for clazz: %s Available methods: %s",
              methodName,
              Arrays.asList(parameterTypes),
              clazz.getName(),
              Arrays.asList(clazz.getDeclaredMethods())),
          nsme);
    }
  }

  /**
   * Same as {@link #invokeDeclaredMethod(Object...)}, but if no method is found, recursively
   * searches for the method on the superclass.
   *
   * @param methodParams array of objects to be passed as arguments to the method
   * @return the method return value, if any
   */
  public Object invokeMethod(Object... methodParams) {
    try {
      return invokeMethodExplosively(
          getMethod(new MethodKey(clazz, methodName, parameterTypes)), methodParams);
    } catch (NoSuchMethodException nsme) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "No method: %s found for clazz: %s. Available methods: %s",
              methodName,
              clazz.getName(),
              Arrays.asList(clazz.getMethods())),
          nsme);
    }
  }

  private Object invokeMethodExplosively(Method method, Object... args) {
    Object returnValue = null;
    try {
      method.setAccessible(true);
      returnValue = method.invoke(instance, args);
    } catch (InvocationTargetException ite) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Cannot invoke method %s with args [%s] on builder %s",
              method,
              Arrays.toString(args),
              clazz.getName()),
          ite);
    } catch (IllegalAccessException iae) {
      throw new RemoteProtocolException(
          String.format(Locale.ROOT, "Cannot create instance of %s", clazz.getName()), iae);
    } catch (SecurityException se) {
      throw new RemoteProtocolException(
          String.format(Locale.ROOT, "Method not accessible: %s", method.getName()), se);
    } finally {
      logDebug(
          TAG,
          "%s.invokeMethodExplosively(%s,%s)",
          clazz.getSimpleName(),
          methodName,
          Arrays.toString(args));
    }
    return returnValue;
  }

  private static final class MethodKey {
    private final Class<?> type;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    public MethodKey(Class<?> type, String methodName, Class<?>[] parameterTypes) {
      this.type = type;
      this.methodName = methodName;
      this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (null == o || getClass() != o.getClass()) {
        return false;
      }

      MethodKey methodKey = (MethodKey) o;

      if (!type.equals(methodKey.type)) {
        return false;
      }
      if (!methodName.equals(methodKey.methodName)) {
        return false;
      }
      return Arrays.equals(parameterTypes, methodKey.parameterTypes);
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + methodName.hashCode();
      result = 31 * result + Arrays.hashCode(parameterTypes);
      return result;
    }
  }
}
