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

import java.util.concurrent.TimeUnit;

/**
 * Test Sizes.
 *
 * <p>Test sizes are used to determine timeouts. These timeouts are extremely forgiving in
 * comparision to timeouts you would find on regular java unit tests. This reflects the fact that
 * running a test on android is really an integration test to begin with.
 *
 * <p>The android test size annotations live in
 *  <a href="https://developer.android.com/reference/android/test/suitebuilder/annotation/package-summary.html"><code>android.test.suitebuilder.annotation<code></a> (deprecated) or
 * in <a href="https://developer.android.com/reference/androidx/test/filters/package-summary.htmlandroidx.test.filters/package-summary.html"><code>android.support.test.filter</code></a>
 *
 * <p>Android supports SMALL, MEDIUM, and LARGE test size annotations. From the host size we handle
 * one additional size - ENORMOUS, which is deprecated and is treated as a LARGE test.
 *
 * <p>We also support an UNKNOWN size (that is an un-annotated test class / method), in this case we
 * use the medium test timeout.
 */
public enum HostTestSize {
  SMALL(90, TimeUnit.SECONDS),
  MEDIUM(4, TimeUnit.MINUTES),
  LARGE(10, TimeUnit.MINUTES),
  @Deprecated
  ENORMOUS(LARGE),
  UNKNOWN(MEDIUM);

  private HostTestSize(long testTimeout, TimeUnit testTimeoutUnit) {
    this.testTimeout = testTimeout;
    this.testTimeoutUnit = testTimeoutUnit;
  }

  private HostTestSize(HostTestSize other) {
    this.testTimeout = other.testTimeout;
    this.testTimeoutUnit = other.testTimeoutUnit;
  }

  private final long testTimeout;
  private final TimeUnit testTimeoutUnit;

  public long getTestTimeout(TimeUnit unit) {
    return unit.convert(testTimeout, testTimeoutUnit);
  }
}
