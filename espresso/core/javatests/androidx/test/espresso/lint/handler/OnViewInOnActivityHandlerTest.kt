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

package androidx.test.espresso.lint.handler

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Platform
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.UastCallVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.util.MethodSignature
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Unit tests of OnViewInOnActivityHandler. */
@RunWith(JUnit4::class)
class OnViewInOnActivityHandlerTest {
  private val mockJavaContext = mock<JavaContext>()
  private val sampleIssue =
    Issue.create(
      "id",
      "briefDescription",
      "explanation",
      Implementation(null, enumSetOf(Scope.ALL_CLASS_FILES)),
      "moreInfo",
      Category.CORRECTNESS,
      0,
      Severity.WARNING,
      enabledByDefault = false,
      androidSpecific = false,
      platforms = enumSetOf(Platform.ANDROID),
      suppressAnnotations = listOf<String>()
    )
  private val handler = OnViewInOnActivityHandler(mockJavaContext, sampleIssue)
  private val mockOnActivityCall = mock<UCallExpression>()
  private val mockOnActivityMethod = mock<PsiMethod>()
  private val mockLambdaExpression = mock<ULambdaExpression>()

  @Before
  fun setUp() {
    // Constructs a valid input for visitCallExpression.
    // Should reach UElement.searchCall().
    val psiType =
      mock<PsiType> {
        on { getCanonicalText(false) } doReturn
          "androidx.test.core.app.ActivityScenario.ActivityAction<A>"
      }
    val psiClass =
      mock<PsiClass> { on { qualifiedName } doReturn "androidx.test.core.app.ActivityScenario" }
    val signatureObject =
      mock<MethodSignature> {
        on { parameterTypes } doReturn arrayOf(psiType)
        on { name } doReturn "onActivity"
      }
    whenever(mockOnActivityMethod.containingClass).doReturn(psiClass)
    whenever(mockOnActivityMethod.getSignature(PsiSubstitutor.EMPTY)).doReturn(signatureObject)
    whenever(mockOnActivityCall.resolve()).doReturn(mockOnActivityMethod)
    whenever(mockOnActivityCall.getArgumentForParameter(0)).doReturn(mockLambdaExpression)
  }

  @Test
  fun visitCallExpression_validMethodCall() {
    // Should reach UElement.searchCall().
    handler.visitCallExpression(mockOnActivityCall)
    // UElement.searchCall() is a final method that cannot be verified. Checks the reachability of
    // UElement.accept() inside UElement.searchCall().
    verify(mockLambdaExpression).accept(any<UastCallVisitor>())
  }

  @Test
  fun visitCallExpression_invalidParameter() {
    // When the first parameter of the onActivity call is not a valid Lambda expression, it should
    // not reach UElement.searchCall().
    val mockInvalidArgument = mock<UExpression>()
    whenever(mockOnActivityCall.getArgumentForParameter(0)).doReturn(mockInvalidArgument)
    handler.visitCallExpression(mockOnActivityCall)

    verify(mockOnActivityCall).getArgumentForParameter(0)
    verify(mockInvalidArgument, never()).accept(any<UastCallVisitor>())
  }

  @Test
  fun visitCallExpression_invalidMethodSignature() {
    // When the method signature matching is failed, it should not reach
    // UCallExpression.getArgumentForParameter(0).
    whenever(mockOnActivityMethod.containingClass).doReturn(null)
    handler.visitCallExpression(mockOnActivityCall)

    verify(mockOnActivityMethod).containingClass
    verify(mockOnActivityCall, never()).getArgumentForParameter(0)
  }

  @Test
  fun visitCallExpression_invalidMethod() {
    // When UCallExpression.resolve() is failed, it should not reach PsiMethod.getMethodSignature().
    whenever(mockOnActivityCall.resolve()).doReturn(null)
    handler.visitCallExpression(mockOnActivityCall)

    verify(mockOnActivityCall).resolve()
    // PsiMethod.getMethodSignature() is a final method that cannot be verified. Checks the
    // reachability of PsiMethod.containingClass inside PsiMethod.getMethodSignature().
    verify(mockOnActivityMethod, never()).containingClass
  }
}
