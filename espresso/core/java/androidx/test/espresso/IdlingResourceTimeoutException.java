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
import java.util.List;
import java.util.Locale;

/**
 * Indicates that an {@link IdlingResource}, which has been registered with the framework, has not
 * idled within the allowed time.
 *
 * <p>Since it is not safe to proceed with test execution while the registered resource is busy (as
 * it is likely to cause inconsistent results in the test), this is an unrecoverable error. The test
 * author should verify that the {@link IdlingResource} interface has been implemented correctly.
 */
public final class IdlingResourceTimeoutException extends RuntimeException
    implements EspressoException {

  public IdlingResourceTimeoutException(List<String> resourceNames) {
    super(
        String.format(
            Locale.ROOT, "Wait for %s to become idle timed out", checkNotNull(resourceNames)));
    TestOutputEmitter.dumpThreadStates("ThreadState-IdlingResTimeoutExcep.txt");
  }
}
