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

package com.google.android.apps.common.testing.suite;

import com.google.android.apps.common.testing.broker.BrokeredDevice;

/** An empty implementation of the AndroidTestHarness */
final class NoopAndroidTestHarness implements AndroidTestHarness {

  public NoopAndroidTestHarness() {}

  /** Called before any android_test method has started to run. */
  @Override public void beforeAllTests() {}

  /** Called after all android_test methods have finished running. */
  @Override public void afterAllTests() {}

  /** Called before every android_test method. */
  @Override public void beforeEachTest(BrokeredDevice device, String testName) {}

  /** Called after every android_test method. */
  @Override public void afterEachTest() {}
}
