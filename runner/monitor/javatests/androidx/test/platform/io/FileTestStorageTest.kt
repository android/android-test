/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.test.platform.io

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileTestStorageTest {

  @Test
  fun readNonExistentInputFile() {
    val storage = FileTestStorage()
    assertThrows(FileNotFoundException::class.java) { storage.openInputFile("not/here") }
  }

  /**
   * Simple test for writing a file. Should be executed on every Android API to test for
   * incompatibilities.
   */
  @Test
  fun writeFile() {
    val storage = FileTestStorage()
    BufferedWriter(OutputStreamWriter(storage.openOutputFile("testcontent.txt"))).use {
      it.write("test content\n")
    }
  }

  @Test
  fun inputArg() {
    val storage = FileTestStorage()
    assertThat(storage.getInputArg("thisisanarg")).isEqualTo("hi")
  }

  @Test
  fun inputFile() {
    val storage = FileTestStorage()

    BufferedReader(InputStreamReader(storage.openInputFile("testinput.txt"))).use {
      assertThat(it.readText().trim()).isEqualTo("Hi I'm an input file")
    }
  }
}
