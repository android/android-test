/*
 * Copyright (C) 2023 The Android Open Source Project
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

package androidx.test.tools.jarcreator

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.jar.JarFile
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JarCreatorTest {

  @Test
  fun invalidInput() {
    assertThrows(java.lang.IllegalArgumentException::class.java) { createJar(emptyArray()) }
  }

  @Test
  fun combineJars() {
    val outFile = File.createTempFile("createJarOut", ".jar")
    val fileToInclude = File.createTempFile("include", ".txt")

    createJar(arrayOf(outFile.absolutePath, fileToInclude.absolutePath))

    val contents = JarFile(outFile.absolutePath).stream().map { it.name }
    assertThat(contents).containsExactly(fileToInclude.name)
  }
}
