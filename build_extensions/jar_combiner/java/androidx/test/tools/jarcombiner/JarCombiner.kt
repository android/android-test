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
package androidx.test.tools.jarcombiner

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

fun combineJars(args: Array<String>) {
  require(args.size >= 2) { "Must provide a least two files: <output> <input...>" }

  val outputFile = args[0]
  val jarOutputStream = JarOutputStream(FileOutputStream(outputFile, false))
  jarOutputStream.use {
    // JarOutputStream will throw an error if any duplicate entry is added, which is undesirable for
    // directories.
    // Keep track of the list of directories already added to prevent this
    val addedDirectories: MutableSet<String> = HashSet()

    for (i in 1 until args.size) {
      val inputFile = args[i]
      val jarInputStream = JarInputStream(FileInputStream(inputFile))
      jarInputStream.use { addToJar(addedDirectories, jarOutputStream, jarInputStream) }
    }
  }
}

private fun addToJar(
  addedDirectories: MutableSet<String>,
  jarOutputStream: JarOutputStream,
  inputJarStream: JarInputStream
) {

  var entry = inputJarStream.nextEntry
  while (entry != null) {
    if (shouldAddEntry(entry, addedDirectories)) {
      jarOutputStream.putNextEntry(entry)
      inputJarStream.transferTo(jarOutputStream)
    }
    entry = inputJarStream.nextEntry
  }
}

private fun shouldAddEntry(entry: ZipEntry, addedDirectories: MutableSet<String>): Boolean {
  if (entry.isDirectory) {
    if (addedDirectories.contains(entry.name)) {
      return false
    }
    addedDirectories.add(entry.name)
  } else if (entry.name.matches(Regex(".*/R[\\.|\\$].*class$"))) {
    // strip generated R.class from resulting jar
    return false
  }
  return true
}
