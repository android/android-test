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
  private final String mAuthority;
  private final Class<? extends ContentProvider> mProviderClass;
  private String mDBName;
  private String[] mDBCmds;
  private File mDBCmdFile;
  private File mDBDataFile;
  private WeakReference<ContentProvider> mProviderRef;

  /**
   * @see ProviderTestRule.Builder#Builder(String, Class)
   * @see ProviderTestRule.Builder#addProvider(String, Class)
   */
  public ProviderArgs(String authority, Class<? extends ContentProvider> providerClass) {
    mAuthority = authority;
    mProviderClass = providerClass;
  }

  /** @see ProviderTestRule.Builder#withDBName(String, String) */
  public void setDBName(String dbName) {
    if (mDBName != null) {
      Log.w(
          TAG,
          String.format(
              "Database name for ContentProvider " + "with authority %s already exists",
              mAuthority));
    }
    mDBName = dbName;
  }

  /** @see ProviderTestRule.Builder#withDBCmds(String, String...) */
  public void setDBCmds(String... dbCmds) {
    if (mDBCmds != null) {
      Log.w(
          TAG,
          String.format(
              "Database commands for ContentProvider " + "with authority %s already set",
              mAuthority));
    }
    mDBCmds = dbCmds;
  }

  /** @see ProviderTestRule.Builder#withDBCmdsFromFile(String, File) */
  public void setDBCmdFile(File dbCmdFile) {
    if (mDBCmdFile != null) {
      Log.w(
          TAG,
          String.format(
              "Database command file for ContentProvider " + "with authority %s already set",
              mAuthority));
    }
    mDBCmdFile = dbCmdFile;
  }

  /** @see ProviderTestRule.Builder#withDBDataFromFile(String, File) */
  public void setDBDataFile(File dbDataFile) {
    if (mDBDataFile != null) {
      Log.w(
          TAG,
          String.format(
              "Database file to restore for ContentProvider " + "with authority %s already set",
              mAuthority));
    }
    mDBDataFile = dbDataFile;
  }

  /** Pass in the provider instance to hold a weak reference to it. */
  public void setProviderRef(ContentProvider provider) {
    if (mProviderRef != null) {
      Log.w(
          TAG,
          String.format(
              "Reference to Provider instance " + "with authority %s already set", mAuthority));
    }
    mProviderRef = new WeakReference<>(provider);
  }

  /**
   * Pass in extra list of database commands and merge with the existing database commands passed in
   * by {@link #setDBCmds}. If the existing commands is {@code null}, the newly passed in commands
   * is directly assigned to the existing commands.
   *
   * @param dbCmds extra list of database commands to merge with existing database commands.
   */
  public void addDBCmds(String... dbCmds) {
    if (null == mDBCmds) {
      mDBCmds = dbCmds;
    } else {
      String[] newCmds = new String[mDBCmds.length + dbCmds.length];
      System.arraycopy(mDBCmds, 0, newCmds, 0, mDBCmds.length);
      System.arraycopy(dbCmds, 0, newCmds, mDBCmds.length, dbCmds.length);
      mDBCmds = newCmds;
    }
  }

  public boolean hasDBName() {
    return (mDBName != null);
  }

  public boolean hasDBCmds() {
    return (mDBCmds != null);
  }

  public boolean hasDBCmdFile() {
    return (mDBCmdFile != null);
  }

  public boolean hasDBDataFile() {
    return (mDBDataFile != null);
  }

  public Class<? extends ContentProvider> getProviderClass() {
    return mProviderClass;
  }

  public String getAuthority() {
    return mAuthority;
  }

  public String getDBName() {
    return mDBName;
  }

  public String[] getDBCmds() {
    return mDBCmds;
  }

  public File getDBCmdFile() {
    return mDBCmdFile;
  }

  public File getDBDataFile() {
    return mDBDataFile;
  }

  public ContentProvider getProvider() {
    if (mProviderRef != null) {
      return mProviderRef.get();
    } else {
      return null;
    }
  }
}
