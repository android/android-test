/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.espresso.lint

import com.android.tools.lint.detector.api.JavaContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.jetbrains.uast.UFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Unit tests of OnViewInOnActivityCheck. */
@RunWith(JUnit4::class)
class OnViewInOnActivityCheckTest {
  private val mockJavaContext = mock<JavaContext>()
  private val detector = OnViewInOnActivityCheck()
  private val mockFile = mock<UFile>()

  @Test
  fun visitFile_minimumCoverage() {
    // UFile.accept() is always called twice.
    detector.createUastHandler(mockJavaContext).visitFile(mockFile)
    verify(mockFile, times(2)).accept(any())
  }
}
