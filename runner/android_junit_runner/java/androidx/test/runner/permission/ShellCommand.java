/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.runner.permission;

import android.app.UiAutomation;
import androidx.annotation.VisibleForTesting;

/**
 * Ideally we wouldn't need this abstraction but since {@link UiAutomation} is final we need an
 * abstraction on top to be able to mock it in tests. It also gives us the flexibility to switch to
 * another implementation in the future.
 */
@VisibleForTesting
public abstract class ShellCommand {

  /** Characters that have no special meaning to the shell. */
  private static final String SAFE_PUNCTUATION = "@%-_+:,./";

  /**
   * Quotes a word so that it can be used, without further quoting, as an argument (or part of an
   * argument) in a shell command.
   */
  static String shellEscape(String word) {
    int len = word.length();
    if (len == 0) {
      // Empty string is a special case: needs to be quoted to ensure that it gets
      // treated as a separate argument.
      return "''";
    }
    for (int ii = 0; ii < len; ii++) {
      char c = word.charAt(ii);
      // We do this positively so as to be sure we don't inadvertently forget
      // any unsafe characters.
      if (!Character.isLetterOrDigit(c) && SAFE_PUNCTUATION.indexOf(c) == -1) {
        // replace() actually means "replace all".
        return "'" + word.replace("'", "'\\''") + "'";
      }
    }
    return word;
  }

  abstract void execute() throws Exception;
}
