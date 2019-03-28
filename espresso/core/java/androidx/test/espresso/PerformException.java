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

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.test.internal.platform.util.TestOutputEmitter;
import java.util.Locale;

/**
 * Indicates that an exception occurred while performing a ViewAction on the UI thread.
 *
 * <p>A description of the {@link ViewAction}, the view being performed on and the cause are
 * included in the error. Note: {@link FailureHandler}s can mutate the exception later to make it
 * more user friendly.
 *
 * <p>This is generally not recoverable so it is thrown on the instrumentation thread.
 */
public final class PerformException extends RuntimeException implements EspressoException {
  private static final String MESSAGE_FORMAT = "Error performing '%s' on view '%s'.";

  private final String actionDescription;
  private final String viewDescription;

  private PerformException(Builder builder) {
    super(
        String.format(
            Locale.ROOT, MESSAGE_FORMAT, builder.actionDescription, builder.viewDescription),
        builder.cause);
    this.actionDescription = checkNotNull(builder.actionDescription);
    this.viewDescription = checkNotNull(builder.viewDescription);
    TestOutputEmitter.dumpThreadStates("ThreadState-PerformException.txt");
  }

  public String getActionDescription() {
    return actionDescription;
  }

  public String getViewDescription() {
    return viewDescription;
  }

  /** Builder for {@link PerformException}. */
  public static class Builder {
    private String actionDescription;
    private String viewDescription;
    private Throwable cause;

    public Builder from(PerformException instance) {
      this.actionDescription = instance.getActionDescription();
      this.viewDescription = instance.getViewDescription();
      this.cause = instance.getCause();
      return this;
    }

    public Builder withActionDescription(String actionDescription) {
      this.actionDescription = actionDescription;
      return this;
    }

    public Builder withViewDescription(String viewDescription) {
      this.viewDescription = viewDescription;
      return this;
    }

    public Builder withCause(Throwable cause) {
      this.cause = cause;
      return this;
    }

    public PerformException build() {
      return new PerformException(this);
    }
  }
}
