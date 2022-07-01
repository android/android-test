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

package androidx.test.lint

import com.android.tools.lint.detector.api.JavaContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Unit tests of SdkSuppressInvalidVersionCheck */
@RunWith(JUnit4::class)
class SdkSuppressInvalidVersionCheckTest {
  private val mockJavaContext = mock<JavaContext>()
  private val handler = SdkSuppressInvalidVersionCheck().createUastHandler(mockJavaContext)
  private val mockAnnotation = mock<UAnnotation>()

  @Before
  fun setUp() {
    // Prepares a valid input for visitAnnotation.
    val mockMethod = mock<UMethod>()
    whenever(mockAnnotation.qualifiedName).doReturn("androidx.test.filters.SdkSuppress")
    whenever(mockAnnotation.uastParent).doReturn(mockMethod)
  }

  @Test
  fun visitAnnotation_validAnnotation() {
    // Should reach UAnnotation.findAttributeValue() twice for two attributes respectively.
    handler.visitAnnotation(mockAnnotation)
    verify(mockAnnotation, times(2)).findAttributeValue(any())
  }

  @Test
  fun visitAnnotation_notInMethod() {
    // The annotation is not on a method. Should not reach UAnnotation.findAttributeValue().
    whenever(mockAnnotation.uastParent).doReturn(null)
    handler.visitAnnotation(mockAnnotation)
    verify(mockAnnotation, never()).findAttributeValue(any())
  }

  @Test
  fun visitAnnotation_wrongClass() {
    // The annotation is not SdkSuppress. Should not reach UAnnotation.findAttributeValue().
    whenever(mockAnnotation.qualifiedName).doReturn("wrongName")
    handler.visitAnnotation(mockAnnotation)
    verify(mockAnnotation, never()).findAttributeValue(any())
  }
}
