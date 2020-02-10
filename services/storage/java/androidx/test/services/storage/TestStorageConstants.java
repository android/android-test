/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.services.storage;

/** Holds constants that are shared between on-device and host-side testing infrastructure. */
@ExperimentalTestStorage
public final class TestStorageConstants {

  /** The parent folder name for all the test related files. */
  public static final String ON_DEVICE_PATH_ROOT = "googletest/";

  /** The folder for internal use. */
  public static final String ON_DEVICE_PATH_INTERNAL_USE = ON_DEVICE_PATH_ROOT + "internal_use/";

  /** The provider authority for internal use. */
  public static final String INTERNAL_USE_PROVIDER_AUTHORITY =
      "androidx.test.services.storage._internal_use_files";

  /** The folder where the test output files are written. */
  public static final String ON_DEVICE_PATH_TEST_OUTPUT = ON_DEVICE_PATH_ROOT + "test_outputfiles/";

  /** The provider authority for test output files. */
  public static final String TEST_OUTPUT_PROVIDER_AUTHORITY =
      "androidx.test.services.storage.outputfiles";

  /** The folder for test properties that shall be exported to the testing infra. */
  public static final String ON_DEVICE_PATH_TEST_PROPERTIES =
      ON_DEVICE_PATH_ROOT + "test_exportproperties/";

  /** The provider authority for output properties. */
  public static final String OUTPUT_PROPERTIES_PROVIDER_AUTHORITY =
      "androidx.test.services.storage.properties";

  /** The folder where the fixture test scripts are pushed on device. */
  public static final String ON_DEVICE_FIXTURE_SCRIPTS = ON_DEVICE_PATH_ROOT + "fixture_scripts/";

  /** The folder where files needed in test runtime are pushed. */
  public static final String ON_DEVICE_TEST_RUNFILES = ON_DEVICE_PATH_ROOT + "test_runfiles/";

  /** The provider authority for files needed in test runtime. */
  public static final String TEST_RUNFILES_PROVIDER_AUTHORITY =
      "androidx.test.services.storage.runfiles";

  /** The provider authority for test arguments. */
  public static final String TEST_ARGS_PROVIDER_AUTHORITY =
      "androidx.test.services.storage.testargs";

  /** The name of the file where test arguments are stored. */
  public static final String TEST_ARGS_FILE_NAME = "test_args.dat";

  /** The name of the test argument that indicates whether qemu ips should be used. */
  public static final String USE_QEMU_IPS_IF_POSSIBLE_ARG_TAG = "infra_use_qemu_ips";

  private TestStorageConstants() {}
}
