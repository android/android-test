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

/** Holds constants that are shared between java and android code. */
public final class SharedEnvironment {
  public static final Environment ENVIRONMENT =
      new Environment.Builder().fromMap(System.getenv()).build();
  public static final String ON_DEVICE_PATH_ROOT = "googletest/";
  public static final String ON_DEVICE_PATH_TEST_PROPERTIES =
      ON_DEVICE_PATH_ROOT + "test_exportproperties/";
  public static final String AAG_TRACE_TAG = "AAG_TRACE";

  private SharedEnvironment() {}
}
