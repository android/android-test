/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.rule.provider;

import android.content.ContentProvider;
import android.util.Log;
import java.io.File;
import java.lang.ref.WeakReference;

/**
 * A class that stores arguments of a {@link ContentProvider} under test for {@link
 * ProviderTestRule}. The arguments include the {@link ContentProvider}'s class, the authority
 * defined for it, and values for other optional arguments that can be passed in via {@link
 * ProviderTestRule.Builder}.
 *
 * @see ProviderTestRule.Builder#withDBName(String, String)
 * @see ProviderTestRule.Builder#withDBCmds(String, String...)
 * @see ProviderTestRule.Builder#withDBCmdsFromFile(String, File)
 * @see ProviderTestRule.Builder#withDBDataFromFile(String, File)
 */
final class ProviderArgs {

  private static final String TAG = "ProviderArgs";
  private final String authority;
  private final Class<? extends ContentProvider> providerClass;
  private String dBName;
  private String[] dBCmds;
  private File dBCmdFile;
  private File dBDataFile;
  private WeakReference<ContentProvider> providerRef;

  /**
   * @see ProviderTestRule.Builder#Builder(String, Class)
   * @see ProviderTestRule.Builder#addProvider(String, Class)
   */
  public ProviderArgs(String authority, Class<? extends ContentProvider> providerClass) {
    this.authority = authority;
    this.providerClass = providerClass;
  }

  /** @see ProviderTestRule.Builder#withDBName(String, String) */
  public void setDBName(String dbName) {
    if (dBName != null) {
      Log.w(
          TAG,
          String.format(
              "Database name for ContentProvider " + "with authority %s already exists",
              authority));
    }
    dBName = dbName;
  }

  /** @see ProviderTestRule.Builder#withDBCmds(String, String...) */
  public void setDBCmds(String... dbCmds) {
    if (dBCmds != null) {
      Log.w(
          TAG,
          String.format(
              "Database commands for ContentProvider " + "with authority %s already set",
              authority));
    }
    dBCmds = dbCmds;
  }

  /** @see ProviderTestRule.Builder#withDBCmdsFromFile(String, File) */
  public void setDBCmdFile(File dbCmdFile) {
    if (dBCmdFile != null) {
      Log.w(
          TAG,
          String.format(
              "Database command file for ContentProvider " + "with authority %s already set",
              authority));
    }
    dBCmdFile = dbCmdFile;
  }

  /** @see ProviderTestRule.Builder#withDBDataFromFile(String, File) */
  public void setDBDataFile(File dbDataFile) {
    if (dBDataFile != null) {
      Log.w(
          TAG,
          String.format(
              "Database file to restore for ContentProvider " + "with authority %s already set",
              authority));
    }
    dBDataFile = dbDataFile;
  }

  /** Pass in the provider instance to hold a weak reference to it. */
  public void setProviderRef(ContentProvider provider) {
    if (providerRef != null) {
      Log.w(
          TAG,
          String.format(
              "Reference to Provider instance " + "with authority %s already set", authority));
    }
    providerRef = new WeakReference<>(provider);
  }

  /**
   * Pass in extra list of database commands and merge with the existing database commands passed in
   * by {@link #setDBCmds}. If the existing commands is {@code null}, the newly passed in commands
   * is directly assigned to the existing commands.
   *
   * @param dbCmds extra list of database commands to merge with existing database commands.
   */
  public void addDBCmds(String... dbCmds) {
    if (null == dBCmds) {
      dBCmds = dbCmds;
    } else {
      String[] newCmds = new String[dBCmds.length + dbCmds.length];
      System.arraycopy(dBCmds, 0, newCmds, 0, dBCmds.length);
      System.arraycopy(dbCmds, 0, newCmds, dBCmds.length, dbCmds.length);
      dBCmds = newCmds;
    }
  }

  public boolean hasDBName() {
    return (dBName != null);
  }

  public boolean hasDBCmds() {
    return (dBCmds != null);
  }

  public boolean hasDBCmdFile() {
    return (dBCmdFile != null);
  }

  public boolean hasDBDataFile() {
    return (dBDataFile != null);
  }

  public Class<? extends ContentProvider> getProviderClass() {
    return providerClass;
  }

  public String getAuthority() {
    return authority;
  }

  public String getDBName() {
    return dBName;
  }

  public String[] getDBCmds() {
    return dBCmds;
  }

  public File getDBCmdFile() {
    return dBCmdFile;
  }

  public File getDBDataFile() {
    return dBDataFile;
  }

  public ContentProvider getProvider() {
    if (providerRef != null) {
      return providerRef.get();
    } else {
      return null;
    }
  }
}
