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
package androidx.test.platform.io;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.test.internal.platform.ServiceLoaderWrapper;

/**
 * A registry instance that holds a reference to an {@code PlatformTestStorage} instance.
 *
 * <p>Users should use this to retrieve the appropriate {@link PlatformTestStorage} for the current
 * execution environment.
 */
public final class PlatformTestStorageRegistry {
  private static PlatformTestStorage testStorageInstance;

  static {
    // By default, uses the instance loaded by the service loader if available; otherwise, uses a
    // default no_op implementation.
    testStorageInstance =
        ServiceLoaderWrapper.loadSingleService(PlatformTestStorage.class, FileTestStorage::new);
  }

  private PlatformTestStorageRegistry() {}

  /**
   * Registers a new {@code PlatformTestStorage} instance. This will override any previously set
   * instance.
   *
   * <p>Users should not typically call this directly - it is intended for use by the test
   * infrastructure.
   *
   * @param instance the instance to be registered. Cannot be null.
   */
  public static synchronized void registerInstance(PlatformTestStorage instance) {
    testStorageInstance = checkNotNull(instance);
  }

  /**
   * Returns the registered {@code PlatformTestStorage} instance.
   *
   * <p>By default, a {@link FileTestStorage} implementation is used. The default implementation is
   * currently recommended for users using android gradle plugins version 8.0 or greater which
   * supports writing output files (only). Gradle users using versions 8.0 or greater can optionally
   * also opt in the test services {@link TestStorage} implementation by adding the following
   * configuration to their build.gradle file: <br>
   * <code>
   * defaultConfig { testInstrumentationRunnerArguments useTestStorageService: "true" }
   * dependencies { androidTestUtil "androidx.test.services:test-services:$servicesVersion" }
   * </code> <br>
   *
   * <p>This method returns the instance last registered by the {@link
   * #registerInstance(PlatformTestStorage)} method, or the default instance if none is ever
   * registered. Advanced users can provide {@link java.util.ServiceLoader} metadata to provide an
   * alternate implementation to load.
   */
  public static synchronized PlatformTestStorage getInstance() {
    return testStorageInstance;
  }
}
