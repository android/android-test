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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest.permission;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.rule.provider.ProviderTestRule;
import androidx.test.runner.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests of {@link SimpleFileContentProvider} using {@link ProviderTestRule}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SimpleFileContentProviderTest {

  private static final String TAG = "SimpleFileCPTest";
  private static final String AUTHORITY =
      "androidx.test.ui.app.files";
  private static final File TEST_HOSTED_DIRECTORY =
      new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "testDir");
  private ContentResolver testResolver;

  @Rule
  public ProviderTestRule providerRule =
      new ProviderTestRule.Builder(TestSimpleFileContentProvider.class, AUTHORITY).build();

  @Before
  public void init() {
    testResolver = providerRule.getResolver();
  }

  @After
  public void deleteTestFilesAndDir() {
    recursiveDelete(TEST_HOSTED_DIRECTORY);
  }

  @Test
  public void getTypeCorrectly() {
    Uri txtUri = buildUri("a-txt-file.txt");
    String txtPlainType = "text/plain";
    assertEquals(txtPlainType, testResolver.getType(txtUri));

    Uri jpgUri = buildUri("a-jpg-file.jpg");
    String imageJpegType = "image/jpeg";
    assertEquals(imageJpegType, testResolver.getType(jpgUri));
  }

  @Test
  public void readExistingFileCorrectly() throws IOException {
    File testFile = new File(TEST_HOSTED_DIRECTORY, "existing.txt");
    assertTrue("Could not create test file", testFile.createNewFile());
    String contentToFile = "Happy testing!";
    writeToOutputStream(contentToFile, new FileOutputStream(testFile));

    ParcelFileDescriptor fileDescriptor =
        testResolver.openFileDescriptor(buildUri("existing.txt"), "r");
    String contentFromFile =
        readFromInputStream(new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor));
    assertEquals("File contents do not match!", contentToFile, contentFromFile);
  }

  @Test
  public void readNonExistingFileThrowsException() throws IOException {
    try {
      testResolver.openFileDescriptor(buildUri("non-existing.txt"), "r");
      fail("expected FileNotFoundException when opening non existing file");
    } catch (FileNotFoundException expected) {
      // expected, do nothing
    }
  }

  @Test
  public void writeToFileCorrectly() throws IOException {
    File expectedFile = new File(TEST_HOSTED_DIRECTORY, "test-data.txt");
    assertFalse("file should not exist in the beginning", expectedFile.exists());
    String contentToFile = "Happy testing!";
    ParcelFileDescriptor fileDescriptor =
        testResolver.openFileDescriptor(buildUri("test-data.txt"), "w");
    writeToOutputStream(contentToFile,
        new ParcelFileDescriptor.AutoCloseOutputStream(fileDescriptor));
    assertTrue("file should exist now", expectedFile.exists());
    String contentFromFile = readFromInputStream(new FileInputStream(expectedFile));
    assertEquals("File contents do not match!", contentToFile, contentFromFile);
  }

  @Test
  public void writeToFileInSubdirCorrectly() throws IOException {
    File expectedFile = new File(TEST_HOSTED_DIRECTORY, "subdir/test-data.txt");
    assertFalse("dir should not exist in the beginning", expectedFile.getParentFile().exists());
    assertFalse("file should not exist in the beginning", expectedFile.exists());
    String contentToFile = "Happy testing!";
    ParcelFileDescriptor fileDescriptor =
        testResolver.openFileDescriptor(buildUri("subdir/test-data.txt"), "w");
    writeToOutputStream(contentToFile,
        new ParcelFileDescriptor.AutoCloseOutputStream(fileDescriptor));
    assertTrue("dir should exist now", expectedFile.getParentFile().exists());
    assertTrue("file should exist now", expectedFile.exists());
    String contentFromFile = readFromInputStream(new FileInputStream(expectedFile));
    assertEquals("Contents do not match", contentToFile, contentFromFile);
  }

  @Test
  public void writeWithoutPermissionThrowsException() throws IOException {
    assertNotNull("We should be able to open file with permission granted by default",
        testResolver.openFileDescriptor(buildUri("test-data.txt"), "w"));
    providerRule.revokePermission(permission.WRITE_EXTERNAL_STORAGE);
    try {
      testResolver.openFileDescriptor(buildUri("test-data.txt"), "w");
      fail("expected SecurityException when permission is revoked");
    } catch (SecurityException expected) {
      // expected, do nothing
    }
  }

  @Test
  public void readOutsideHostedDirectoryThrowsException() throws IOException {
    File outsideDirectory = new File(TEST_HOSTED_DIRECTORY.getParentFile(), "outsideDir");
    Uri uriToOutsideDir = buildUri("../outsideDir/test-data.txt");
    try {
      testResolver.openFileDescriptor(uriToOutsideDir, "r");
      fail("expected SecurityException when opening file from outside directory");
    } catch (SecurityException expected) {
      // expected, do nothing
    }
    assertFalse("outside directory should not be created", outsideDirectory.exists());
  }

  @Test
  public void accessWithBadModeThrowsException() throws IOException {
    try {
      testResolver.openFileDescriptor(buildUri("test-data.txt"), "u");
      fail("expected IllegalArgumentException when passing in wrong mode");
    } catch (IllegalArgumentException expected) {
      // expected, do nothing
    }
  }

  private static Uri buildUri(String path) {
    return Uri.EMPTY.buildUpon()
        .scheme("content")
        .authority(AUTHORITY)
        .path(path)
        .build();
  }

  private static String readFromInputStream(InputStream inputStream) throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(inputStream, Charset.forName("UTF-8")));
    StringBuilder builder = new StringBuilder();
    String lineIn = null;
    while (null != (lineIn = reader.readLine())) {
      builder.append(lineIn);
    }
    return builder.toString();
  }

  private static void writeToOutputStream(String content, OutputStream outStream)
      throws IOException {
    try {
      outStream.write(content.getBytes("UTF-8"));
    } finally {
      outStream.close();
    }
  }

  private static void recursiveDelete(File file) {
    File[] files = file.listFiles();
    if (files != null) {
      for (File each : files) {
        recursiveDelete(each);
      }
    }
    if (!file.delete()) {
      Log.e(TAG, "Fail to delete file or directory " + file);
    }
  }

  public static class TestSimpleFileContentProvider extends SimpleFileContentProvider {

    public TestSimpleFileContentProvider() throws IOException {
      super(TEST_HOSTED_DIRECTORY);
    }
  }
}
