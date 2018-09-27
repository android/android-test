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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.util;

import android.util.Log;

/** Util methods related to logging, using the android.util.Log class. */
public final class LogUtil {
  private static volatile String myProcName = null;

  /**
   * Calls {@link Log#d(String, String)} if {@link Log#isLoggable(String, int)} returns {@code true}
   * for the given tag. Additionally, provides the ability to use a formatted message using the
   * specified format string and arguments.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *     activity where the log call occurs.
   * @param message The message you would like logged.
   * @param args Arguments referenced by the format specifiers in the format string.
   */
  public static void logDebug(String tag, String message, Object... args) {
    logDebug(tag, () -> message, args);
  }

  private static void logDebug(String tag, Supplier<String> msgSupplier, Object... args) {
    if (isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, String.format(msgSupplier.get(), args));
    }
  }

  /**
   * Similar to {@link #logDebug} except it appends the current process name to the end of the
   * message using {@link ProcessUtil#getCurrentProcessName(Context)}.
   */
  public static void logDebugWithProcess(String tag, String message, Object... args) {
    logDebug(tag, () -> message + " in " + procName(), args);
  }

  private static final String procName() {
    String procDesc = myProcName;
    if (procDesc == null) {
      try {
        procDesc = ProcSummary.summarize("self").cmdline;
      } catch (ProcSummary.SummaryException se) {
        procDesc = "unknown";
      }
      if (procDesc.length() > 64 && procDesc.contains("-classpath")) {
        // on robolectric, not an interesting proc name :)
        procDesc = "robolectric";
      }
    }
    return procDesc;
  }

  /**
   * Checks to see whether or not a log for the specified tag is loggable at the specified level.
   *
   * @param tag The tag to check.
   * @param level The level to check.
   * @return Whether or not that this is allowed to be logged
   * @see {@link Log#isLoggable(String, int)}
   */
  private static boolean isLoggable(String tag, final int level) {
    if (tag.length() > 23) {
      // Trim the tag to prevent Log.isLoggable() from throwing an exception if the length of the
      // tag greater 23 characters.
      tag = tag.substring(0, 22);
    }
    return Log.isLoggable(tag, level);
  }

  interface Supplier<T> {
    T get();
  }
}
