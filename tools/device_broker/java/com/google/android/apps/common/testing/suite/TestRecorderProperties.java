/*
 * Copyright (C) 2017 The Android Open Source Project
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
 *
 */

package com.google.android.apps.common.testing.suite;

import java.util.Map;

/**
 * A Test can implement this interface if it would like to publish a set of properties. The
 * TestRecordingSuite can record the run of the tests into a file. It uses this interface to see if
 * it should record any additional properties for this test (implicit properties are name, suite,
 * pass/file, etc...)
 */
public interface TestRecorderProperties {

  /**
   * Implement this method if you would like to publish additional properties for the TestRecorder
   * to save.
   *
   * @return map of properties to save
   */
  Map<String, Object> getTestRecorderProperties();
}
