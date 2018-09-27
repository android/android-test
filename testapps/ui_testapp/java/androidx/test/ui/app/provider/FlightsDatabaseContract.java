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

package androidx.test.ui.app.provider;

import android.provider.BaseColumns;

/**
 * Defines table name, commands used by {@link FlightsContentProvider} and tests.
 */
final class FlightsDatabaseContract {

  public static final int DB_VERSION = 1;
  public static final String DB_NAME = "flightsDB.db";
  public static final String PATH = "flights";
  public static final String TABLE_NAME = "flights";

  public static final String SQL_CREATE_FLIGHT_TABLE_COMMAND =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
          + " (" + FlightsColumns._ID + " INTEGER PRIMARY KEY, "
          + FlightsColumns.FLIGHT_TIME + " INTEGER, "
          + FlightsColumns.FLIGHT_AIRLINE + " TEXT, "
          + FlightsColumns.FLIGHT_NUMBER + " INTEGER, "
          + FlightsColumns.FLIGHT_CUSTOMER + " TEXT, "
          + FlightsColumns.FLIGHT_SOURCE + " TEXT, "
          + FlightsColumns.FLIGHT_DESTINATION + " TEXT"
          + ");";

  public static final String SQL_DROP_FLIGHT_TABLE_COMMAND =
      "DROP TABLE IF EXISTS " + TABLE_NAME;

  public static final String SQL_INSERTION_COMMAND_PREFIX =
      "INSERT INTO " + TABLE_NAME
          + "(" + FlightsColumns.FLIGHT_TIME + ","
          + FlightsColumns.FLIGHT_AIRLINE + ","
          + FlightsColumns.FLIGHT_NUMBER + ","
          + FlightsColumns.FLIGHT_CUSTOMER + ","
          + FlightsColumns.FLIGHT_SOURCE + ","
          + FlightsColumns.FLIGHT_DESTINATION + ") VALUES ";

  /**
   * Defines the table column names.
   */
  public static class FlightsColumns implements BaseColumns {

    // table columns
    public static final String FLIGHT_TIME = "time";
    public static final String FLIGHT_AIRLINE = "airline";
    public static final String FLIGHT_NUMBER = "number";
    public static final String FLIGHT_CUSTOMER = "customer";
    public static final String FLIGHT_SOURCE = "src";
    public static final String FLIGHT_DESTINATION = "dest";
  }
}
