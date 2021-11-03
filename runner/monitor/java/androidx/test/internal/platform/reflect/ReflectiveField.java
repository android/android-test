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
import java.lang.reflect.Field;

/**
 * Helper class for making more performant reflection field access.
 *
 * <p>Lazy initializes and caches Method object ro attempt to reduce reflection overhead.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class ReflectiveField<T> {
  private final String className;
  private final String fieldName;

  // lazy init
  private boolean initialized = false;
  private Field field;

  /**
   * Creates a ReflectiveField.
   *
   * @param className the fully qualified class name that defines the field
   * @param fieldName the field name
   */
  public ReflectiveField(String className, String fieldName) {
    this.className = className;
    this.fieldName = fieldName;
  }

  /**
   * Retrieves the field's value, initializing if necessary.
   *
   * @param object the object that holds the field's value
   * @return the field's value
   * @throws ReflectionException if field could not be accessed
   */
  public T get(Object object) throws ReflectionException {
    try {
      initIfNecessary();
      return (T) field.get(object);
    } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      throw new ReflectionException(e);
    }
  }

  private synchronized void initIfNecessary() throws ClassNotFoundException, NoSuchFieldException {
    if (initialized) {
      return;
    }
    field = Class.forName(className).getDeclaredField(fieldName);
    field.setAccessible(true);
    initialized = true;
  }
}
