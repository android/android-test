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

import android.util.Log;
import java.io.File;

/**
 * A class that stores all arguments passed in for a database used by a {@code ContentProvider}
 * under test for {@link ProviderTestRule}. The arguments include database name, database commands
 * to run, the file containing database commands or the file containing data to restore.
 *
 * @see ProviderTestRule.Builder#setDatabaseCommands(String, String...)
 * @see ProviderTestRule.Builder#setDatabaseCommandsFile(String, File)
 * @see ProviderTestRule.Builder#setDatabaseFile(String, File)
 */
final class DatabaseArgs {

  private static final String TAG = "DatabaseArgs";
  private String mDBName;
  private String[] mDBCmds;
  private File mDBCmdFile;
  private File mDBDataFile;

  public DatabaseArgs(String dbName) {
    mDBName = dbName;
  }

  /** @see ProviderTestRule.Builder#setDatabaseCommands(String, String...) */
  public void setDBCmds(String... dbCmds) {
    if (mDBCmds != null) {
      Log.w(TAG, String.format("Commands for database %s already set", mDBName));
    }
    mDBCmds = dbCmds;
  }

  /** @see ProviderTestRule.Builder#setDatabaseCommandsFile(String, File) */
  public void setDBCmdFile(File dbCmdFile) {
    if (mDBCmdFile != null) {
      Log.w(TAG, String.format("Command file for database %s already set", mDBName));
    }
    mDBCmdFile = dbCmdFile;
  }

  /** @see ProviderTestRule.Builder#setDatabaseFile(String, File) */
  public void setDBDataFile(File dbDataFile) {
    if (mDBDataFile != null) {
      Log.w(TAG, String.format("Data file to restore for database %s already set", mDBName));
    }
    mDBDataFile = dbDataFile;
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

  public boolean hasDBCmds() {
    return (mDBCmds != null);
  }

  public boolean hasDBCmdFile() {
    return (mDBCmdFile != null);
  }

  public boolean hasDBDataFile() {
    return (mDBDataFile != null);
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
}
