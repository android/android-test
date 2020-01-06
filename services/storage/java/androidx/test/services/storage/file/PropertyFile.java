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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.net.Uri;
import androidx.test.services.storage.TestStorageConstants;

/**
 * Constants to access property file data (holding name/value pairs) and convenience methods for
 * build URIs.
 */
public final class PropertyFile {

  /** Represents columns returned by the property file service. */
  public enum Column {
    NAME("name", 0),
    VALUE("value", 1);

    private final String columnName;
    private final int position;

    private Column(String columnName, int position) {
      this.columnName = checkNotNull(columnName);
      this.position = position;
    }

    public String getName() {
      return columnName;
    }

    public int getPosition() {
      return position;
    }

    public static String[] getNames() {
      Column[] columns = values();
      String[] names = new String[values().length];
      for (int i = 0; i < names.length; i++) {
        names[i] = columns[i].getName();
      }
      return names;
    }
  }

  /** Enumerates authorities for property-based (i.e. key/value pair) content providers. */
  public enum Authority {
    TEST_ARGS(TestStorageConstants.TEST_ARGS_PROVIDER_AUTHORITY);

    private final String authority;

    Authority(String authority) {
      this.authority = checkNotNull(authority);
    }

    public String getAuthority() {
      return authority;
    }
  }

  /** Returns URI for retrieving all properties. */
  public static Uri buildUri(Authority host) {
    checkNotNull(host);
    return new Uri.Builder().scheme("content").authority(host.getAuthority()).build();
  }

  /** Returns URI for retrieving a specific property. */
  public static Uri buildUri(Authority host, String property) {
    checkNotNull(host);
    checkNotNull(property);
    return new Uri.Builder()
        .scheme("content")
        .authority(host.getAuthority())
        .path(property)
        .build();
  }

  private PropertyFile() {}
}
