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

package androidx.test.tools.jarcombiner

import androidx.test.tools.jarvalidator.getClassesInJar
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipException
import kotlin.io.path.exists
import kotlin.streams.toList
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JarCombinerTest {

  @Test
  fun invalidInput() {
    assertThrows(java.lang.IllegalArgumentException::class.java) { combineJars(emptyArray()) }
  }

  @Test
  fun combineJars() {
    val outFile = File.createTempFile("combineJarsOut", ".jar")

    combineJars(
      arrayOf(outFile.absolutePath, getDataJarPath("libjar1.jar"), getDataJarPath("libjar2.jar"))
    )
    val classes = getClassesInJar(outFile.absolutePath)
    assertThat(classes.toList())
      .containsExactly(
        "androidx.test.tools.jarcombiner.fixtures.Jar1Class",
        "androidx.test.tools.jarcombiner.fixtures.Jar2Class"
      )
  }

  @Test
  fun rClassesRemoved() {
    val outFile = File.createTempFile("rClassesRemovedOut", ".jar")

    combineJars(arrayOf(outFile.absolutePath, getDataJarPath("libjar_with_r.jar")))
    val classes = getClassesInJar(outFile.absolutePath)
    assertThat(classes.toList())
      .containsExactly("androidx.test.tools.jarcombiner.fixtures.Jar1Class")
  }

  @Test
  fun duplicateClasses() {
    val outFile = File.createTempFile("duplicateClasses", ".jar")

    assertThrows(
      ZipException::class.java,
      {
        combineJars(
          arrayOf(
            outFile.absolutePath,
            getDataJarPath("libjar1.jar"),
            getDataJarPath("libjar_with_r.jar")
          )
        )
      }
    )
  }

  private fun getDataJarPath(name: String): String {
    val path =
      Paths.get(
        System.getenv("TEST_SRCDIR"),
        "build_extensions",
        "jar_combiner",
        "javatests",
        "androidx",
        "test",
        "tools",
        "jarcombiner",
        "fixtures",
        name
      )
    assertThat(path.exists()).isTrue()
    return path.toAbsolutePath().toString()
  }
}
