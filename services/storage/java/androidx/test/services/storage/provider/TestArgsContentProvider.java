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
package androidx.test.services.storage.provider;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import androidx.test.services.storage.TestStorageConstants;
import androidx.test.services.storage.TestStorageException;
import androidx.test.services.storage.TestStorageServiceProto.TestArgument;
import androidx.test.services.storage.TestStorageServiceProto.TestArguments;
import androidx.test.services.storage.file.HostedFile;
import androidx.test.services.storage.file.HostedFile.FileHost;
import androidx.test.services.storage.file.PropertyFile;
import androidx.test.services.storage.file.PropertyFile.Authority;
import androidx.test.services.storage.internal.TestStorageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides access to test arguments stored as a file on external device storage. This provider
 * supports only the query api. Use {@link PropertyFile#buildUri(Authority)} to retrieve all test
 * arguments. To retrieve a specific argument, build the URI with the arg name as path by calling
 * {@link PropertyFile#buildUri(Authority, String)}.
 */
@SuppressWarnings("javadoc")
public final class TestArgsContentProvider extends ContentProvider {

  private static final String TAG = "TestArgCP";

  @Override
  public int delete(Uri arg0, String arg1, String[] arg2) {
    // Not allowed.
    return 0;
  }

  @Override
  public String getType(Uri arg0) {
    // mime types not supported
    return null;
  }

  @Override
  public Uri insert(Uri arg0, ContentValues arg1) {
    throw new UnsupportedOperationException("Insertion is not allowed.");
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(
      Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    checkNotNull(uri);

    Map<String, String> argMap = buildArgMapFromFile();

    MatrixCursor cursor = new MatrixCursor(PropertyFile.Column.getNames());
    String argName = uri.getLastPathSegment();
    if (argName != null) {
      // Return the specific arg name/value.
      if (argMap.containsKey(argName)) {
        String[] row = {argName, argMap.get(argName)};
        cursor.addRow(row);
      }
    } else {
      // No specific arg specified. Return the entire argMap.
      for (Entry<String, String> entry : argMap.entrySet()) {
        String[] row = {entry.getKey(), entry.getValue()};
        cursor.addRow(row);
      }
    }
    return cursor;
  }

  private Map<String, String> buildArgMapFromFile() {
    Map<String, String> argMap = new HashMap<>();

    for (TestArgument testArg : readProtoFromFile(this.getContext()).getArgList()) {
      String key = testArg.getName();
      String val = testArg.getValue();
      argMap.put(key, val);
    }
    return argMap;
  }

  private static TestArguments readProtoFromFile(Context context) {
    // File written by the InternalUseOnlyFilesContentProvider
    Uri testArgsProtoUri =
        HostedFile.buildUri(FileHost.INTERNAL_USE_ONLY, TestStorageConstants.TEST_ARGS_FILE_NAME);

    try (InputStream testArgsProtoInputStream =
        TestStorageUtil.getInputStream(testArgsProtoUri, context.getContentResolver())) {
      Log.i(TAG, "Parsing test args from URI: " + testArgsProtoUri);
      return TestArguments.parseFrom(testArgsProtoInputStream);
    } catch (IOException | TestStorageException e) {
      Log.i(
          TAG,
          "Test args file not found via URI: " + testArgsProtoUri + ". Checking file system...");
    }

    // File written directly to /sdcard/
    File testArgsFile =
        new File(
            HostedFile.getInputRootDirectory(context),
            TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE
                + TestStorageConstants.TEST_ARGS_FILE_NAME);
    if (!testArgsFile.exists()) {
      Log.i(TAG, "Test args file also not found at " + testArgsFile.getAbsolutePath());
      return TestArguments.getDefaultInstance();
    }
    try {
      Log.i(TAG, "Parsing test args from " + testArgsFile.getAbsolutePath());
      return TestArguments.parseFrom(new FileInputStream(testArgsFile));
    } catch (IOException e) {
      throw new RuntimeException("Not able to read from file: " + testArgsFile.getName(), e);
    }
  }

  @Override
  public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
    // Not allowed.
    return 0;
  }
}
