/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.internal.runner;

/**
 * A special runner to indicate that the class is not a valid test because it has no methods.
 *
 * <p>Ideally we would just default to the upstream JUnit behavior to handle this case, but for
 * historical reasons this condition has been ignored when class path scanning for tests, and
 * changing it now may introduce painful migration for users. See b/203614578
 */
public class EmptyTestRunner extends ErrorReportingRunner {

  public EmptyTestRunner(Class<?> clazz) {
    super(
        clazz.getName(),
        new RuntimeException(
            String.format("Invalid test class '%s': No test methods found", clazz.getName())));
  }
}
