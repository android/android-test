/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.espresso.util;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for tracing.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public final class TracingUtil {
  private static final int SPAN_NAME_MAX_LEN = 100;
  private static final String SPAN_NAME_EXCLUDE = "[^0-9A-Za-z._$()\\[\\] /:-]";

  private TracingUtil() {}

  /**
   * Generates a span name according to multiple optional arguments.
   *
   * @param prefix User-defined prefix string, e.g. Espresso.
   * @param methodName The name of method being traced, e.g. onView, perform, and check.
   * @param arguments An optional list of description of arguments, e.g. user-defined description,
   *     ViewAction's class name, and description of Matcher.
   * @return a string as the name of a span.
   */
  public static String getSpanName(String prefix, String methodName, Object... arguments) {
    // Sanitize all input data.
    String processedPrefix = sanitizeName(prefix, SPAN_NAME_EXCLUDE, -1);
    String processedMethodName = sanitizeName(methodName, SPAN_NAME_EXCLUDE, -1);

    List<String> processedArguments = new ArrayList<>();
    if (arguments != null) {
      for (Object argument : arguments) {
        if (argument == null) {
          continue;
        }
        String processedArgument = sanitizeName(argument.toString(), SPAN_NAME_EXCLUDE, -1);
        if (!processedArgument.isEmpty()) {
          processedArguments.add(processedArgument);
        }
      }
    }

    // Assemble processed strings.
    String spanName = processedPrefix;
    if (!processedPrefix.isEmpty() && !processedMethodName.isEmpty()) {
      spanName += ".";
    }
    spanName += processedMethodName;
    if (!processedArguments.isEmpty()) {
      spanName += "(" + StringJoinerKt.joinToString(processedArguments, ", ") + ")";
    }

    // Check the length.
    spanName = sanitizeName(spanName, null, SPAN_NAME_MAX_LEN);
    return spanName;
  }

  /**
   * Generates a short string indicating the class type of the given element.
   *
   * @param element An object instance.
   * @param defaultName The default result in case the class type is not accessible.
   * @return a string as the class name.
   */
  public static String getClassName(Object element, String defaultName) {
    // Note: getSimpleName() may return an empty string for an anonymous class.
    // Ideally we would use Class.getTypeName() but this is not supported in legacy
    // Android with compiler < 1.8.
    String name = element == null ? null : element.getClass().getSimpleName();
    if (name == null || name.length() == 0) {
      name = defaultName;
    }
    return name == null ? "" : name;
  }

  /**
   * Generates a span name according to multiple optional arguments.
   *
   * @param name The input name to be processed.
   * @param exclude A regex pattern indicating the characters to exclude.
   * @param maxLength The maximum length of the name; the substring out of the length will be
   *     deleted; skip the length check if maxLength is negative.
   * @return a string as the name.
   */
  private static String sanitizeName(String name, String exclude, int maxLength) {
    if (name == null) {
      return "";
    }

    String newName = name;
    if (exclude != null && exclude.length() > 0) {
      newName = newName.replaceAll(exclude, "").trim();
    }

    if (maxLength > 0 && newName.length() > maxLength) {
      newName = newName.substring(0, maxLength).trim();
    }
    return newName;
  }
}
