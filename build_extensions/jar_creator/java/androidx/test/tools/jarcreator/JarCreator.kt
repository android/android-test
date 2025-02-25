/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.test.tools.jarcreator

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * This is a simple utility that creates a jar file of input files.
 *
 * Unlike other solutions like invoking jar command directly or using bazel's
 * java_common.pack_sources, this will create a jar without timestamp and with files in the root
 * directory of the jar.
 */
fun createJar(args: Array<String>) {
  require(args.size >= 2) { "Must provide at least two files: <output> <input...>" }

  val outputFile = args[0]
  val jarOutputStream = JarOutputStream(FileOutputStream(outputFile, false))
  jarOutputStream.use {
    for (i in 1 until args.size) {
      val inputFile = File(args[i])
      val inputStream = BufferedInputStream(FileInputStream(inputFile))
      inputStream.use { addToJar(jarOutputStream, inputStream, inputFile) }
    }
  }
}

private fun addToJar(jarOutputStream: JarOutputStream, inputStream: InputStream, inputFile: File) {
  val entry = JarEntry(inputFile.name)
  jarOutputStream.putNextEntry(entry)
  inputStream.transferTo(jarOutputStream)
}
