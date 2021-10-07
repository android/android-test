/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.platform.io;

import androidx.test.annotation.ExperimentalTestApi;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * An interface represents on-device I/O operations in an Android test.
 *
 * <p>This is a low level API, typically used by higher level test frameworks. It is generally not
 * recommended for direct use by most tests.
 *
 * <p>Use a concrete implementation class of this interface if you need to read/write files in your
 * tests. For example, in an Android Instrumentation test, use {@code
 * androidx.test.services.storage.TestStorage} when the test services is installed on the device.
 *
 * <p>This API is experimental and is subject to change or removal in future releases.
 */
@ExperimentalTestApi
public interface PlatformTestStorage {
  /**
   * Provides an InputStream to a test file dependency.
   *
   * @param pathname path to the test file dependency. Should not be null.
   * @return an InputStream to the given test file.
   */
  InputStream openInputFile(String pathname) throws IOException;

  /**
   * Returns the value of a given argument name.
   *
   * @param argName the argument name. Should not be null.
   */
  String getInputArg(String argName);

  /**
   * Returns the name/value map of all test arguments or an empty map if no arguments are defined.
   */
  Map<String, String> getInputArgs();

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null.
   * @return an OutputStream to the given output file.
   */
  OutputStream openOutputFile(String pathname) throws IOException;

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null.
   * @param append if true, then the lines will be added to the end of the file rather than
   *     overwriting.
   * @return an OutputStream to the given output file.
   */
  OutputStream openOutputFile(String pathname, boolean append) throws IOException;

  /**
   * Adds the given properties.
   *
   * <p>Adding a property with the same name would append new values and overwrite the old values if
   * keys already exist.
   */
  void addOutputProperties(Map<String, Serializable> properties);

  /**
   * Returns a map of all the output test properties. If no properties exist, an empty map will be
   * returned.
   */
  Map<String, Serializable> getOutputProperties();

  /**
   * Provides an InputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null.
   * @return an InputStream to the given test file.
   */
  InputStream openInternalInputFile(String pathname) throws IOException;

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null.
   * @return an OutputStream to the given output file.
   */
  OutputStream openInternalOutputFile(String pathname) throws IOException;
}
