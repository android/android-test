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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.runner.JUnitCore.runClasses;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.rule.provider.ProviderTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.ui.app.provider.FlightsDatabaseContract.FlightsColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

/**
 * Tests of different APIs in {@link ProviderTestRule} based on the {@link FlightsContentProvider}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FlightsContentProviderByRuleTest {

  private static final String TAG = "FlightsCPByRuleTest";
  private static final String AUTHORITY = FlightsContentProvider.AUTHORITY;
  // "fixtureCmdsForTest" file contains database commands, creating the table if it does not exist,
  // and inserting three entries of test data including two "United" and one "American".
  private static final String TEST_CMD_FILE_NAME = "fixtureCmdsForTest";
  // "backupFlightsDB.db" is a database file including two "United" and one "American" entries.
  private static final String TEST_BACKUP_DB_DATA_FILE_NAME = "backupFlightsDB.db";
  private static final String INSERT_FIRST_UNITED_COMMAND =
      FlightsDatabaseContract.SQL_INSERTION_COMMAND_PREFIX
          + "(1469502629418, 'United', 12, 'Y', 'DCA', 'ORD')";
  private static final String INSERT_AMERICAN_COMMAND =
      FlightsDatabaseContract.SQL_INSERTION_COMMAND_PREFIX
          + "(1470374129880, 'American', 42, 'S', 'ORD', 'SFO')";
  private static final String INSERT_SECOND_UNITED_COMMAND =
      FlightsDatabaseContract.SQL_INSERTION_COMMAND_PREFIX
          + "(1470374529880, 'United', 380, 'Y', 'SFO', 'IAD')";
  private static final String INSERT_DELTA_COMMAND =
      FlightsDatabaseContract.SQL_INSERTION_COMMAND_PREFIX
          + "(1470374129125, 'Delta', 13, 'S', 'ORD', 'DCA')";

  private static final Uri URI_DIR = Uri.EMPTY.buildUpon().scheme("content")
      .authority(AUTHORITY).path(FlightsDatabaseContract.PATH).build();

  private static File testFolder;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * Creates the file containing commands to test method
   * {@link ProviderTestRule.Builder#setDatabaseCommandsFile}, and file containing database
   * data to test method {@link ProviderTestRule.Builder#setDatabaseFile}.
   */
  @Before
  public void createTestFiles() throws IOException {
    testFolder = temp.newFolder("test");

    File cmdTestFile = new File(testFolder, TEST_CMD_FILE_NAME);
    File backupDbFile = new File(testFolder, TEST_BACKUP_DB_DATA_FILE_NAME);

    copyAssetFile("testfiles/" + TEST_CMD_FILE_NAME, cmdTestFile);
    copyAssetFile("testfiles/" + TEST_BACKUP_DB_DATA_FILE_NAME, backupDbFile);
  }

  public static class VerifySuccessfulUseOfWithDBCmdsBuilderTest {

    private ContentResolver testResolver;
    private final String[] testInitCmds =
        new String[]{
            FlightsDatabaseContract.SQL_CREATE_FLIGHT_TABLE_COMMAND,
            INSERT_FIRST_UNITED_COMMAND,
            INSERT_AMERICAN_COMMAND,
            INSERT_SECOND_UNITED_COMMAND
        };

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY)
            .setDatabaseCommands(FlightsDatabaseContract.DB_NAME, testInitCmds)
            .build();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyThreeEntriesInsertedByDBCmds() {
      assertAirlineEntryCount(testResolver, "United", 2);
      assertAirlineEntryCount(testResolver, "American", 1);
    }
  }

  @Test
  public void verifySuccessfulUseOfWithDBCmdsBuilder() {
    Result result = runClasses(VerifySuccessfulUseOfWithDBCmdsBuilderTest.class);
    assertEquals(result.getFailureCount(), 0);
  }

  public static class VerifySuccessfulUseOfWithDBCmdsFromFileBuilderTest {

    private ContentResolver testResolver;

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY)
            .setDatabaseCommandsFile(FlightsDatabaseContract.DB_NAME,
                new File(testFolder, TEST_CMD_FILE_NAME))
            .build();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyThreeEntriesInsertedByDBCmdsFromFile() {
      assertAirlineEntryCount(testResolver, "United", 2);
      assertAirlineEntryCount(testResolver, "American", 1);
    }
  }

  @Test
  public void verifySuccessfulUseOfWithDBCmdsFromFileBuilder() {
    Result result = runClasses(VerifySuccessfulUseOfWithDBCmdsFromFileBuilderTest.class);
    assertEquals(result.getFailureCount(), 0);
  }

  public static class VerifySuccessfulUseOfWithDBDataFromFileBuilderTest {

    private ContentResolver testResolver;

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY)
            .setDatabaseFile(FlightsDatabaseContract.DB_NAME,
                new File(testFolder, TEST_BACKUP_DB_DATA_FILE_NAME))
            .build();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyThreeEntriesRestoredFromBackupDBFile() {
      assertAirlineEntryCount(testResolver, "United", 2);
      assertAirlineEntryCount(testResolver, "American", 1);
    }
  }

  @Test
  public void verifySuccessfulUseOfWithDBDataFromFileBuilder() {
    Result result = runClasses(VerifySuccessfulUseOfWithDBDataFromFileBuilderTest.class);
    assertEquals(result.getFailureCount(), 0);
  }

  public static class VerifySuccessOfWithDBDataAndWithCmdsBuilderTest {

    private ContentResolver testResolver;

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY)
            .setDatabaseFile(FlightsDatabaseContract.DB_NAME,
                new File(testFolder, TEST_BACKUP_DB_DATA_FILE_NAME))
            .setDatabaseCommands(FlightsDatabaseContract.DB_NAME, INSERT_DELTA_COMMAND)
            .build();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyThreeEntriesRestoredAndOneEntryInserted() {
      // To verify database commands run after data restoration
      assertAirlineEntryCount(testResolver, "United", 2);
      assertAirlineEntryCount(testResolver, "American", 1);
      assertAirlineEntryCount(testResolver, "Delta", 1);
    }
  }

  @Test
  public void verifySuccessOfWithDBDataAndWithCmdsBuilder() {
    Result result = runClasses(VerifySuccessOfWithDBDataAndWithCmdsBuilderTest.class);
    assertEquals(result.getFailureCount(), 0);
  }

  public static class VerifySuccessfulUseOfRunDBCommandsTest {

    private ContentResolver testResolver;

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY).build();

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyCorrectAmountsBeforeAndAfterCommands() {
      String[] insertCmds =
          new String[]{
              INSERT_FIRST_UNITED_COMMAND,
              INSERT_AMERICAN_COMMAND,
              INSERT_SECOND_UNITED_COMMAND
          };
      // Before running commands, no entry in Database
      assertAirlineEntryCount(testResolver, "United", 0);
      assertAirlineEntryCount(testResolver, "American", 0);
      providerRule.runDatabaseCommands(FlightsDatabaseContract.DB_NAME, insertCmds);
      // Verify two "United" entries and one "American" entry inserted
      assertAirlineEntryCount(testResolver, "United", 2);
      assertAirlineEntryCount(testResolver, "American", 1);
    }

    @Test
    public void verifyRunInvalidCommandsRaiseException() {
      expected.expect(SQLiteException.class);
      providerRule.runDatabaseCommands(FlightsDatabaseContract.DB_NAME, "invalid");
    }
  }

  @Test
  public void verifySuccessfulUseOfRunDBCommands() {
    Result result = runClasses(VerifySuccessfulUseOfRunDBCommandsTest.class);
    assertEquals(result.getFailureCount(), 0);
  }

  public static class FailedRestoreDBDataWhenInputDBDataFileNotFoundTest {

    private ContentResolver testResolver;

    @Rule
    public ProviderTestRule providerRule =
        new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY)
            .setDatabaseFile(FlightsDatabaseContract.DB_NAME,
                new File(testFolder, "non-existed.db"))
            .build();

    @Before
    public void init() {
      testResolver = providerRule.getResolver();
    }

    @Test
    public void verifyNeverReached() {
      assertAirlineEntryCount(testResolver, "United", 2);
      fail("should not reach here!");
    }
  }

  @Test
  public void failedRestoreDBDataWhenInputDBDataFileNotFound() {
    // Because database file to restore does not exist.
    Result result = runClasses(FailedRestoreDBDataWhenInputDBDataFileNotFoundTest.class);
    assertEquals(result.getFailureCount(), 1);
    assertTrue(result.getFailures().get(0).getMessage()
        .contains("doesn't exist"));
  }

  private static void assertAirlineEntryCount(ContentResolver resolver, String airline,
      int expectedCount) {
    String where = FlightsColumns.FLIGHT_AIRLINE + "=?";
    String[] args = {airline};

    Cursor c = null;
    try {
      c = resolver.query(URI_DIR, null, where, args, null);
      assertNotNull(c);
      assertEquals(expectedCount, c.getCount());
    } finally {
      closeCursor(c);
    }
  }

  private static void copyAssetFile(String assetFilePath, File destFile) throws IOException {
    InputStream in = null;
    OutputStream out = null;

    try {
      in = InstrumentationRegistry.getContext().getAssets().open(assetFilePath);
      out = new FileOutputStream(destFile);
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    } catch (IOException e) {
      Log.e(TAG, String.format("error copying asset file from %s to %s", assetFilePath, destFile));
      throw e;
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  private static void closeCursor(Cursor cursor) {
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
  }
}
