/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.internal.runner.storage;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test cases for {@link RunnerTestStorageIO}. */
@RunWith(AndroidJUnit4.class)
public class RunnerTestStorageIOTest {

  @Test
  public void readWriteFile() throws IOException {
    RunnerTestStorageIO runnerStorage = new RunnerTestStorageIO();
    try (OutputStream output = runnerStorage.openOutputStream("path/to/file")) {
      output.write(new byte[] {'h', 'e', 'l', 'l', 'o'});
    }

    byte[] data = new byte[5];
    try (InputStream input = runnerStorage.openInputStream("path/to/file")) {
      input.read(data);
    }

    assertThat(new String(data, Charset.defaultCharset())).isEqualTo("hello");
  }
}
