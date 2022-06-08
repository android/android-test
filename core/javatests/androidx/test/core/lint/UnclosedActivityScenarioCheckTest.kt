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

package androidx.test.core.lint

import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.MethodSignature
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.jetbrains.uast.UCallExpression
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Unit tests of UnclosedActivityScenarioCheck. */
@RunWith(JUnit4::class)
class UnclosedActivityScenarioCheckTest {
  private val mockJavaContext = mock<JavaContext>()
  private val mockCallExpression = mock<UCallExpression>()
  private val mockPsiMethod = mock<PsiMethod>()
  private val detector = UnclosedActivityScenarioCheck()

  @Before
  fun SetUp() {
    val mockMethodSignature =
      mock<MethodSignature> {
        on { name } doReturn "launch"
        on { parameterTypes } doReturn arrayOf()
      }
    whenever(mockPsiMethod.getSignature(PsiSubstitutor.EMPTY)).doReturn(mockMethodSignature)
  }

  @Test
  fun visitMethodCall_minimumCoverage() {
    // Should at least reach UCallExpression.matchMethodSignature().
    detector.visitMethodCall(mockJavaContext, mockCallExpression, mockPsiMethod)
    verify(mockPsiMethod).getSignature(any())
  }
}
