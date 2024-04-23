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

import android.os.Build
import android.os.Environment
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** @hide */
// this should be just an internal class, but lint complains about accessing a kotlin internal class
// from java
@RestrictTo(Scope.LIBRARY)
class TestDirCalculator {
  val outputDir: File by lazy { calculateOutputDir() }
  val inputDir: File by lazy { calculateInputDir() }

  private fun calculateOutputDir(): File {
    val additionalOutputTestDir =
      InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
    if (additionalOutputTestDir != null) {
      return File(additionalOutputTestDir)
    }

    val rootdir = File(calculateDefaultRootDir(), "additionalTestOutputDir")

    return rootdir
  }

  private fun calculateInputDir(): File {
    val testInputDir = InstrumentationRegistry.getArguments().getString("testInputDir")
    if (testInputDir != null) {
      return File(testInputDir)
    }

    return File(calculateDefaultRootDir(), "testInputDir")
  }

  private fun calculateDefaultRootDir(): File {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (Build.VERSION.SDK_INT >= 29) {
      // On Android Q+ first attempt to use the media directory because that is
      // writable without any extra storage permissions
      // https://developer.android.com/about/versions/11/privacy/storage
      @Suppress("DEPRECATION")
      for (mediaDir in context.externalMediaDirs) {
        if (Environment.getExternalStorageState(mediaDir) == Environment.MEDIA_MOUNTED)
          return mediaDir
      }
    }
    // on older platforms or if media dir wasn't mounted try using the app's external cache dir
    if (context.externalCacheDir != null) {
      return context.externalCacheDir!!
    }
    // finally, fallback to cacheDir
    return context.cacheDir
  }
}
