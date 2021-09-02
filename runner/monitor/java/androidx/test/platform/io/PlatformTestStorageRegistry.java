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

import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.internal.platform.ServiceLoaderWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An exposed registry instance that holds a reference to an {@code PlatformTestStorage} instance.
 *
 * <p>{@code PlatformTestStorage} and {@code PlatformTestStorageRegistry} are low level APIs,
 * typically used by higher level test frameworks. It is generally not recommended for direct use by
 * most tests.
 *
 * <p>This API is experimental and is subject to change or removal in future releases.
 */
@ExperimentalTestApi
public final class PlatformTestStorageRegistry {
  private static PlatformTestStorage testStorageInstance;

  static {
    // By default, uses the instance loaded by the service loader if available; otherwise, uses a
    // default no_op implementation.
    testStorageInstance =
        ServiceLoaderWrapper.loadSingleService(
            PlatformTestStorage.class, NoOpPlatformTestStorage::new);
  }

  private PlatformTestStorageRegistry() {}

  /**
   * Registers a new {@code PlatformTestStorage} instance. This will override any previously set
   * instance.
   *
   * @param instance the instance to be registered. Cannot be null.
   */
  public static synchronized void registerInstance(PlatformTestStorage instance) {
    testStorageInstance = checkNotNull(instance);
  }

  /**
   * Returns the registered {@code PlatformTestStorage} instance.
   *
   * <p>This method returns the instance last registered by the {@link
   * #registerInstance(PlatformTestStorage)} method, or the default instance if none is ever
   * registered.
   */
  public static synchronized PlatformTestStorage getInstance() {
    return testStorageInstance;
  }

  /** A test storage that does nothing. All the I/O operations in this class are ignored. */
  static class NoOpPlatformTestStorage implements PlatformTestStorage {

    @Override
    public InputStream openInputFile(String pathname) {
      return new NullInputStream();
    }

    @Override
    public String getInputArg(String argName) {
      return null;
    }

    @Override
    public Map<String, String> getInputArgs() {
      return new HashMap<>();
    }

    @Override
    public OutputStream openOutputFile(String pathname) {
      return new NullOutputStream();
    }

    @Override
    public OutputStream openOutputFile(String pathname, boolean append) {
      return new NullOutputStream();
    }

    @Override
    public void addOutputProperties(Map<String, Serializable> properties) {}

    @Override
    public Map<String, Serializable> getOutputProperties() {
      return new HashMap<>();
    }

    @Override
    public InputStream openInternalInputFile(String pathname) throws IOException {
      return new NullInputStream();
    }

    @Override
    public OutputStream openInternalOutputFile(String pathname) throws IOException {
      return new NullOutputStream();
    }

    static class NullInputStream extends InputStream {
      @Override
      public int read() {
        return -1;
      }
    }

    static class NullOutputStream extends OutputStream {
      @Override
      public void write(int b) {}
    }
  }
}
