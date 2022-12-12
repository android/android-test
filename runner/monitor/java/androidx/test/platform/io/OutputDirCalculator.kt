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
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** @hide */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class OutputDirCalculator {
  val outputDir: File by lazy { calculateOutputDir() }

  private fun calculateOutputDir(): File {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val additionalOutputTestDir =
      InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
    if (additionalOutputTestDir != null) {
      return File(additionalOutputTestDir)
    }
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
