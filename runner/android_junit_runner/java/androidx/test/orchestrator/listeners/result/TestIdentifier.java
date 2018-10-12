/*
 * Copyright (C) 2008 The Android Open Source Project
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

package androidx.test.orchestrator.listeners.result;

/**
 * Identifies a parsed instrumentation test.
 *
 * <p>This is a copy of {@code com.android.ddmlib.testrunner.TestIdentifier}.
 */
public class TestIdentifier {

  private final String className;
  private final String testName;

  /**
   * Creates a test identifier.
   *
   * @param className fully qualified class name of the test. Cannot be null.
   * @param testName name of the test. Cannot be null.
   */
  public TestIdentifier(String className, String testName) {
    if (className == null || testName == null) {
      throw new IllegalArgumentException("className and testName must " + "be non-null");
    }
    this.className = className;
    this.testName = testName;
  }

  /** Returns the fully qualified class name of the test. */
  public String getClassName() {
    return className;
  }

  /** Returns the name of the test. */
  public String getTestName() {
    return testName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((testName == null) ? 0 : testName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TestIdentifier other = (TestIdentifier) obj;
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if (!className.equals(other.className)) {
      return false;
    }
    if (testName == null) {
      if (other.testName != null) {
        return false;
      }
    } else if (!testName.equals(other.testName)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format("%s#%s", getClassName(), getTestName());
  }
}
