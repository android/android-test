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

import android.util.Log;
import androidx.test.annotation.ExperimentalTestApi;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that reads/writes the runner data using the raw file system.
 *
 * <p>This API is experimental and is subject to change or removal in future releases.
 */
@ExperimentalTestApi
public final class FileTestStorage implements PlatformTestStorage {

  private static final String TAG = FileTestStorage.class.getSimpleName();

  /**
   * Provides an InputStream to a test file dependency.
   *
   * @param pathname path to the test file dependency. Should not be null. This is an absolute file
   *     path on the device, and it's the infrastructure/client's responsibility to make sure the
   *     file path is readable.
   */
  @Override
  public InputStream openInputFile(String pathname) throws IOException {
    return new FileInputStream(pathname);
  }

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test file dependency. Should not be null. This is an absolute file
   *     path on the device, and it's the infrastructure/client's responsibility to make sure the
   *     file path is writable.
   */
  @Override
  public OutputStream openOutputFile(String pathname) throws IOException {
    return new FileOutputStream(pathname);
  }

  @Override
  public OutputStream openOutputFile(String pathname, boolean append) throws IOException {
    return new FileOutputStream(pathname, append);
  }

  /**
   * Test input arguments is not supported when raw file I/O is used.
   *
   * <p><code>null</code> is always returned.
   */
  @Override
  public String getInputArg(String argName) {
    Log.w(TAG, "Test input args is not supported.");
    return null;
  }

  /**
   * Test input arguments is not supported when raw file I/O is used.
   *
   * <p>An empty map is always returned.
   */
  @Override
  public Map<String, String> getInputArgs() {
    Log.w(TAG, "Test input args is not supported.");
    return new HashMap<>();
  }

  /** Test output properties is not supported when raw file I/O is used. */
  @Override
  public void addOutputProperties(Map<String, Serializable> properties) {
    Log.w(TAG, "Output properties is not supported.");
  }

  /**
   * Test output properties is not supported when raw file I/O is used.
   *
   * <p>An empty map is always returned.
   */
  @Override
  public Map<String, Serializable> getOutputProperties() {
    Log.w(TAG, "Output properties is not supported.");
    return new HashMap<>();
  }

  /**
   * Provides an InputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal input file. Should not be null. This is an absolute file
   *     path on the device, and it's the infrastructure/client's responsibility to make sure the
   *     file path is readable.
   */
  @Override
  public InputStream openInternalInputFile(String pathname) throws IOException {
    return new FileInputStream(pathname);
  }

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal output file. Should not be null. This is an absolute file
   *     path on the device, and it's the infrastructure/client's responsibility to make sure the
   *     file path is writable.
   */
  @Override
  public OutputStream openInternalOutputFile(String pathname) throws IOException {
    return new FileOutputStream(pathname);
  }
}
