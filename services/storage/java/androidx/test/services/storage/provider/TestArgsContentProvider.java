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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.test.services.storage.TestStorageConstants;
import androidx.test.services.storage.TestStorageServiceProto.TestArgument;
import androidx.test.services.storage.TestStorageServiceProto.TestArguments;
import androidx.test.services.storage.file.PropertyFile;
import androidx.test.services.storage.file.PropertyFile.Authority;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
  private static final String ANDROID_TEST_SERVER_SPEC_FORMAT = "_server_address";

  private static final String SYSTEM_PROPERTY_CLAZZ = "android.os.SystemProperties";
  private static final String GET_METHOD = "get";

  private String systemPropertyClassName;
  private Method getString;

  void setSystemPropertyClassNameForTest(String className) {
    this.systemPropertyClassName = className;
  }

  private String getQemuHost() {
    try {
      if (null == getString) {
        if (null == systemPropertyClassName) {
          systemPropertyClassName = SYSTEM_PROPERTY_CLAZZ;
        }

        Class<?> clazz = Class.forName(systemPropertyClassName);
        getString = clazz.getMethod(GET_METHOD, String.class, String.class);
      }
      return (String) getString.invoke(null, "qemu.host.hostname", "");
    } catch (ClassNotFoundException cnfe) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", cnfe);
      return "";
    } catch (SecurityException se) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", se);
      return "";
    } catch (NoSuchMethodException nsme) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", nsme);
      return "";
    } catch (InvocationTargetException ite) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", ite);
      return "";
    } catch (IllegalAccessException iae) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", iae);
      return "";
    } catch (IllegalArgumentException iae) {
      Log.w(TAG, "Couldn't access SysProps for qemu hostname.", iae);
      return "";
    }
  }

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
    Map<String, String> cleanArgMap = new HashMap<>();
    Map<String, String> qemuArgMap = new HashMap<>();
    String qemuHost = getQemuHost();

    for (TestArgument testArg : readProtoFromFile().getArgList()) {
      String key = testArg.getName();
      String val = testArg.getValue();
      cleanArgMap.put(key, val);

      if (!"".equals(qemuHost) && key.endsWith(ANDROID_TEST_SERVER_SPEC_FORMAT)) {
        String serverHost = val.split(":")[0];
        if (serverHost.startsWith(qemuHost)) {
          // TODO: remove startswith check once the emulator launcher passes in FQDN.
          // val.replace(qemuHost, "10.0.2.2");
          // b/c the system property should be a FQDN (just like the test args are FQDN)
          val = val.replace(serverHost, "10.0.2.2");
        }
      }
      qemuArgMap.put(key, val);
    }
    if ("true"
        .equalsIgnoreCase(cleanArgMap.get(TestStorageConstants.USE_QEMU_IPS_IF_POSSIBLE_ARG_TAG))) {
      return qemuArgMap;
    } else {
      return cleanArgMap;
    }
  }

  private static TestArguments readProtoFromFile() {
    File testArgsFile =
        new File(
            Environment.getExternalStorageDirectory(),
            TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE
                + TestStorageConstants.TEST_ARGS_FILE_NAME);
    if (!testArgsFile.exists()) {
      return TestArguments.getDefaultInstance();
    }
    try {
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
