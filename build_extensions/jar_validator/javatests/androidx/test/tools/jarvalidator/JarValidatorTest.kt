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

package androidx.test.tools.jarvalidator

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Arrays
import kotlin.io.path.Path
import kotlin.io.path.exists
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JarValidatorTest {

  @Test
  fun invalidInput() {
    assertThrows(java.lang.IllegalArgumentException::class.java) { validateJar(emptyArray()) }
  }

  @Test
  fun matchingClasses() {
    val matchingJarPath = getDataJarPath("libmatching.jar")
    assertThat(matchingJarPath.exists()).isTrue()

    val outFile = File.createTempFile("matchingClasses", ".txt")

    assertThat(validateJar(arrayOf(outFile.absolutePath, matchingJarPath.toAbsolutePath().toString(), "androidx.test.tools.jarvalidator.fixtures.matching" ))).isTrue()
  }

  @Test
  fun notMatchingClasses() {
    val matchingJarPath = getDataJarPath("libnotmatching.jar")
    assertThat(matchingJarPath.exists()).isTrue()

    val outFile = File.createTempFile("notMatchingClasses", ".txt")

    assertThat(validateJar(arrayOf(outFile.absolutePath, matchingJarPath.toAbsolutePath().toString(), "androidx.test.tools.jarvalidator.fixtures.matching" ))).isFalse()
    val outFileContents = outFile.readText()
    // assert not matching class is listed
    assertThat(outFileContents).contains("androidx.test.tools.jarvalidator.fixtures.notmatching.NotMatching")
    assertThat(outFileContents).doesNotContain("androidx.test.tools.jarvalidator.fixtures.matching.Matching")
  }

  private fun getDataJarPath(name: String) =
    Paths.get(System.getenv("TEST_SRCDIR"), "build_extensions", "jar_validator", "javatests", "androidx", "test", "tools", "jarvalidator", "fixtures" , name)
}