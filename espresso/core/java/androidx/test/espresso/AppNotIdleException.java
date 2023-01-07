/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso;

import static androidx.test.internal.util.Checks.checkState;

import android.os.Looper;
import androidx.test.espresso.util.StringJoinerKt;
import androidx.test.internal.platform.util.TestOutputEmitter;
import java.util.List;
import java.util.Locale;

/**
 * An exception which indicates that the App has not become idle even after the specified duration.
 */
public final class AppNotIdleException extends RuntimeException implements EspressoException {

  private AppNotIdleException(String description) {
    super(description);
    TestOutputEmitter.dumpThreadStates("ThreadState-AppNotIdleException.txt");
  }

  /**
   * Creates a new AppNotIdleException suitable for erroring out a test case.
   *
   * <p>This should be called only from the main thread if the app does not idle out within the
   * specified duration.
   *
   * @param idleConditions list of idleConditions that failed to become idle.
   * @param loopCount number of times it was tried to check if they became idle.
   * @param seconds number of seconds that was tried before giving up.
   * @return a AppNotIdleException suitable to be thrown on the instrumentation thread.
   * @deprecated use {@link #create(List, String)} instead
   */
  @Deprecated
  public static AppNotIdleException create(
      List<String> idleConditions, int loopCount, int seconds) {
    checkState(Looper.myLooper() == Looper.getMainLooper());
    String errorMessage =
        String.format(
            Locale.ROOT,
            "App not idle within timeout of %s seconds even"
                + "after trying for %s iterations. The following Idle Conditions failed %s",
            seconds,
            loopCount,
            StringJoinerKt.joinToString(idleConditions, ","));
    return new AppNotIdleException(errorMessage);
  }

  /**
   * Creates a new AppNotIdleException suitable for erroring out a test case.
   *
   * <p>This should be called only from the main thread if the app does not idle out within the
   * specified duration.
   *
   * @param idleConditions list of idleConditions that failed to become idle.
   * @param message a message about the failure.
   * @return a AppNotIdleException suitable to be thrown on the instrumentation thread.
   */
  public static AppNotIdleException create(List<String> idleConditions, String message) {
    String errorMessage =
        String.format(
            Locale.ROOT,
            "%s The following Idle Conditions failed %s.",
            message,
            StringJoinerKt.joinToString(idleConditions, ","));
    return new AppNotIdleException(errorMessage);
  }
}
