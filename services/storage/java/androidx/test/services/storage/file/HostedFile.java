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
package androidx.test.services.storage.file;

import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.test.services.storage.TestStorageConstants;

/** Constants to access hosted file data and convenience methods for building Uris. */
public final class HostedFile {

  /** An enum of the columns returned by the hosted file service. */
  public enum HostedFileColumn {
    NAME("name", String.class, 3 /* Cursor.FIELD_TYPE_STRING since api 11 */, 0),
    TYPE("type", String.class, 3 /* Cursor.FIELD_TYPE_STRING since api 11 */, 1),
    SIZE("size", Long.class, 1 /* Cursor.FIELD_TYPE_INTEGER since api 11 */, 2),
    DATA("_data", Byte[].class, 4 /* Cursor.FIELD_TYPE_BLOB since api 11 */, 3),
    DISPLAY_NAME(OpenableColumns.DISPLAY_NAME, String.class, 3, 4),
    SIZE_2(OpenableColumns.SIZE, Long.class, 2, 5);

    private final String columnName;
    private final Class<?> columnType;
    private final int androidType;
    private final int position;

    private HostedFileColumn(
        String columnName, Class<?> columnType, int androidType, int position) {
      this.columnName = checkNotNull(columnName);
      this.columnType = checkNotNull(columnType);
      this.androidType = androidType;
      this.position = position;
    }

    public String getColumnName() {
      return columnName;
    }

    public Class<?> getColumnType() {
      return columnType;
    }

    public int getAndroidType() {
      return androidType;
    }

    public int getPosition() {
      return position;
    }

    public static String[] getColumnNames() {
      HostedFileColumn[] columns = values();
      String[] names = new String[columns.length];
      for (int i = 0; i < names.length; i++) {
        names[i] = columns[i].getColumnName();
      }
      return names;
    }
  }

  /** Enum used to indicate whether a file is a directory or regular file. */
  public enum FileType {
    FILE("f"),
    DIRECTORY("d");
    private String type;

    private FileType(String type) {
      this.type = checkNotNull(type);
    }

    public String getTypeCode() {
      return type;
    }

    public static FileType fromTypeCode(String type) {
      for (FileType fileType : values()) {
        if (fileType.getTypeCode().equals(type)) {
          return fileType;
        }
      }
      throw new IllegalArgumentException("unknown type: " + type);
    }
  }

  /** An enum containing all known storage services. */
  public enum FileHost {
    TEST_FILE(TestStorageConstants.TEST_RUNFILES_PROVIDER_AUTHORITY, false),
    EXPORT_PROPERTIES(TestStorageConstants.OUTPUT_PROPERTIES_PROVIDER_AUTHORITY, true),
    OUTPUT(TestStorageConstants.TEST_OUTPUT_PROVIDER_AUTHORITY, true),
    INTERNAL_USE_ONLY(TestStorageConstants.INTERNAL_USE_PROVIDER_AUTHORITY, true);

    private final String authority;
    private final boolean writeable;

    FileHost(String authority, boolean writeable) {
      this.authority = checkNotNull(authority);
      this.writeable = writeable;
    }

    /** The content resolver authority. */
    public String getAuthority() {
      return authority;
    }

    /** True if writable location, false otherwise. */
    public boolean isWritable() {
      return writeable;
    }
  }

  public static Uri buildUri(FileHost host, String fileName) {
    return new Uri.Builder()
        .scheme("content")
        .authority(host.getAuthority())
        .path(fileName)
        .build();
  }

  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  private HostedFile() {}
}
