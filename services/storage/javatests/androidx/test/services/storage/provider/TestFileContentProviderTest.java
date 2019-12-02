/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.services.storage.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.test.ProviderTestCase2;
import android.test.suitebuilder.annotation.Suppress;
import androidx.test.services.storage.file.HostedFile;
import androidx.test.services.storage.provider.AbstractFileContentProvider.Access;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * Unit tests for {@link TestFileContentProvider}.
 *
 * TODO(b/145236542): Converts the tests to JUnit4.
 */
public class TestFileContentProviderTest
    extends ProviderTestCase2<TestFileContentProviderTest.TestFileContentProvider> {

  private static final Object TEST_RESOLVER_INIT_LOCK = new Object();
  private static final File BOGUS_DIRECTORY =
      new File(Environment.getExternalStorageDirectory(), "fcp_test/bogus");
  private static volatile File resolverHostedDirectory = BOGUS_DIRECTORY;
  private static volatile Access resolverAccess = Access.READ_ONLY;
  private static volatile Predicate<Void> resolverOnCreateHookResult = Predicates.alwaysTrue();
  private static final String TEST_AUTHORITY = "test_files";
  private static final String[] EMPTY_ARRAY = {};

  private Access access = Access.READ_ONLY;
  private File hostedDirectory;
  private ContentResolver resolver;

  public TestFileContentProviderTest() {
    super(TestFileContentProvider.class, TEST_AUTHORITY);
  }

  @Override
  public void setUp() throws Exception {
    hostedDirectory = new File(Environment.getExternalStorageDirectory(), "fcp_test/" + getName());
    hostedDirectory.getParentFile().mkdirs();
    access = Access.READ_ONLY;
    super.setUp();
    initResolver();
  }

  private Uri makeUri(String path) {
    return new Uri.Builder().scheme("content").authority(TEST_AUTHORITY).path(path).build();
  }

  public void testReadFile() throws Exception {
    File testFile = new File(hostedDirectory, "test-data.txt");
    assertTrue("Couldnt create test file.", testFile.createNewFile());
    write("hello world", testFile, Charsets.UTF_8);
    ParcelFileDescriptor providerFile = resolver.openFileDescriptor(makeUri("test-data.txt"), "r");
    String contents = read(providerFile, Charsets.UTF_8);
    assertEquals("Unexpected file contents!", "hello world", contents);
  }

  public void testReadFile_WithNameThatNeedsEncoding() throws Exception {
    File testFile = new File(hostedDirectory, "oh wow & aren't modern day file-systems [SO] great");
    assertTrue("Couldnt create test file.", testFile.createNewFile());
    write("8 ascii chars and a 3 letter extension", testFile, Charsets.UTF_8);
    ParcelFileDescriptor providerFile =
        resolver.openFileDescriptor(
            makeUri("oh wow & aren't modern day file-systems [SO] great"), "r");
    String contents = read(providerFile, Charsets.UTF_8);
    assertEquals("Unexpected file contents!", "8 ascii chars and a 3 letter extension", contents);
  }

  public void testRead_DoesNotExist() throws Exception {
    try {
      resolver.openFileDescriptor(makeUri("does-not-exist.txt"), "r");
      fail("file doesnt exist, shouldnt be able to open it.");
    } catch (FileNotFoundException expected) {
      /*expected*/
    }
  }

  public void testWrite_OnReadOnlyFileSystem() throws Exception {
    File testFile = new File(hostedDirectory, "test-data.txt");
    assertTrue("Couldnt create test file.", testFile.createNewFile());
    try {
      resolver.openFileDescriptor(makeUri("test-data.txt"), "w");
      fail("shouldnt be able to write to ro fs");
    } catch (SecurityException expected) {
      /*expected*/
    }
  }

  public void testWrite_FileInExistingDirectory() throws Exception {
    access = Access.READ_WRITE;
    initResolver();
    ParcelFileDescriptor providerFile = resolver.openFileDescriptor(makeUri("test-data.txt"), "w");
    write(
        "hello world",
        new ParcelFileDescriptor.AutoCloseOutputStream(providerFile),
        Charsets.UTF_8);
    File expectedFile = new File(hostedDirectory, "test-data.txt");
    assertTrue("file not in expected place.", expectedFile.exists());
    String fileContent = read(expectedFile, Charsets.UTF_8);
    assertEquals("contents unexpected", "hello world", fileContent);
  }

  public void testWriteFile_WithNameThatNeedsEncoding() throws Exception {
    access = Access.READ_WRITE;
    initResolver();
    ParcelFileDescriptor providerFile =
        resolver.openFileDescriptor(
            makeUri("oh wow & aren't modern day file-systems [SO] great"), "w");
    write(
        "hello world",
        new ParcelFileDescriptor.AutoCloseOutputStream(providerFile),
        Charsets.UTF_8);
    File expectedFile =
        new File(hostedDirectory, "oh wow & aren't modern day file-systems [SO] great");
    assertTrue("file not in expected place.", expectedFile.exists());
    String fileContent = read(expectedFile, Charsets.UTF_8);
    assertEquals("contents unexpected", "hello world", fileContent);
  }

  public void testWrite_FileInNewDirectory() throws Exception {
    access = Access.READ_WRITE;
    initResolver();
    ParcelFileDescriptor providerFile =
        resolver.openFileDescriptor(makeUri("subdir/test-data.txt"), "w");
    write(
        "hello world",
        new ParcelFileDescriptor.AutoCloseOutputStream(providerFile),
        Charsets.UTF_8);
    File expectedFile = new File(hostedDirectory, "subdir/test-data.txt");
    assertTrue("file not in expected place.", expectedFile.exists());
    String fileContent = read(expectedFile, Charsets.UTF_8);
    assertEquals("contents unexpected", "hello world", fileContent);
  }

  public void testWrite_RelativePath() throws Exception {
    access = Access.READ_WRITE;
    initResolver();
    ParcelFileDescriptor providerFile =
        resolver.openFileDescriptor(makeUri("subdir/../test-data.txt"), "w");
    write(
        "hello world",
        new ParcelFileDescriptor.AutoCloseOutputStream(providerFile),
        Charsets.UTF_8);
    File expectedFile = new File(hostedDirectory, "test-data.txt");
    assertTrue("file not in expected place.", expectedFile.exists());
    String fileContent = read(expectedFile, Charsets.UTF_8);
    assertEquals("contents unexpected", "hello world", fileContent);
  }

  public void testWrite_OutsideHostedDirectory() throws Exception {
    hostedDirectory = new File(hostedDirectory, "resolver_dir");
    hostedDirectory.mkdirs();
    access = Access.READ_WRITE;
    initResolver();
    try {
      resolver.openFileDescriptor(makeUri("../test-data.txt"), "w");
      fail("shouldnt be able to write outside of hosted directory.");
    } catch (SecurityException expected) {
      /*expected*/
    }
    assertFalse(new File(hostedDirectory.getParent(), "test-data.txt").exists());
  }

  public void testRead_OutsideHostedDirectory() throws Exception {
    hostedDirectory = new File(hostedDirectory, "resolver_dir");
    hostedDirectory.mkdirs();
    initResolver();
    write("secrets", new File(hostedDirectory.getParent(), "secret.dat"), Charsets.UTF_8);
    try {
      resolver.openFileDescriptor(makeUri("../secrets.dat"), "w");
      fail("shouldnt be able to write outside of hosted directory.");
    } catch (SecurityException expected) {
      /*expected*/
    }
  }

  public void testReadAndWrite() throws Exception {
    access = Access.READ_WRITE;
    initResolver();
    ParcelFileDescriptor providerFile = resolver.openFileDescriptor(makeUri("test-data.txt"), "w");
    write(
        "hello world",
        new ParcelFileDescriptor.AutoCloseOutputStream(providerFile),
        Charsets.UTF_8);
    ParcelFileDescriptor readInFile = resolver.openFileDescriptor(makeUri("test-data.txt"), "r");
    String fileContent = read(readInFile, Charsets.UTF_8);
    assertEquals("cannot read content back", "hello world", fileContent);
  }

  @Suppress
  public void testQueryDirectory() throws Exception {
    write("file1 contents", new File(hostedDirectory, "file1.txt"), Charsets.UTF_8);
    write("brown cow", new File(hostedDirectory, "file2.txt"), Charsets.UTF_8);
    new File(hostedDirectory, "subdir").mkdirs();

    Cursor cursor =
        resolver.query(
            makeUri(""), HostedFile.HostedFileColumn.getColumnNames(), "", EMPTY_ARRAY, "");
    List<String> listedFileNames = Lists.newArrayList();
    try {
      assertEquals(2, cursor.getCount());
      int nameIndex = HostedFile.HostedFileColumn.NAME.getPosition();
      int typeIndex = HostedFile.HostedFileColumn.TYPE.getPosition();

      while (cursor.moveToNext()) {
        listedFileNames.add(cursor.getString(nameIndex));
        HostedFile.FileType expectedFileType = HostedFile.FileType.FILE;
        if (cursor.getString(nameIndex).equals("subdir")) {
          expectedFileType = HostedFile.FileType.DIRECTORY;
        }
        HostedFile.FileType actualType =
            HostedFile.FileType.fromTypeCode(cursor.getString(typeIndex));
        assertEquals("Wrong file type: " + cursor, expectedFileType, actualType);
      }
    } finally {
      cursor.close();
    }
    assertContents(listedFileNames, "file1.txt", "file2.txt", "subdir");
  }

  private <T> void assertContents(Collection<T> collection, T... expectedItems) {
    for (T item : expectedItems) {
      assertTrue("missing: " + item + " from " + collection, collection.contains(item));
    }
    assertEquals(
        "size mismatch: " + collection, expectedItems.length, collection.size());
  }

  @Suppress
  public void testQueryFile() throws Exception {
    write("file1 contents", new File(hostedDirectory, "file1.txt"), Charsets.UTF_8);
    Cursor cursor =
        resolver.query(
            makeUri("file1.txt"),
            HostedFile.HostedFileColumn.getColumnNames(),
            "",
            EMPTY_ARRAY,
            "");
    try {
      assertEquals(1, cursor.getCount());
      int nameIndex = HostedFile.HostedFileColumn.NAME.getPosition();
      int typeIndex = HostedFile.HostedFileColumn.TYPE.getPosition();
      int sizeIndex = HostedFile.HostedFileColumn.SIZE.getPosition();
      assertEquals("file1.txt", cursor.getString(nameIndex));
      assertEquals(
          HostedFile.FileType.FILE, HostedFile.FileType.fromTypeCode(cursor.getString(typeIndex)));
      assertEquals("file1 contents".length(), cursor.getString(sizeIndex));
    } finally {
      cursor.close();
    }
  }

  private String read(File file, Charset inputCharset) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file), inputCharset));
    return read(reader);
  }

  private String read(ParcelFileDescriptor fileDescriptor, Charset inputCharset)
      throws IOException {
    BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor), inputCharset));
    return read(reader);
  }

  private String read(BufferedReader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    String lineIn = null;
    while (null != (lineIn = reader.readLine())) {
      builder.append(lineIn);
    }
    return builder.toString();
  }

  private void write(String content, OutputStream outStream, Charset outputCharset)
      throws IOException {
    try {
      // FYI: getBytes(Charset) from api 9 and up.
      // getBytes(String charset) from api 1
      outStream.write(content.getBytes(outputCharset.name()));
    } finally {
      outStream.close();
    }
  }

  private void write(String content, File output, Charset outputCharset) throws IOException {
    write(content, new FileOutputStream(output), outputCharset);
  }

  private void initResolver() throws Exception {
    synchronized (TEST_RESOLVER_INIT_LOCK) {
      try {
        resolverHostedDirectory = hostedDirectory;
        resolverAccess = access;
        // Holy type safe language batman!
        resolver =
            ProviderTestCase2
                .<TestFileContentProviderTest.TestFileContentProvider>
                    newResolverWithContentProviderFromSql(
                        getContext(),
                        "foo",
                        TestFileContentProvider.class,
                        TEST_AUTHORITY,
                        "bogus",
                        0,
                        "");
      } finally {
        resolverHostedDirectory = BOGUS_DIRECTORY;
        resolverAccess = Access.READ_ONLY;
      }
    }
  }

  public static class TestFileContentProvider extends AbstractFileContentProvider {
    private final Predicate<Void> onCreateHookResult;

    public TestFileContentProvider() {
      super(resolverHostedDirectory, resolverAccess);
      this.onCreateHookResult = resolverOnCreateHookResult;
    }

    @Override
    public boolean onCreateHook() {
      return onCreateHookResult.apply(null);
    }
  }
}
