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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hamcrest.Description;

import java.util.HashMap;
import java.util.Map;

/**
 * An auxiliary class that makes masked characters more visible.
 * You might want to use it in cases when user thorough comprehension is required, i.e. test reports.
 */
public class SpecialCharVisualizer {

  private static final Map<Character, String> SPECIAL_CHAR_CODES = new HashMap<>();
  static {
    SPECIAL_CHAR_CODES.put('\u00A0', "[NBSP]");
  }

  @NonNull
  public static String highlight(@NonNull CharSequence text) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char current = text.charAt(i);
      if (SPECIAL_CHAR_CODES.containsKey(current)) {
        result.append(SPECIAL_CHAR_CODES.get(current));
      } else {
        result.append(current);
      }
    }
    return result.toString();
  }

  public static Description appendHighlightedValue(
      @Nullable CharSequence text, Description description, boolean asValue
  ) {
    if (text == null) {
      return description;
    }
    String result = SpecialCharVisualizer.highlight(text);
    if (asValue) {
      description.appendValue(result);
    } else {
      description.appendText(result);
    }
    if (!result.contentEquals(text)) {
      description.appendText(
          " (This string contains special characters - see SpecialCharVisualizer in Espresso)"
      );
    }
    return description;
  }
}
