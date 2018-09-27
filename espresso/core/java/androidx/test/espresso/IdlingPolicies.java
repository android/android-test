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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

/**
 * Allows users fine grain control over idling policies.
 *
 * <p>Espresso's default idling policies are suitable for most usecases - however certain execution
 * environments (like the ARM emulator) might be very slow. This class allows users the ability to
 * adjust defaults to sensible values for their environments.
 */
public final class IdlingPolicies {

  private IdlingPolicies() {}

  private static volatile IdlingPolicy masterIdlingPolicy =
      new IdlingPolicy.Builder()
          .withIdlingTimeout(60)
          .withIdlingTimeoutUnit(TimeUnit.SECONDS)
          .throwAppNotIdleException()
          .build();

  private static volatile IdlingPolicy dynamicIdlingResourceErrorPolicy =
      new IdlingPolicy.Builder()
          .withIdlingTimeout(26)
          .withIdlingTimeoutUnit(TimeUnit.SECONDS)
          .throwIdlingResourceTimeoutException()
          .build();

  private static volatile IdlingPolicy dynamicIdlingResourceWarningPolicy =
      new IdlingPolicy.Builder()
          .withIdlingTimeout(5)
          .withIdlingTimeoutUnit(TimeUnit.SECONDS)
          .logWarning()
          .build();

  /**
   * Updates the IdlingPolicy used in UiController.loopUntil to detect AppNotIdleExceptions.
   *
   * @param timeout the timeout before an AppNotIdleException is created.
   * @param unit the unit of the timeout value.
   */
  public static void setMasterPolicyTimeout(long timeout, TimeUnit unit) {
    checkArgument(timeout > 0);
    checkNotNull(unit);
    masterIdlingPolicy =
        masterIdlingPolicy
            .toBuilder()
            .withIdlingTimeout(timeout)
            .withIdlingTimeoutUnit(unit)
            .build();
  }

  /**
   * Updates the IdlingPolicy used by IdlingResourceRegistry to determine when IdlingResources
   * timeout.
   *
   * @param timeout the timeout before an IdlingResourceTimeoutException is created.
   * @param unit the unit of the timeout value.
   */
  public static void setIdlingResourceTimeout(long timeout, TimeUnit unit) {
    checkArgument(timeout > 0);
    checkNotNull(unit);
    dynamicIdlingResourceErrorPolicy =
        dynamicIdlingResourceErrorPolicy
            .toBuilder()
            .withIdlingTimeout(timeout)
            .withIdlingTimeoutUnit(unit)
            .build();
  }

  /**
   * This method overrides Espresso's default behaviour of disabling timeouts when a step debugger
   * is attached to the VM. Setting the timeout flag reenables the behaviour of throwing a timeout
   * exception.
   *
   * <p>The value set in this method is superceded if {@link #unsafeMakeMasterPolicyWarning()} is
   * also called.
   */
  public static void setMasterPolicyTimeoutWhenDebuggerAttached(
      boolean timeoutWhenDebuggerAttached) {
    masterIdlingPolicy =
        masterIdlingPolicy
            .toBuilder()
            .build();
  }


  public static IdlingPolicy getMasterIdlingPolicy() {
    return masterIdlingPolicy;
  }

  public static IdlingPolicy getDynamicIdlingResourceWarningPolicy() {
    return dynamicIdlingResourceWarningPolicy;
  }

  public static IdlingPolicy getDynamicIdlingResourceErrorPolicy() {
    return dynamicIdlingResourceErrorPolicy;
  }
}
