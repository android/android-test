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
  private String dBName;
  private String[] dBCmds;
  private File dBCmdFile;
  private File dBDataFile;

  public DatabaseArgs(String dbName) {
    dBName = dbName;
  }

  /** @see ProviderTestRule.Builder#setDatabaseCommands(String, String...) */
  public void setDBCmds(String... dbCmds) {
    if (dBCmds != null) {
      Log.w(TAG, String.format("Commands for database %s already set", dBName));
    }
    dBCmds = dbCmds;
  }

  /** @see ProviderTestRule.Builder#setDatabaseCommandsFile(String, File) */
  public void setDBCmdFile(File dbCmdFile) {
    if (dBCmdFile != null) {
      Log.w(TAG, String.format("Command file for database %s already set", dBName));
    }
    dBCmdFile = dbCmdFile;
  }

  /** @see ProviderTestRule.Builder#setDatabaseFile(String, File) */
  public void setDBDataFile(File dbDataFile) {
    if (dBDataFile != null) {
      Log.w(TAG, String.format("Data file to restore for database %s already set", dBName));
    }
    dBDataFile = dbDataFile;
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

  public boolean hasDBCmds() {
    return (dBCmds != null);
  }

  public boolean hasDBCmdFile() {
    return (dBCmdFile != null);
  }

  public boolean hasDBDataFile() {
    return (dBDataFile != null);
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
}
