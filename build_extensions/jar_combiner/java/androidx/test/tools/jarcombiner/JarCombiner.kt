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
    // directories and certain classes.
    // Keep track of the list of directories already added to prevent this
    val alreadyAddedAllowedDuplicates: MutableSet<String> = HashSet()

    val entryToJar: MutableMap<String, String> = HashMap()

    for (i in 1 until args.size) {
      val inputFile = args[i]
      val jarInputStream = JarInputStream(FileInputStream(inputFile))
      jarInputStream.use {
        addToJar(
          alreadyAddedAllowedDuplicates,
          entryToJar,
          jarOutputStream,
          jarInputStream,
          args[i]
        )
      }
    }
  }
}

private fun addToJar(
  alreadyAddedAllowedDuplicates: MutableSet<String>,
  entryToJar: MutableMap<String, String>,
  jarOutputStream: JarOutputStream,
  inputJarStream: JarInputStream,
  inputJarName: String,
) {

  var entry = inputJarStream.nextEntry
  while (entry != null) {
    if (shouldAddEntry(entry, alreadyAddedAllowedDuplicates)) {
      checkIfAlreadyExists(entry.name, entryToJar, inputJarName)
      jarOutputStream.putNextEntry(entry)
      inputJarStream.transferTo(jarOutputStream)
      entryToJar.put(entry.name, inputJarName)
    }
    entry = inputJarStream.nextEntry
  }
}

fun checkIfAlreadyExists(
  name: String,
  entryToJar: MutableMap<String, String>,
  inputJarName: String
) {
  if (entryToJar.containsKey(name)) {
    throw RuntimeException(
      "Duplicate entry: $name is present in both ${entryToJar.get(name)} and $inputJarName"
    )
  }
}

private fun shouldAddEntry(
  entry: ZipEntry,
  alreadyAddedAllowedDuplicates: MutableSet<String>
): Boolean {
  if (entry.isDirectory) {
    if (alreadyAddedAllowedDuplicates.contains(entry.name)) {
      return false
    }
    alreadyAddedAllowedDuplicates.add(entry.name)
    return true
  } else if (entry.name.startsWith("com/google/protobuf")) {
    // bazel's new version of rules_proto creates java wrapper libraries around any proto_library
    // dependencies, which contain classes already present in the main protobuf-javalite-3.21.7.jar
    if (alreadyAddedAllowedDuplicates.contains(entry.name)) {
      return false
    }
    alreadyAddedAllowedDuplicates.add(entry.name)
    return true
  } else if (entry.name.matches(Regex(".*/R[\\.|\\$].*class$"))) {
    // strip generated R.class from resulting jar
    return false
  } else if (entry.name.equals("protobuf.meta")) {
    return false
  } else if (entry.name.startsWith("META-INF/maven")) {
    // strip out files added to META-INF/maven since this can lead to duplicate file errors
    return false
  } else if (entry.name.startsWith("google/protobuf")) {
    // strip out all the google/protobuf/*.proto files since this can lead to duplicate file errors
    return false
  } else if (entry.name.startsWith("META-INF/com.google.dagger_dagger.version")) {
    // strip out META-INF/com.google.dagger_dagger.version since this can lead to duplicate file
    // errors
    return false
  }
  return true
}
