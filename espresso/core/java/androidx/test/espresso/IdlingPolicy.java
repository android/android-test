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

import android.util.Log;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Allows users to control idling idleTimeouts in Espresso. */
public final class IdlingPolicy {
  private static final String TAG = "IdlingPolicy";

  private enum ResponseAction {
    THROW_APP_NOT_IDLE,
    THROW_IDLE_TIMEOUT,
    LOG_ERROR
  };

  private final long idleTimeout;
  private final TimeUnit unit;
  private final ResponseAction errorHandler;

  /** The amount of time the policy allows a resource to be non-idle. */
  public long getIdleTimeout() {
    return idleTimeout;
  }

  /** The unit for {@link #getIdleTimeout}. */
  public TimeUnit getIdleTimeoutUnit() {
    return unit;
  }

  /**
   * Invoked when the idle idleTimeout has been exceeded.
   *
   * @param busyResources the resources that are not idle.
   * @param message an additional message to include in an exception.
   */
  public void handleTimeout(List<String> busyResources, String message) {
    switch (errorHandler) {
      case THROW_APP_NOT_IDLE:
        throw AppNotIdleException.create(busyResources, message);
      case THROW_IDLE_TIMEOUT:
        throw new IdlingResourceTimeoutException(busyResources);
      case LOG_ERROR:
        Log.w(TAG, "These resources are not idle: " + busyResources);
        break;
      default:
        throw new IllegalStateException("should never reach here." + busyResources);
    }
  }


  Builder toBuilder() {
    return new Builder(this);
  }

  private IdlingPolicy(Builder builder) {
    checkArgument(builder.idleTimeout > 0);
    this.idleTimeout = builder.idleTimeout;
    this.unit = checkNotNull(builder.unit);
    this.errorHandler = checkNotNull(builder.errorHandler);
  }

  static class Builder {
    private long idleTimeout = -1;
    private TimeUnit unit = null;
    private ResponseAction errorHandler = null;

    public Builder() {}

    private Builder(IdlingPolicy copy) {
      this.idleTimeout = copy.idleTimeout;
      this.unit = copy.unit;
      this.errorHandler = copy.errorHandler;
    }

    public IdlingPolicy build() {
      return new IdlingPolicy(this);
    }


    public Builder withIdlingTimeout(long idleTimeout) {
      this.idleTimeout = idleTimeout;
      return this;
    }

    public Builder withIdlingTimeoutUnit(TimeUnit unit) {
      this.unit = unit;
      return this;
    }

    public Builder throwAppNotIdleException() {
      this.errorHandler = ResponseAction.THROW_APP_NOT_IDLE;
      return this;
    }

    public Builder throwIdlingResourceTimeoutException() {
      this.errorHandler = ResponseAction.THROW_IDLE_TIMEOUT;
      return this;
    }

    public Builder logWarning() {
      this.errorHandler = ResponseAction.LOG_ERROR;
      return this;
    }
  }
}
