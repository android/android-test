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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import androidx.test.ui.app.provider.FlightsDatabaseContract.FlightsColumns;

/**
 * A ContentProvider to provide access to flight itinerary entries. This provider is implemented
 * based on underlying SQLite database.
 */
public final class FlightsContentProvider extends ContentProvider {

  private static final String TAG = "FlightsContentProvider";

  public static final String AUTHORITY =
      "androidx.test.ui.app.flights";
  public static final Uri CONTENT_URI =
      Uri.parse("content://" + AUTHORITY + "/" + FlightsDatabaseContract.PATH);
  public static final String TYPE_FLIGHT_DIR =
      "vnd.android.cursor.dir/vnd.google.android.apps.common.testing.ui.testapp.flights";
  public static final String TYPE_FLIGHT_ITEM =
      "vnd.android.cursor.item/vnd.google.android.apps.common.testing.ui.testapp.flights";

  private static final int FLIGHTS_ALL = 1;
  private static final int FLIGHT_ONE = 2;
  private static final long INVALID_ID = -1L;
  private static final UriMatcher URI_MATCHER = createUriMatcher();

  private Context context;
  private DatabaseHelper dbHelper;

  @Override
  public boolean onCreate() {
    context = getContext();
    checkNotNull(context);
    dbHelper = new DatabaseHelper(context);
    return true;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    checkNotNull(uri);
    switch (URI_MATCHER.match(uri)) {
      case FLIGHTS_ALL:
        return TYPE_FLIGHT_DIR;
      case FLIGHT_ONE:
        return TYPE_FLIGHT_ITEM;
      default:
        return null;
    }
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
    checkNotNull(uri);
    checkArgument(FLIGHTS_ALL == URI_MATCHER.match(uri), "Unsupported Uri: " + uri.toString());
    try {
      SQLiteDatabase database = dbHelper.getWritableDatabase();
      long id = database.insertOrThrow(FlightsDatabaseContract.TABLE_NAME, "", contentValues);
      if (id != INVALID_ID) {
        Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
        context.getContentResolver().notifyChange(newUri, null);
        return newUri;
      } else {
        String errorMessage = "SQL insertion return -1 when inserting new entry to " + uri;
        Log.e(TAG, errorMessage);
        throw new SQLException(errorMessage);
      }
    } catch (SQLException e) {
      Log.e(TAG, "SQLException occurred when inserting new entry to " + uri, e);
      throw e;
    }
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String where, String[] args,
      String sortOrder) {
    checkNotNull(uri);
    SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
    sqlBuilder.setTables(FlightsDatabaseContract.TABLE_NAME);
    switch (URI_MATCHER.match(uri)) {
      case FLIGHTS_ALL:
        break;
      case FLIGHT_ONE:
        sqlBuilder.appendWhere(FlightsColumns._ID + " = " + uri.getLastPathSegment());
        break;
      default:
        throw new IllegalArgumentException("Unrecognized uri: " + uri);
    }
    String sort = TextUtils.isEmpty(sortOrder) ? FlightsColumns.FLIGHT_CUSTOMER : sortOrder;
    SQLiteDatabase database = dbHelper.getWritableDatabase();

    Cursor c = sqlBuilder.query(database, projection, where, args, null, null, sort);
    c.setNotificationUri(context.getContentResolver(), uri);
    return c;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues contentValues, String where, String[] args) {
    checkNotNull(uri);
    int count;
    SQLiteDatabase database;
    switch (URI_MATCHER.match(uri)) {
      case FLIGHTS_ALL:
        database = dbHelper.getWritableDatabase();
        count = database.update(FlightsDatabaseContract.TABLE_NAME, contentValues, where, args);
        break;
      case FLIGHT_ONE:
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(FlightsColumns._ID).append(" = ").append(uri.getLastPathSegment());
        if (!TextUtils.isEmpty(where)) {
          whereBuilder.append(" AND ").append('(').append(where).append(')');
        }
        database = dbHelper.getWritableDatabase();
        count = database.update(FlightsDatabaseContract.TABLE_NAME, contentValues,
            whereBuilder.toString(), args);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized uri: " + uri);
    }
    if (count > 0) {
      context.getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  @Override
  public int delete(@NonNull Uri uri, String where, String[] args) {
    checkNotNull(uri);
    int count;
    SQLiteDatabase database;
    switch (URI_MATCHER.match(uri)) {
      case FLIGHTS_ALL:
        database = dbHelper.getWritableDatabase();
        count = database.delete(FlightsDatabaseContract.TABLE_NAME, where, args);
        break;
      case FLIGHT_ONE:
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(FlightsColumns._ID).append(" = ").append(uri.getLastPathSegment());
        if (!TextUtils.isEmpty(where)) {
          whereBuilder.append(" AND ").append('(').append(where).append(')');
        }
        database = dbHelper.getWritableDatabase();
        count = database.delete(FlightsDatabaseContract.TABLE_NAME, whereBuilder.toString(), args);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized uri : " + uri);
    }
    if (count > 0) {
      context.getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  private static UriMatcher createUriMatcher() {
    UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(AUTHORITY, "flights", FLIGHTS_ALL);
    uriMatcher.addURI(AUTHORITY, "flights/#", FLIGHT_ONE);
    return uriMatcher;
  }

  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, FlightsDatabaseContract.DB_NAME, null, FlightsDatabaseContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(FlightsDatabaseContract.SQL_CREATE_FLIGHT_TABLE_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion != newVersion) {
        db.execSQL(FlightsDatabaseContract.SQL_DROP_FLIGHT_TABLE_COMMAND);
        onCreate(db);
      }
    }
  }
}
