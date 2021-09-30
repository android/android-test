/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.core.graphics

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.io.PlatformTestStorage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapStorageTest {

  @Test
  fun writeToTestStorage() {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    bitmap.writeToTestStorage("test")
  }

  @Test
  fun writeToTestStorage_throws() {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    assertThrows(IOException::class.java) {
      bitmap.writeToTestStorage(ThrowingPlatformTestStorage(), "test")
    }
  }

  class ThrowingPlatformTestStorage : PlatformTestStorage {
    override fun openInputFile(pathname: String?): InputStream {
      TODO("Not yet implemented")
    }

    override fun getInputArg(argName: String?): String {
      TODO("Not yet implemented")
    }

    override fun getInputArgs(): MutableMap<String, String> {
      TODO("Not yet implemented")
    }

    override fun openOutputFile(pathname: String?): OutputStream {
      throw IOException("error")
    }

    override fun openOutputFile(pathname: String?, append: Boolean): OutputStream {
      TODO("Not yet implemented")
    }

    override fun addOutputProperties(properties: MutableMap<String, Serializable>?) {
      TODO("Not yet implemented")
    }

    override fun getOutputProperties(): MutableMap<String, Serializable> {
      TODO("Not yet implemented")
    }

    override fun openInternalInputFile(pathname: String?): InputStream {
      TODO("Not yet implemented")
    }

    override fun openInternalOutputFile(pathname: String?): OutputStream {
      TODO("Not yet implemented")
    }
  }
}
