/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import com.google.auto.value.AutoValue;
import java.util.Objects;
import javax.annotation.Nullable;

@AutoValue
public abstract class ExecutedTest {
  public enum Status {
    ERROR, FAILED, PASSED, STARTED, ASSUMPTION_FAILURE
  }

  ExecutedTest() {}

  public abstract String getAllLines();
  @Nullable public abstract String getCurrentTest();
  @Nullable public abstract String getNumTests();
  @Nullable public abstract String getId();
  public abstract String getStackTrace();
  @Nullable public abstract Status getStatus();
  @Nullable public abstract String getTestClass();
  @Nullable public abstract String getTestMethod();
  public abstract String getTestResult();
  public abstract String getTestStatusStream();

  @Override public final boolean equals(Object other) {
    if (!(other instanceof ExecutedTest)) {
      return false;
    }

    ExecutedTest otherExecutedTest = (ExecutedTest) other;
    // allLines and testResult do not contribute to equality
    return Objects.equals(getCurrentTest(), otherExecutedTest.getCurrentTest())
        && Objects.equals(getNumTests(), otherExecutedTest.getNumTests())
        && Objects.equals(getId(), otherExecutedTest.getId())
        && Objects.equals(getStackTrace(), otherExecutedTest.getStackTrace())
        && Objects.equals(getStatus(), otherExecutedTest.getStatus())
        && Objects.equals(getTestClass(), otherExecutedTest.getTestClass())
        && Objects.equals(getTestMethod(), otherExecutedTest.getTestMethod())
        && Objects.equals(getTestStatusStream(), otherExecutedTest.getTestStatusStream());
  }

  @Override public final int hashCode() {
    return Objects.hash(
        getCurrentTest(),
        getNumTests(),
        getId(),
        getStackTrace(),
        getStatus(),
        getTestClass(),
        getTestMethod(),
        getTestStatusStream());
  }

  public static Builder builder() {
    return new AutoValue_ExecutedTest.Builder()
        .setId(null)
        .setCurrentTest(null)
        .setNumTests(null)
        .setTestClass(null)
        .setTestMethod(null);
  }

  /** A builder for {@link ExecutedTest}s. */
  @AutoValue.Builder
  public abstract static class Builder {
    private final StringBuilder allLinesBuilder = new StringBuilder();
    private final StringBuilder testResultBuilder = new StringBuilder();
    private final StringBuilder stackTraceBuilder = new StringBuilder();
    private final StringBuilder testStatusStreamBuilder = new StringBuilder();

    public abstract Builder setId(@Nullable String id);
    public abstract Builder setCurrentTest(@Nullable String currentTest);
    public abstract Builder setNumTests(@Nullable String numTests);
    public abstract Builder setTestClass(@Nullable String testClass);
    public abstract Builder setTestMethod(@Nullable String testMethod);
    abstract Builder setAllLines(String allLines);
    abstract Builder setTestResult(String testResult);
    abstract Builder setStackTrace(String stackTrace);
    abstract Builder setTestStatusStream(String testStatusStream);

    public Builder appendAllLines(String allLines) {
      appendToStringBuilder(allLinesBuilder, allLines);
      return this;
    }

    public Builder appendResultStream(String testResult) {
      appendToStringBuilder(testResultBuilder, testResult);
      return this;
    }

    public Builder appendStackTrace(String stackTrace) {
      appendToStringBuilder(stackTraceBuilder, stackTrace);
      return this;
    }

    public Builder appendStatusStream(String testStatusStream) {
      appendToStringBuilder(testStatusStreamBuilder, testStatusStream);
      return this;
    }

    public abstract Builder setStatus(@Nullable Status status);

    public abstract ExecutedTest autoBuild();
    public ExecutedTest build() {
      setAllLines(allLinesBuilder.toString());
      setTestResult(testResultBuilder.toString());
      setStackTrace(stackTraceBuilder.toString());
      setTestStatusStream(testStatusStreamBuilder.toString());
      return autoBuild();
    }

    private static void appendToStringBuilder(StringBuilder sb, String text) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(text);
    }
  }
}
