/*
 * Copyright (C) 2021 The Android Open Source Project
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

@file:JvmName("BitmapStorage")

package androidx.test.core.graphics

import android.graphics.Bitmap
import androidx.annotation.RestrictTo
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.platform.io.PlatformTestStorage
import androidx.test.platform.io.PlatformTestStorageRegistry
import java.io.IOException

/**
 * Writes the contents of the [Bitmap] to a compressed png file on [PlatformTestStorage]
 *
 * @param name a descriptive base name for the resulting file. '.png' will be appended to this name.
 * @throws IOException if bitmap could not be compressed or written to ds
 */
@ExperimentalTestApi
@Throws(IOException::class)
fun Bitmap.writeToTestStorage(name: String) {
  writeToTestStorage(PlatformTestStorageRegistry.getInstance(), name)
}

/**
 * @deprecated
 * @hide
 */
@Deprecated(
  "use  PlatformTestStorageRegistry.setInstance in the rare cases where you want to override the PlatformTestStorage to use",
  replaceWith = ReplaceWith("writeToTestStorage()"),
)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // legacy - used by espresso 3.5.0 DefaultFailureHandler
@Throws(IOException::class)
fun Bitmap.writeToTestStorage(testStorage: PlatformTestStorage, name: String) {
  testStorage.openOutputFile("$name.png").use {
    if (
      !this.compress(
        Bitmap.CompressFormat.PNG,
        /** PNG is lossless, so quality is ignored. */
        0,
        it,
      )
    ) {
      throw IOException("Failed to compress bitmap")
    }
  }
}
