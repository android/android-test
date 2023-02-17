/*
 * Copyright 2023 The Android Open Source Project
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
package androidx.test.tools.jarvalidator

import java.io.File
import java.lang.System
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.stream.Stream

/** Validates the classes contained in a given jar file. */
fun validateJar(args: Array<String>): Boolean {
  require(args.size >= 3) {
    "Usage: <output txt> <input jar> <expected prefix 1> <expected prefix 2> ..."
  }

  val outputFile = File(args[0])
  val expectedPrefixes = args.asList().drop(2)
  val nonMatchingEntries = mutableListOf<String>()
  val jarFilePath = args[1]
  val classes = getClassesInJar(jarFilePath)
  for (className in classes) {
    if (!matchesExpectedPrefixes(expectedPrefixes, className)) {
      nonMatchingEntries.add(className)
    }
  }
  return if (nonMatchingEntries.size > 0) {
    nonMatchingEntries.sort()
    val error =
      "Error: The following classes in $jarFilePath did not match one of the expected prefixes $expectedPrefixes \n" +
        nonMatchingEntries.joinToString("\n")
    System.err.println(error)
    outputFile.writeText(error)
    false
  } else {
    outputFile.writeText("Success!")
    true
  }
}

fun getClassesInJar(filePath: String): Stream<String> {
  return JarFile(filePath)
    .stream()
    .filter { it.name.endsWith(".class") }
    .map { classNameFromPath(it) }
}

private fun matchesExpectedPrefixes(expectedPrefixes: List<String>, className: String): Boolean {
  for (pkg in expectedPrefixes) {
    if (className.startsWith(pkg)) {
      return true
    }
  }
  return false
}

private fun classNameFromPath(it: JarEntry) = it.name.replace('/', '.').replace(".class", "")
