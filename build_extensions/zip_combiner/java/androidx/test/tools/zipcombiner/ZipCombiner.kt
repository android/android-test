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
package androidx.test.tools.zipcombiner

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun combineZips(args: Array<String>) {
  require(args.size >= 2) { "Must provide a least two files: <output> <input...>" }

  val outputFile = args[0]
  val zipOutputStream = ZipOutputStream(FileOutputStream(outputFile, false))
  zipOutputStream.use {
    // keep track of what zip each entry belongs to, in order to have a better error
    // message in case of duplicates
    val entryToZip: MutableMap<String, String> = HashMap()

    for (i in 1 until args.size) {
      val inputFile = args[i]
      val zipInputStream = ZipInputStream(FileInputStream(inputFile))
      zipInputStream.use { addToZip(entryToZip, zipOutputStream, zipInputStream, args[i]) }
    }
  }
}

private fun addToZip(
  entryToZip: MutableMap<String, String>,
  zipOutputStream: ZipOutputStream,
  inputZipStream: ZipInputStream,
  inputZipName: String,
) {
  var entry = inputZipStream.nextEntry
  while (entry != null) {
    // ZipOutputStream will throw an obscure error if any duplicate entry is added, which is
    // undesirable for
    // directories and certain classes.
    // Add our own handling to allow certain duplicates, and throw a more descriptive error in
    // case there are unexpected duplicates
    if (entryToZip.containsKey(entry.name)) {
      if (!isAllowedDuplicate(entry)) {
        throw RuntimeException(
          "Duplicate entry: ${entry.name} is present in both ${entryToZip.get(entry.name)} and $inputZipName"
        )
      }
    } else {
      zipOutputStream.putNextEntry(entry)
      inputZipStream.transferTo(zipOutputStream)
      entryToZip.put(entry.name, inputZipName)
    }
    entry = inputZipStream.nextEntry
  }
}

private fun isAllowedDuplicate(entry: ZipEntry): Boolean {
  // always allow duplicate directories
  return entry.isDirectory
}
