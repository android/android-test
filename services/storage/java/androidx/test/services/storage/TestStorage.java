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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.services.storage.file.HostedFile;
import androidx.test.services.storage.file.HostedFile.FileHost;
import androidx.test.services.storage.file.PropertyFile;
import androidx.test.services.storage.file.PropertyFile.Authority;
import androidx.test.services.storage.internal.TestStorageUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides convenient I/O operations for reading/writing testing relevant files, properties in a
 * test.
 */
@ExperimentalTestApi
public final class TestStorage implements PlatformTestStorage {
  private static final String TAG = TestStorage.class.getSimpleName();
  private static final String PROPERTIES_FILE_NAME = "properties.dat";

  private final ContentResolver contentResolver;

  /**
   * Default constructor.
   *
   * <p>This class is supposed to be used mostly in the Instrumentation process, e.g. in an Android
   * Instrumentation test. Thus by default, we use the content resolver of the app under test as the
   * one to resolve a URI in this storage service.
   */
  public TestStorage() {
    this(InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver());
  }

  /**
   * Constructor.
   *
   * @param contentResolver the content resolver that shall be used to resolve a URI in the test
   *     storage service. Should not be null.
   */
  public TestStorage(@NonNull ContentResolver contentResolver) {
    this.contentResolver = contentResolver;
  }

  /**
   * Provides a Uri to a test file dependency.
   *
   * <p>In most of the cases, you would use {@link #openInputFile(String)} for opening up an
   * InputStream to the input file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * @param pathname path to the test file dependency. Should not be null. This is a relative path
   *     to where the storage service stores the input files. For example, if the storage service
   *     stores the input files under "/sdcard/test_input_files", with a pathname
   *     "/path/to/my_input.txt", the file will end up at
   *     "/sdcard/test_input_files/path/to/my_input.txt" on device.
   * @return a content Uri to the test file dependency.
   */
  public static Uri getInputFileUri(@NonNull String pathname) {
    checkNotNull(pathname);
    return HostedFile.buildUri(HostedFile.FileHost.TEST_FILE, pathname);
  }

  /**
   * Provides a Uri to a test output file.
   *
   * <p>In most of the cases, you would use {@link #openOutputFile(String)} for opening up an
   * OutputStream to the output file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * @param pathname path to the test output file. Should not be null. This is a relative path to
   *     where the storage service stores the output files. For example, if the storage service
   *     stores the output files under "/sdcard/test_output_files", with a pathname
   *     "/path/to/my_output.txt", the file will end up at
   *     "/sdcard/test_output_files/path/to/my_output.txt" on device.
   */
  public static Uri getOutputFileUri(@NonNull String pathname) {
    checkNotNull(pathname);
    return HostedFile.buildUri(HostedFile.FileHost.OUTPUT, pathname);
  }

  /**
   * Returns true if {@code pathname} corresponds to a file or directory that is in a directory
   * where the storage service stores files.
   *
   * @param context Android context for getExternalFilesDir().
   * @param pathname path to a file or directory. Should not be null. This is an absolute path to a
   *     file that may be a part of the storage service.
   */
  public static boolean isTestStorageFilePath(@NonNull Context context, @NonNull String pathname) {
    File onDevicePathRoot =
        new File(HostedFile.getRootDirectory(context), TestStorageConstants.ON_DEVICE_PATH_ROOT);
    // Append a trailing slash because ON_DEVICE_PATH_ROOT has a trailing slash. If pathname already
    // has a trailing slash or other suffix, this won't affect the startsWith() matching logic.
    pathname = pathname + "/";
    return pathname.startsWith(onDevicePathRoot.getAbsolutePath());
  }

  /**
   * Provides an InputStream to a test file dependency.
   *
   * @param pathname path to the test file dependency. Should not be null. This is a relative path
   *     to where the storage service stores the input files. For example, if the storage service
   *     stores the input files under "/sdcard/test_input_files", with a pathname
   *     "/path/to/my_input.txt", the file will end up at
   *     "/sdcard/test_input_files/path/to/my_input.txt" on device.
   * @return an InputStream to the given test file.
   * @throws FileNotFoundException if pathname does not exist
   */
  @Override
  public InputStream openInputFile(@NonNull String pathname) throws FileNotFoundException {
    Uri dataUri = getInputFileUri(pathname);
    return TestStorageUtil.getInputStream(dataUri, contentResolver);
  }

