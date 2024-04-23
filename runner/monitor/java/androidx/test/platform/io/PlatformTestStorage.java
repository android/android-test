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
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * An interface representing on-device I/O operations in an Android test.
 *
 * <p>This API allows users to retrieve test data specified in the build configuration, and write
 * output test data that can be automatically collected by the test runner infrastructure, if the
 * environment supports it.
 *
 * <p>Typically users will retrieve the appropriate implementation via {@link
 * PlatformTestStorageRegistry#getInstance()}.
 *
 * <p>Implementers would need to also implement the appropriate test runner support for pushing and
 * pulling the test data to and from the device from the build environment.
 */
public interface PlatformTestStorage {

  /**
   * Provides an InputStream to a test file dependency.
   *
   * <p>In bazel/blaze environments, this corresponds to files passed in the 'data' attribute of the
   * android_instrumentation_test or android_local_test build rule.
   *
   * <p>This API is currently not supported in gradle environments.
   *
   * @param pathname the path to the test file dependency, relative to the root where the storage
   *     implementation stores input files. Should not be null.
   * @return a potentially unbuffered InputStream to the given test file. Users will typically want
   *     to buffer the input in memory when reading from this stream.
   * @throws FileNotFoundException if pathname does not exist
   */
  InputStream openInputFile(String pathname) throws FileNotFoundException;

  /**
   * Returns the value of a given argument name.
   *
   * <p>In bazel/blaze environments, this corresponds to flags passed in the 'args' attribute of the
   * android_instrumentation_test or android_local_test build rule.
   *
   * <p>This API is currently unsupported in gradle environments. It is recommended to use {@link
   * InstrumentationRegistry#getArguments()} as an alternative.
   *
   * @param argName the argument name. Should not be null.
   */
  String getInputArg(String argName);

  /**
   * Returns the name/value map of all test arguments or an empty map if no arguments are defined.
   *
   * @see {@link #getInputArg(String)}
   */
  Map<String, String> getInputArgs();

  /**
   * Provides an OutputStream to a test output file. Will overwrite any data written to the same
   * pathname in the same test run.
   *
   * <p>Supported test runners will pull the files from the device once the test completes.
   *
   * <p>In gradle android instrumentation test environments, the files will typically be stored in
   * path_to_your_project/module_name/build/outputs/managed_device_android_test_additional_output
   * <br>
   *
   * @param pathname relative path to the test output file. Should not be null.
   * @return a potentially unbuffered OutputStream to the given output file. Users will typically
   *     want to buffer the output in memory when writing to this stream.
   * @throws FileNotFoundException if pathname does not exist
   */
  OutputStream openOutputFile(String pathname) throws FileNotFoundException;

  /**
   * Provides an OutputStream to a test output file.
   *
   * <p>This API is identical to {@link #openOutputFile(String)} with the additional feature of
   * allowing appending or overwriting test data.
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
   *
   * <p>This API is unsupported in gradle environments. <br>
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
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  default InputStream openInternalInputFile(String pathname) throws FileNotFoundException {
    return openInputFile(pathname);
  }

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null.
   * @return an OutputStream to the given output file.
   * @throws FileNotFoundException if pathname does not exist
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  default OutputStream openInternalOutputFile(String pathname) throws FileNotFoundException {
    return openOutputFile(pathname);
  }

  /**
   * Provides a Uri to a test file dependency.
   *
   * <p>In most of the cases, you would use {@link #openInputFile(String)} for opening up an
   * InputStream to the input file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * @param pathname path to the test file dependency. Should not be null. This is a relative path
   *     to where the storage implementation stores the input files. For example, if the storage
   *     service stores the input files under "/sdcard/test_input_files", with a pathname
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
   * @param pathname path to the test output file. Should not be null. This is a relative path to
   *     where the storage implementation stores the output files. For example, if the storage
   *     service stores the output files under "/sdcard/test_output_files", with a pathname
   *     "/path/to/my_output.txt", the file will end up at
   *     "/sdcard/test_output_files/path/to/my_output.txt" on device.
   */
  Uri getOutputFileUri(@NonNull String pathname);

  /**
   * Returns true if {@code pathname} corresponds to a file or directory that is in a directory
   * where the storage implementation stores files.
   *
   * @param pathname path to a file or directory. Should not be null. This is an absolute path to a
   *     file that may be a part of the storage service.
   */
  boolean isTestStorageFilePath(@NonNull String pathname);
}
