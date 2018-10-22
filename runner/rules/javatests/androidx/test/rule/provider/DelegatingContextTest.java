/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule.provider;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for the {@link DelegatingContext} class. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class DelegatingContextTest {

  private static final String TAG = "DelegatingContextTest";
  private static final String TEST_PREFIX = "test.";
  private static final String DUMMY_TEST_DIR_NAME = "dummy";
  private static final String DUMMY_TEST_FILE_NAME = "dummy.file";
  private static final String DUMMY_TEST_DB_NAME = "dummy.db";
  private static final Context context = getInstrumentation().getContext();
  private DelegatingContext delegatingContext;

  @Mock private ContentResolver mockResolver;

  @Before
  public void setUpMocksAndDelegatingContext() {
    MockitoAnnotations.initMocks(this);
    delegatingContext = new DelegatingContext(context, TEST_PREFIX, mockResolver);
  }

  @After
  public void cleanUpFiles() {
    delete(context.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE));
    delete(delegatingContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE));
    delete(context.getFileStreamPath(DUMMY_TEST_FILE_NAME));
    delete(delegatingContext.getFileStreamPath(DUMMY_TEST_FILE_NAME));
    delete(context.getDatabasePath(DUMMY_TEST_DB_NAME));
    delete(delegatingContext.getDatabasePath(DUMMY_TEST_DB_NAME));
  }

  @Test
  public void verifyDirIsRenamed() {
    File testDir = context.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE);
    File renamedDir = delegatingContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE);
    assertIsRenamed(testDir, renamedDir);
  }

  @Test
  public void verifyFileIsRenamed() {
    File testFile = context.getFileStreamPath(DUMMY_TEST_FILE_NAME);
    File renamedFile = delegatingContext.getFileStreamPath(DUMMY_TEST_FILE_NAME);
    assertIsRenamed(testFile, renamedFile);
  }

  @Test
  public void verifyDatabaseIsRenamed() {
    File testDatabase = context.getDatabasePath(DUMMY_TEST_DB_NAME);
    File renamedDatabase = delegatingContext.getDatabasePath(DUMMY_TEST_DB_NAME);
    assertIsRenamed(testDatabase, renamedDatabase);
  }

  @Test
  public void openFilesAndDelete() throws FileNotFoundException {
    // No file in context in the beginning
    assertEquals(delegatingContext.fileList().length, 0);

    // Open a file not visible in delegatingContext throws exception
    try {
      delegatingContext.openFileInput(DUMMY_TEST_FILE_NAME);
      fail("expected FileNotFoundException");
    } catch (FileNotFoundException expected) {
      // expected, do nothing
    }

    // openFileOutput will create file and add to delegatingContext
    delegatingContext.openFileOutput(DUMMY_TEST_FILE_NAME, MODE_PRIVATE);
    assertThat(Arrays.asList(delegatingContext.fileList()), hasItem(DUMMY_TEST_FILE_NAME));

    // Delete file will also remove it from context
    assertTrue(delegatingContext.deleteFile(DUMMY_TEST_FILE_NAME));
    assertEquals(delegatingContext.fileList().length, 0);
  }

  @Test
  public void openOrCreateDatabaseWithoutHandler() {
    // No db in context in the beginning
    assertEquals(delegatingContext.databaseList().length, 0);
    SQLiteDatabase testDatabase =
        context.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null);
    // openOrCreateDatabase will create database and add to delegatingContext
    SQLiteDatabase renamedDatabase =
        delegatingContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null);

    assertThat(Arrays.asList(delegatingContext.databaseList()), hasItem(DUMMY_TEST_DB_NAME));
    assertIsRenamed(new File(testDatabase.getPath()), new File(renamedDatabase.getPath()));

    testDatabase.close();
    renamedDatabase.close();

    delegatingContext.deleteDatabase(DUMMY_TEST_DB_NAME);
    assertEquals(delegatingContext.databaseList().length, 0);
  }

  /**
   * Similar to test {@link #openOrCreateDatabaseWithoutHandler}, but test openOrCreateDatabase API
   * with errorHandler, which is added in API level 11.
   */
  @Test
  @SdkSuppress(minSdkVersion = 11)
  public void openOrCreateDatabaseWithHandler() {
    assertEquals(delegatingContext.databaseList().length, 0);
    SQLiteDatabase testDatabase =
        context.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null, null);
    SQLiteDatabase renamedDatabase =
        delegatingContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null, null);

    assertThat(Arrays.asList(delegatingContext.databaseList()), hasItem(DUMMY_TEST_DB_NAME));
    assertIsRenamed(new File(testDatabase.getPath()), new File(renamedDatabase.getPath()));

    testDatabase.close();
    renamedDatabase.close();
  }

  @Test
  public void verifyRevokePermissionEffectively() {
    String permission = "TEST.DUMMY.PERMISSION";
    assertEquals(
        PackageManager.PERMISSION_GRANTED, delegatingContext.checkSelfPermission(permission));
    delegatingContext.addRevokedPermission(permission);
    assertEquals(
        PackageManager.PERMISSION_DENIED, delegatingContext.checkSelfPermission(permission));

    try {
      delegatingContext.enforceCallingPermission(permission, "have no permission" + permission);
      fail("expected SecurityException with permission revoked");
    } catch (SecurityException expected) {
      // expected, do nothing
    }
  }

  private static void assertIsRenamed(File realFile, File renamedFile) {
    String prefixToRemove = "app_";
    assertEquals(realFile.getParent(), renamedFile.getParent());
    assertEquals(
        TEST_PREFIX + realFile.getName().replaceFirst(prefixToRemove, ""),
        renamedFile.getName().replaceFirst(prefixToRemove, ""));
  }

  private static void delete(File file) {
    if (file.exists() && !file.delete()) {
      Log.e(TAG, String.format("File %s exists but fail to delete", file));
    }
  }
}
