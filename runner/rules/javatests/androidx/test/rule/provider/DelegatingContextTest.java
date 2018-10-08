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
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
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
  private static final Context mContext = InstrumentationRegistry.getContext();
  private DelegatingContext mDelegatingContext;

  @Mock private ContentResolver mMockResolver;

  @Before
  public void setUpMocksAndDelegatingContext() {
    MockitoAnnotations.initMocks(this);
    mDelegatingContext = new DelegatingContext(mContext, TEST_PREFIX, mMockResolver);
  }

  @After
  public void cleanUpFiles() {
    delete(mContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE));
    delete(mDelegatingContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE));
    delete(mContext.getFileStreamPath(DUMMY_TEST_FILE_NAME));
    delete(mDelegatingContext.getFileStreamPath(DUMMY_TEST_FILE_NAME));
    delete(mContext.getDatabasePath(DUMMY_TEST_DB_NAME));
    delete(mDelegatingContext.getDatabasePath(DUMMY_TEST_DB_NAME));
  }

  @Test
  public void verifyDirIsRenamed() {
    File testDir = mContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE);
    File renamedDir = mDelegatingContext.getDir(DUMMY_TEST_DIR_NAME, MODE_PRIVATE);
    assertIsRenamed(testDir, renamedDir);
  }

  @Test
  public void verifyFileIsRenamed() {
    File testFile = mContext.getFileStreamPath(DUMMY_TEST_FILE_NAME);
    File renamedFile = mDelegatingContext.getFileStreamPath(DUMMY_TEST_FILE_NAME);
    assertIsRenamed(testFile, renamedFile);
  }

  @Test
  public void verifyDatabaseIsRenamed() {
    File testDatabase = mContext.getDatabasePath(DUMMY_TEST_DB_NAME);
    File renamedDatabase = mDelegatingContext.getDatabasePath(DUMMY_TEST_DB_NAME);
    assertIsRenamed(testDatabase, renamedDatabase);
  }

  @Test
  public void openFilesAndDelete() throws FileNotFoundException {
    // No file in context in the beginning
    assertEquals(mDelegatingContext.fileList().length, 0);

    // Open a file not visible in mDelegatingContext throws exception
    try {
      mDelegatingContext.openFileInput(DUMMY_TEST_FILE_NAME);
      fail("expected FileNotFoundException");
    } catch (FileNotFoundException expected) {
      // expected, do nothing
    }

    // openFileOutput will create file and add to mDelegatingContext
    mDelegatingContext.openFileOutput(DUMMY_TEST_FILE_NAME, MODE_PRIVATE);
    assertThat(Arrays.asList(mDelegatingContext.fileList()), hasItem(DUMMY_TEST_FILE_NAME));

    // Delete file will also remove it from context
    assertTrue(mDelegatingContext.deleteFile(DUMMY_TEST_FILE_NAME));
    assertEquals(mDelegatingContext.fileList().length, 0);
  }

  @Test
  public void openOrCreateDatabaseWithoutHandler() {
    // No db in context in the beginning
    assertEquals(mDelegatingContext.databaseList().length, 0);
    SQLiteDatabase testDatabase =
        mContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null);
    // openOrCreateDatabase will create database and add to mDelegatingContext
    SQLiteDatabase renamedDatabase =
        mDelegatingContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null);

    assertThat(Arrays.asList(mDelegatingContext.databaseList()), hasItem(DUMMY_TEST_DB_NAME));
    assertIsRenamed(new File(testDatabase.getPath()), new File(renamedDatabase.getPath()));

    testDatabase.close();
    renamedDatabase.close();

    mDelegatingContext.deleteDatabase(DUMMY_TEST_DB_NAME);
    assertEquals(mDelegatingContext.databaseList().length, 0);
  }

  /**
   * Similar to test {@link #openOrCreateDatabaseWithoutHandler}, but test openOrCreateDatabase API
   * with errorHandler, which is added in API level 11.
   */
  @Test
  @SdkSuppress(minSdkVersion = 11)
  public void openOrCreateDatabaseWithHandler() {
    assertEquals(mDelegatingContext.databaseList().length, 0);
    SQLiteDatabase testDatabase =
        mContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null, null);
    SQLiteDatabase renamedDatabase =
        mDelegatingContext.openOrCreateDatabase(DUMMY_TEST_DB_NAME, MODE_PRIVATE, null, null);

    assertThat(Arrays.asList(mDelegatingContext.databaseList()), hasItem(DUMMY_TEST_DB_NAME));
    assertIsRenamed(new File(testDatabase.getPath()), new File(renamedDatabase.getPath()));

    testDatabase.close();
    renamedDatabase.close();
  }

  @Test
  public void verifyRevokePermissionEffectively() {
    String permission = "TEST.DUMMY.PERMISSION";
    assertEquals(
        PackageManager.PERMISSION_GRANTED, mDelegatingContext.checkSelfPermission(permission));
    mDelegatingContext.addRevokedPermission(permission);
    assertEquals(
        PackageManager.PERMISSION_DENIED, mDelegatingContext.checkSelfPermission(permission));

    try {
      mDelegatingContext.enforceCallingPermission(permission, "have no permission" + permission);
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
