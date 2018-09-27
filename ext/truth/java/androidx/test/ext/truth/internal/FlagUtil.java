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
package androidx.test.ext.truth.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for asserting flag values
 *
 * @hide
 */
public final class FlagUtil {

  private FlagUtil() {}

  /**
   * Builds list of specified flags as hex strings, intended for building readable error messages
   * for flag assets.
   *
   * @hide
   */
  public static List<String> flagNames(int flags) {
    List<String> flagStrings = new ArrayList<>();
    for (int i = 0; i < 32; i++) {
      int flagValue = 1 << i;
      if ((flags & flagValue) == flagValue) {
        flagStrings.add(String.format("0x%x", flagValue));
      }
    }
    return flagStrings;
  }
}