  /**
   * Returns the value of a given argument name.
   *
   * <p>There should be one and only one argument defined with the given argument name. Otherwise,
   * it will throw a TestStorageException if zero or more than one arguments are found.
   *
   * <p>We suggest using some naming convention when defining the argument name to avoid possible
   * conflict, e.g. defining "namespaces" for your arguments which helps clarify how the argument is
   * used and also its scope. For example, for arguments used for authentication purposes, you could
   * name the account email argument as something like "google_account.email" and its password as
   * "google_account.password".
   *
   * @param argName the argument name. Should not be null.
   */
  @Override
  public String getInputArg(@NonNull String argName) {
    checkNotNull(argName);

    Uri testArgUri = PropertyFile.buildUri(Authority.TEST_ARGS, argName);
    Cursor cursor = null;
    try {
      cursor = doQuery(contentResolver, testArgUri);
      if (cursor.getCount() == 0) {
        throw new TestStorageException(
            String.format(
                "Query for URI '%s' did not return any results."
                    + " Make sure the argName is actually being passed in as a test argument.",
                testArgUri));
      }
      if (cursor.getCount() > 1) {
        throw new TestStorageException(
            String.format("Query for URI '%s' returned more than one result. Weird.", testArgUri));
      }
      cursor.moveToFirst();
      return cursor.getString(PropertyFile.Column.VALUE.getPosition());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  /**
   * Returns the name/value map of all test arguments or an empty map if no arguments are defined.
   */
  @Override
  public Map<String, String> getInputArgs() {
    Uri testArgUri = PropertyFile.buildUri(Authority.TEST_ARGS);
    Cursor cursor = null;
    try {
      cursor = doQuery(contentResolver, testArgUri);
      return getProperties(cursor);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null. This is a relative path to
   *     where the storage service stores the output files. For example, if the storage service
   *     stores the output files under "/sdcard/test_output_files", with a pathname
   *     "/path/to/my_output.txt", the file will end up at
   *     "/sdcard/test_output_files/path/to/my_output.txt" on device.
   * @return an OutputStream to the given output file.
   */
  @Override
  public OutputStream openOutputFile(@NonNull String pathname) throws FileNotFoundException {
    return openOutputFile(pathname, false);
  }

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null. This is a relative path to
   *     where the storage service stores the output files. For example, if the storage service
   *     stores the output files under "/sdcard/test_output_files", with a pathname
   *     "/path/to/my_output.txt", the file will end up at
   *     "/sdcard/test_output_files/path/to/my_output.txt" on device.
   * @param append if true, then the lines will be added to the end of the file rather than
   *     overwriting.
   * @return an OutputStream to the given output file.
   */
  @Override
  public OutputStream openOutputFile(@NonNull String pathname, boolean append)
      throws FileNotFoundException {
    checkNotNull(pathname);
    Uri outputUri = getOutputFileUri(pathname);
    return TestStorageUtil.getOutputStream(outputUri, contentResolver, append);
  }

  /**
   * Adds the given properties.
   *
   * <p>Adding a property with the same name would append new values and overwrite the old values if
   * keys already exist.
   */
  @Override
  public void addOutputProperties(Map<String, Serializable> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    Map<String, Serializable> allProperties = getOutputProperties();
    allProperties.putAll(properties);

    Uri propertyFileUri = getPropertyFileUri();
    ObjectOutputStream objectOutputStream = null;
    try {
      // Buffered to improve performance and avoid the unbuffered IO violation when running under
      // strict mode.
      OutputStream outputStream =
          new BufferedOutputStream(
              TestStorageUtil.getOutputStream(propertyFileUri, contentResolver));
      objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(allProperties);
    } catch (FileNotFoundException ex) {
      throw new TestStorageException("Unable to create file", ex);
    } catch (IOException e) {
      throw new TestStorageException("I/O error occurred during reading test properties.", e);
    } finally {
      silentlyClose(objectOutputStream);
    }
  }

  /**
   * Returns a map of all the output test properties. If no properties exist, an empty map will be
   * returned.
   */
  @Override
  public Map<String, Serializable> getOutputProperties() {
    Uri propertyFileUri = getPropertyFileUri();

    ObjectInputStream in = null;
    InputStream rawStream = null;
    try {
      rawStream = TestStorageUtil.getInputStream(propertyFileUri, contentResolver);
      in = new ObjectInputStream(rawStream);
      @SuppressWarnings("unchecked")
      Map<String, Serializable> recordedProperties = (Map<String, Serializable>) in.readObject();
      if (recordedProperties == null) {
        return new HashMap<>();
      } else {
        return recordedProperties;
      }
    } catch (FileNotFoundException fnfe) {
      Log.i(TAG, String.format("%s: does not exist, we must be the first call.", propertyFileUri));
    } catch (IOException | ClassNotFoundException e) {
      Log.w(TAG, "Failed to read recorded stats!", e);
    } finally {
      silentlyClose(in);
      silentlyClose(rawStream);
    }
    return new HashMap<>();
  }

  /**
   * Provides an InputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null. This is a relative path to where
   *     the storage service stores the internal files. For example, if the storage service stores
   *     the input files under "/sdcard/internal_only", with a pathname "/path/to/my_input.txt", the
   *     file will end up at "/sdcard/internal_only/path/to/my_input.txt" on device.
   * @return an InputStream to the given test file.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY)
  @Override
  public InputStream openInternalInputFile(String pathname) throws IOException {
    checkNotNull(pathname);
    Uri outputUri = HostedFile.buildUri(FileHost.INTERNAL_USE_ONLY, pathname);
    return TestStorageUtil.getInputStream(outputUri, contentResolver);
  }

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal file. Should not be null. This is a relative path to where
   *     the storage service stores the output files. For example, if the storage service stores the
   *     output files under "/sdcard/internal_only", with a pathname "/path/to/my_output.txt", the
   *     file will end up at "/sdcard/internal_only/path/to/my_output.txt" on device.
   * @return an OutputStream to the given output file.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY)
  @Override
  public OutputStream openInternalOutputFile(String pathname) throws IOException {
    checkNotNull(pathname);
    Uri outputUri = HostedFile.buildUri(FileHost.INTERNAL_USE_ONLY, pathname);
    return TestStorageUtil.getOutputStream(outputUri, contentResolver);
  }

  private static Uri getPropertyFileUri() {
    return HostedFile.buildUri(HostedFile.FileHost.EXPORT_PROPERTIES, PROPERTIES_FILE_NAME);
  }

  /**
   * Caller of this method is responsible for closing the cursor instance to avoid possible resource
   * leaks.
   */
  private static Cursor doQuery(ContentResolver resolver, Uri uri) {
    checkNotNull(resolver);
    checkNotNull(uri);

    Cursor cursor =
        resolver.query(
            uri,
            null /* projection */,
            null /* selection */,
            null /* selectionArgs */,
            null /* sortOrder */);
    if (cursor == null) {
      throw new TestStorageException(String.format("Failed to resolve query for URI: %s", uri));
    }
    return cursor;
  }

  private static Map<String, String> getProperties(Cursor cursor) {
    checkNotNull(cursor);

    Map<String, String> properties = new HashMap<>();
    while (cursor.moveToNext()) {
      properties.put(
          cursor.getString(PropertyFile.Column.NAME.getPosition()),
          cursor.getString(PropertyFile.Column.VALUE.getPosition()));
    }
    return properties;
  }

  private static void silentlyClose(InputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException e) {
        // do nothing.
      }
    }
  }

  private static void silentlyClose(OutputStream out) {
    if (out != null) {
      try {
        out.close();
      } catch (IOException e) {
        // do nothing.
      }
    }
  }
}
