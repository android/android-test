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
import org.hamcrest.Matcher;

/**
 * Indicates that a given matcher did not match any {@link Root}s (windows) from those that are
 * currently available.
 */
public final class NoMatchingRootException extends RuntimeException implements EspressoException {

  private NoMatchingRootException(String description) {
    super(description);
    TestOutputEmitter.dumpThreadStates("ThreadState-NoMatchingRootException.txt");
  }

  public static NoMatchingRootException create(Matcher<Root> rootMatcher, List<Root> roots) {
    checkNotNull(rootMatcher);
    checkNotNull(roots);
    return new NoMatchingRootException(
        String.format(
            Locale.ROOT,
            "Matcher '%s' did not match any of the following roots: %s",
            rootMatcher,
            roots));
  }
}
