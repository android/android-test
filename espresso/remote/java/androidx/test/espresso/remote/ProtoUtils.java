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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/** Contains various utility methods to ease use of protos and increased readability in code. */
public final class ProtoUtils {

  private ProtoUtils() {
    // noOp instance
  }

  /**
   * Maps an enum proto message type to a internal representation enum type T.
   *
   * @param protoEnumIndex the proto enum index of the value returned by the unwrapped proto message
   * @param enumClass the enum class to map against
   * @param <T> the generic type of the enum representation
   * @return the enum constant for a proto enum index
   */
  @SuppressWarnings("unchecked") // safe covariant cast
  public static <T extends Enum> T checkedGetEnumForProto(int protoEnumIndex, Class<T> enumClass) {
    T[] enumConstants = enumClass.getEnumConstants();
    if (0 <= protoEnumIndex && protoEnumIndex < enumConstants.length) {
      return enumConstants[protoEnumIndex];
    }
    throw new IllegalArgumentException(
        String.format(
            Locale.ROOT,
            "No such index: %d in enum class: %s",
            protoEnumIndex,
            enumClass.getSimpleName()));
  }

  /**
   * Returns a filtered view of a class's declared {@link Field} list.
   *
   * @param clazz the class to introspect
   * @param targetFieldNames the field names to filter from a class {@link Field} list
   * @return a filtered list of class {@link Field}s
   * @throws NoSuchFieldException if a field name does not exist in {@code clazz}
   */
  public static List<Field> getFilteredFieldList(Class<?> clazz, List<String> targetFieldNames)
      throws NoSuchFieldException {
    List<Field> targetFields = new LinkedList<>();
    for (String targetFieldName : targetFieldNames) {
      targetFields.add(getFieldRecursively(clazz, targetFieldName, null));
    }
    return targetFields;
  }

  private static Field getFieldRecursively(
      Class<?> clazz, String targetFieldName, NoSuchFieldException noSuchField)
      throws NoSuchFieldException {
    // Throw if we have reached the top of the class hierarchy and the Field was not found!
    if (Object.class == clazz) {
      throw noSuchField;
    }

    try {
      return clazz.getDeclaredField(targetFieldName);
    } catch (NoSuchFieldException nsfe) {
      return getFieldRecursively(clazz.getSuperclass(), targetFieldName, nsfe);
    }
  }

  /**
   * Capitalizes the first char of a String.
   *
   * <p>Examples: "espresso" -> "Espresso", "Espresso" -> "Espresso"
   *
   * @param aString the String to capitalize
   * @return capitalized String or original String, if aString was empty
   */
  public static String capitalizeFirstChar(String aString) {
    return null == aString || aString.isEmpty()
        ? aString
        : aString.substring(0, 1).toUpperCase(Locale.ENGLISH) + aString.substring(1);
  }
}
