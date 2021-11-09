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

import static androidx.test.internal.util.LogUtil.logDebug;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;

/** Reflectively invokes the constructor of a declared class. */
public final class ConstructorInvocation {
  private static final String TAG = "ConstructorInvocation";
  private static final Cache<ConstructorKey, Constructor<?>> constructorCache =
      CacheBuilder.newBuilder().maximumSize(256 /* LRU eviction after max size exceeded */).build();

  private final Class<?> clazz;
  @Nullable private final Class<? extends Annotation> annotationClass;
  @Nullable private final Class<?>[] parameterTypes;

  /**
   * Creates a new {@link ConstructorInvocation}.
   *
   * <p>Constructor lookup is either done using an annotation by passing the {@code annotationClass}
   * as a parameter or through {@code parameterTypes} lookup. This class will attempt to lookup a
   * constructor by first looking for a constructor annotated with {@code annotationClass}. If no
   * constructors are found it will fallback and try to use {@code parameterTypes}.
   *
   * @param clazz the declared class to create the instance off
   * @param annotationClass the annotation class to lookup the constructor
   * @param parameterTypes array of parameter types to lookup a constructor on the declared class.
   *     The declared order of parameter types must match the order of the constructor parameters
   *     passed into {@link #invokeConstructor(Object...)}.
   */
  public ConstructorInvocation(
      @NonNull Class<?> clazz,
      @Nullable Class<? extends Annotation> annotationClass,
      @Nullable Class<?>... parameterTypes) {
    this.clazz = checkNotNull(clazz, "clazz cannot be null!");
    this.annotationClass = annotationClass;
    this.parameterTypes = parameterTypes;
  }

  @VisibleForTesting
  static void invalidateCache() {
    constructorCache.invalidateAll();
  }

  /**
   * Invokes the target constructor with the provided constructor parameters
   *
   * @param constructorParams array of objects to be passed as arguments to the constructor
   * @return a new instance of the declared class
   */
  public Object invokeConstructor(Object... constructorParams) {
    return invokeConstructorExplosively(constructorParams);
  }

  @SuppressWarnings("unchecked") // raw type for constructor can not be avoided
  private Object invokeConstructorExplosively(Object... constructorParams) {
    Object returnValue = null;
    Constructor<?> constructor = null;
    ConstructorKey constructorKey = new ConstructorKey(clazz, parameterTypes);
    try {
      // Lookup constructor in cache
      constructor = constructorCache.getIfPresent(constructorKey);
      if (null == constructor) {
        logDebug(
            TAG,
            "Cache miss for constructor: %s(%s). Loading into cache.",
            clazz.getSimpleName(),
            Arrays.toString(constructorParams));
        // Lookup constructor using annotation class
        if (annotationClass != null) {
          for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
            if (candidate.isAnnotationPresent(annotationClass)) {
              constructor = candidate;
              break;
            }
          }
        }
        // No annotated constructor found. Try constructor lookup by parameter types
        if (null == constructor) {
          constructor = clazz.getConstructor(parameterTypes);
        }

        checkState(
            constructor != null,
            "No constructor found for annotation: %s, or parameter types: %s",
            annotationClass,
            Arrays.asList(parameterTypes));
        constructorCache.put(constructorKey, constructor);
      } else {
        logDebug(
            TAG,
            "Cache hit for constructor: %s(%s).",
            clazz.getSimpleName(),
            Arrays.toString(constructorParams));
      }

      constructor.setAccessible(true);
      returnValue = constructor.newInstance(constructorParams);
    } catch (InvocationTargetException ite) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Cannot invoke constructor %s with constructorParams [%s] on clazz %s",
              constructor,
              Arrays.toString(constructorParams),
              clazz.getName()),
          ite);
    } catch (IllegalAccessException iae) {
      throw new RemoteProtocolException(
          String.format(Locale.ROOT, "Cannot create instance of %s", clazz.getName()), iae);
    } catch (InstantiationException ia) {
      throw new RemoteProtocolException(
          String.format(Locale.ROOT, "Cannot create instance of %s", clazz.getName()), ia);
    } catch (NoSuchMethodException nsme) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "No constructor found for clazz: %s. Available constructors: %s",
              clazz.getName(),
              Arrays.asList(clazz.getConstructors())),
          nsme);
    } catch (SecurityException se) {
      throw new RemoteProtocolException(
          String.format(Locale.ROOT, "Constructor not accessible: %s", constructor.getName()), se);
    } finally {
      logDebug(TAG, "%s(%s)", clazz.getSimpleName(), Arrays.toString(constructorParams));
    }
    return returnValue;
  }

  private static final class ConstructorKey {
    private final Class<?> type;
    private final Class<?>[] parameterTypes;

    public ConstructorKey(Class<?> type, Class<?>[] parameterTypes) {
      this.type = type;
      this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ConstructorKey that = (ConstructorKey) o;

      if (!type.equals(that.type)) {
        return false;
      }
      return Arrays.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + Arrays.hashCode(parameterTypes);
      return result;
    }
  }
}
