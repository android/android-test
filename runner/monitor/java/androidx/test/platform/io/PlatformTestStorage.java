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

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.test.annotation.ExperimentalTestApi;
import java.io.FileNotFoundException;
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
   * @throws FileNotFoundException if pathname does not exist
   */
  InputStream openInputFile(String pathname) throws FileNotFoundException;

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
   * @throws FileNotFoundException if pathname does not exist
   */
  OutputStream openOutputFile(String pathname) throws FileNotFoundException;

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null.
   * @param append if true, then the lines will be added to the end of the file rather than
   *     overwriting.
   * @return an OutputStream to the given output file.
   * @throws FileNotFoundException if pathname does not exist
   */
  OutputStream openOutputFile(String pathname, boolean append) throws FileNotFoundException;

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
   * @throws FileNotFoundException if pathname does not exist
   */
  InputStream openInternalInputFile(String pathname) throws FileNotFoundException;

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null.
   * @return an OutputStream to the given output file.
   * @throws FileNotFoundException if pathname does not exist
   */
  OutputStream openInternalOutputFile(String pathname) throws FileNotFoundException;

  /**
   * Provides a Uri to a test file dependency.
   *
   * <p>In most of the cases, you would use {@link #openInputFile(String)} for opening up an
   * InputStream to the input file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * <p><b>Note:</b> temporary API - will be renamed to getInpFileUri in future
   *
   * @param pathname path to the test file dependency. Should not be null. This is a relative path
   *     to where the storage service stores the input files. For example, if the storage service
   *     stores the input files under "/sdcard/test_input_files", with a pathname
   *     "/path/to/my_input.txt", the file will end up at
   *     "/sdcard/test_input_files/path/to/my_input.txt" on device.
   * @return a content Uri to the test file dependency.
   *     <p>Note: temporary API - will be renamed to getInputFileUri in future
   */
  Uri getInputFileUri(@NonNull String pathname);

  /**
   * Provides a Uri to a test output file.
   *
   * <p>In most of the cases, you would use {@link #openOutputFile(String)} for opening up an
   * OutputStream to the output file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * <p><b>Note:</b> temporary API - will be renamed to getOutputFileUri in future
   *
   * @param pathname path to the test output file. Should not be null. This is a relative path to
   *     where the storage service stores the output files. For example, if the storage service
   *     stores the output files under "/sdcard/test_output_files", with a pathname
   *     "/path/to/my_output.txt", the file will end up at
   *     "/sdcard/test_output_files/path/to/my_output.txt" on device.
   */
  Uri getOutputFileUri(@NonNull String pathname);

  /**
   * Returns true if {@code pathname} corresponds to a file or directory that is in a directory
   * where the storage stores files.
   *
   * @param pathname path to a file or directory. Should not be null. This is an absolute path to a
   *     file that may be a part of the storage service.
   */
  boolean isTestStorageFilePath(@NonNull String pathname);
}
