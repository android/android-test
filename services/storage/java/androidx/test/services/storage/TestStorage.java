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

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.annotation.Beta;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.services.storage.file.HostedFile;
import androidx.test.services.storage.file.PropertyFile;
import androidx.test.services.storage.file.PropertyFile.Authority;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Provides convenient I/O operations for reading/writing testing relevant files, properties in a
 * test.
 */
@Beta
public final class TestStorage {
  private static final String TAG = TestStorage.class.getSimpleName();

  private static final ContentResolver contentResolver =
      InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver();
  private static final String PROPERTIES_FILE_NAME = "properties.dat";

  private TestStorage() {}

  /**
   * Provides an InputStream to a test file dependency.
   *
   * @param pathname path to the test file dependency. Should not be null.
   * @return an InputStream to the given test file.
   */
  public static InputStream openInputFile(@Nonnull String pathname) throws FileNotFoundException {
    Uri dataUri = getInputFileUri(pathname);
    return getInputStream(dataUri);
  }

  /**
   * Provides a Uri to a test file dependency.
   *
   * <p>In most of the cases, you would use {@link #openInputFile(String)} for opening up an
   * InputStream to the input file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * @param pathname path to the test file dependency. Should not be null.
   * @return a content Uri to the test file dependency.
   */
  public static Uri getInputFileUri(@Nonnull String pathname) {
    checkNotNull(pathname);
    return HostedFile.buildUri(HostedFile.FileHost.TEST_FILE, pathname);
  }

  /**
   * Returns the value of a given argument name.
   *
   * <p>There should be one and only one argument defined with the given argument name. Otherwise,
   * it will throw a TestStorageException if zero or more than one arguments are found.
   *
   * @param argName the argument name. Should not be null.
   */
  public static String getInputArg(@Nonnull String argName) {
    checkNotNull(argName);

    Uri testArgUri = PropertyFile.buildUri(Authority.TEST_ARGS, argName);
    Cursor cursor = doQuery(contentResolver, testArgUri);
    try {
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
      cursor.close();
    }
  }

  /**
   * Returns the name/value map of all test arguments or an empty map if no arguments are defined.
   */
  public static Map<String, String> getInputArgs() {
    Uri testArgUri = PropertyFile.buildUri(Authority.TEST_ARGS);
    Cursor cursor = doQuery(contentResolver, testArgUri);
    Map<String, String> result = getProperties(cursor);
    cursor.close();
    return result;
  }

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test output file. Should not be null.
   * @return an OutputStream to the given output file.
   */
  public static OutputStream openOutputFile(@Nonnull String pathname)
      throws FileNotFoundException {
    checkNotNull(pathname);

    Uri outputUri = getOutputFileUri(pathname);
    return getOutputStream(outputUri);
  }

  /**
   * Provides a Uri to a test output file.

   * <p>In most of the cases, you would use {@link #openOutputFile(String)} for opening up an
   * OutputStream to the output file content immediately. Only use this method if you would like to
   * store the file Uri and use it for I/O operations later.
   *
   * @param pathname path to the test output file. Should not be null.
   */
  public static Uri getOutputFileUri(@Nonnull String pathname) {
    checkNotNull(pathname);
    return HostedFile.buildUri(HostedFile.FileHost.OUTPUT, pathname);
  }

  /**
   * Adds the given properties.
   *
   * <p>Adding a property with the same name would append new values and overwrite the old values if
   * keys already exist.
   */
  public static void addOutputProperties(Map<String, Serializable> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    Map<String, Serializable> allProperties = getOutputProperties();
    allProperties.putAll(properties);

    Uri propertyFileUri = getPropertyFileUri();
    ObjectOutputStream objectOutputStream = null;
    try {
      objectOutputStream = new ObjectOutputStream(getOutputStream(propertyFileUri));
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
  public static Map<String, Serializable> getOutputProperties() {
    Uri propertyFileUri = getPropertyFileUri();

    ObjectInputStream in = null;
    InputStream rawStream = null;
    try {
      rawStream = getInputStream(propertyFileUri);
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

  private static Uri getPropertyFileUri() {
    return HostedFile.buildUri(HostedFile.FileHost.EXPORT_PROPERTIES, PROPERTIES_FILE_NAME);
  }

  /**
   * Gets the input stream for a given Uri.
   *
   * @param uri The Uri for which the InputStream is required.
   */
  static InputStream getInputStream(Uri uri) throws FileNotFoundException {
    checkNotNull(uri);

    ContentProviderClient providerClient = makeContentProviderClient(contentResolver, uri);
    try {
      // Assignment to a variable is required. Do not inline.
      ParcelFileDescriptor pfd = providerClient.openFile(uri, "r");
      // Buffered to improve performance.
      return new BufferedInputStream(new ParcelFileDescriptor.AutoCloseInputStream(pfd));
    } catch (RemoteException re) {
      throw new TestStorageException("Unable to access content provider: " + uri, re);
    } finally {
      // Uses #release() to be compatible with API < 24.
      providerClient.release();
    }
  }

  /**
   * Gets the output stream for a given Uri.
   *
   * @param uri The Uri for which the OutputStream is required.
   */
  static OutputStream getOutputStream(Uri uri) throws FileNotFoundException {
    checkNotNull(uri);

    ContentProviderClient providerClient = makeContentProviderClient(contentResolver, uri);
    try {
      return new ParcelFileDescriptor.AutoCloseOutputStream(providerClient.openFile(uri, "w"));
    } catch (RemoteException re) {
      throw new TestStorageException("Unable to access content provider: " + uri, re);
    } finally {
      // Uses #release() to be compatible with API < 24.
      providerClient.release();
    }
  }

  private static ContentProviderClient makeContentProviderClient(
      ContentResolver resolver, Uri uri) {
    checkNotNull(resolver);

    ContentProviderClient providerClient = resolver.acquireContentProviderClient(uri);
    if (null == providerClient) {
      throw new TestStorageException(
          String.format(
              "No content provider registered for: %s. Are all test services apks installed?",
              uri));
    }
    return providerClient;
  }

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
