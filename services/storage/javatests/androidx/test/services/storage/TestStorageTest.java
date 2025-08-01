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
package androidx.test.services.storage;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.net.Uri;
import android.os.UserManager;
import androidx.test.services.storage.file.HostedFile;
import androidx.test.services.storage.internal.TestStorageUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test cases for {@link TestStorage}. */
@RunWith(JUnit4.class)
public final class TestStorageTest {

  private static final String OUTPUT_PATH = "parent_dir/output_file";

  private final TestStorage testStorage = new TestStorage();

  @Test
  public void readInputFile() throws IOException {
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(
                testStorage.openInputFile(
                    "google3/third_party/android/androidx_test/services/storage/javatests/androidx/test/services/storage/testinput.txt")))) {
      assertThat(br.readLine().trim()).isEqualTo("Hi I'm an input file");
    }
  }

  @Test
  public void readNonExistentInputFile() {
    try {
      testStorage.openInputFile("not/here");
      fail("Should throw FileNotFoundException.");
    } catch (FileNotFoundException e) {
      // Exception excepted.
    }
  }

  @Test
  public void writeFile() throws Exception {
    OutputStream rawStream = testStorage.openOutputFile(OUTPUT_PATH);
    Writer writer = new BufferedWriter(new OutputStreamWriter(rawStream));
    try {
      writer.write("Four score and 7 years ago\n");
      writer.write("Our forefathers executed some tests.");
    } finally {
      writer.close();
    }
  }

  @Test
  public void addOutputProperties() throws Exception {
    Map<String, Serializable> propertyMap = new HashMap<String, Serializable>();
    propertyMap.put("property-a", "test");
    // Pass in a cloned copy since addStatsToSponge may modify the propertyMap instance.
    testStorage.addOutputProperties(new HashMap<String, Serializable>(propertyMap));
    propertyMap.put("property-b", "test");
    testStorage.addOutputProperties(new HashMap<String, Serializable>(propertyMap));
    // Test property value updated.
    propertyMap.put("property-b", "test-updated");
    testStorage.addOutputProperties(new HashMap<String, Serializable>(propertyMap));

    Uri dataUri = HostedFile.buildUri(HostedFile.FileHost.EXPORT_PROPERTIES, "properties.dat");
    InputStream rawStream =
        TestStorageUtil.getInputStream(dataUri, getApplicationContext().getContentResolver());

    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(rawStream);
      Map<String, Serializable> recordedStats = (Map<String, Serializable>) in.readObject();
      assertEquals("Properties not written to the properties file", propertyMap, recordedStats);
    } catch (IOException | ClassNotFoundException e) {
      closeInputStream(in);
    }
  }

  @Test
  public void writeInternalFile() throws IOException {
    // known not to work in multi-user mode
    assumeTrue(isSystemUser());
    try (OutputStream output = testStorage.openInternalOutputFile("path/to/file")) {
      output.write(new byte[] {'h', 'e', 'l', 'l', 'o'});
    }
  }

  private static boolean isSystemUser() {

      UserManager um =
          ((UserManager) getApplicationContext().getSystemService(Context.USER_SERVICE));
      return um.isSystemUser();
  }

  @Test
  public void readWriteOverwriteReadFile() throws IOException {
    try (OutputStream output = testStorage.openOutputFile("path/to/file")) {
      output.write(new byte[] {'h', 'e', 'l', 'l', 'o', '1', '2', '3'});
    }

    try (OutputStream output = testStorage.openOutputFile("path/to/file")) {
      output.write(new byte[] {'w', 'o', 'r', 'l', 'd'}); // Strictly shorter
    }

    StringBuilder contents = new StringBuilder();
    Uri outputFileUri = testStorage.getOutputFileUri("path/to/file");
    try (InputStream input =
        TestStorageUtil.getInputStream(
            outputFileUri, getApplicationContext().getContentResolver())) {
      int nextVal = input.read();
      while (nextVal != -1) {
        contents.append((char) nextVal);
        nextVal = input.read();
      }
    }

    assertThat(contents.toString()).isEqualTo("world");
  }

  @Test
  public void writeFileInAppendMode() throws IOException {
    try (OutputStream output = testStorage.openOutputFile("path/to/file")) {
      output.write(new byte[] {'h', 'e', 'l', 'l', 'o'});
    }

    try (OutputStream output = testStorage.openOutputFile("path/to/file", true)) {
      output.write(new byte[] {' ', 'w', 'o', 'r', 'l', 'd'});
    }

    byte[] data = new byte[11];
    Uri outputFileUri = testStorage.getOutputFileUri("path/to/file");
    try (InputStream input =
        TestStorageUtil.getInputStream(
            outputFileUri, getApplicationContext().getContentResolver())) {
      input.read(data);
    }

    assertThat(new String(data, Charset.defaultCharset())).isEqualTo("hello world");
  }

  private void closeInputStream(ObjectInputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException e) {
        // do nothing.
      }
    }
  }
}
