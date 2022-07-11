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

import androidx.test.tools.lint.LintMethodSignature
import androidx.test.tools.lint.matchMethodSignature
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UDeclarationsExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * A lint detector that reports a warning if it detects a call to
 * [androidx.test.espresso.Espresso.onIdle] right before a call to
 * [androidx.test.espresso.Espresso.onView].
 *
 * For example,
 * ```java
 *     onIdle();
 *     onView(...).perform(...);
 * ```
 *
 * Calling `onIdle` before `onView` is unnecessary because `onView` does the same synchronization as
 * `onIdle` does internally. In this case, the call to `onIdle` should be removed.
 */
@Suppress("DetectorIsMissingAnnotations")
open class OnIdleBeforeOnViewCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used.
  protected open val issue: Issue =
    Issue.create(
      id = "OnIdleBeforeOnView",
      briefDescription = "Calling onIdle() before onView() is unnecessary",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(OnIdleBeforeOnViewCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableUastTypes() = listOf(UMethod::class.java)

  override fun createUastHandler(context: JavaContext) =
    object : UElementHandler() {
      override fun visitMethod(node: UMethod) {
        val patternVisitor =
          object : AbstractUastVisitor() {
            // The UAST node recording the location of found onIdle().
            private var foundOnIdle: UElement? = null

            override fun visitCallExpression(node: UCallExpression): Boolean {
              if (node.matchMethodSignature(ON_IDLE_METHOD_SIGNATURE)) {
                // Found the statement which calls onIdle(). Records the node.
                foundOnIdle = node
                while (foundOnIdle!!.uastParent is UQualifiedReferenceExpression) {
                  foundOnIdle = foundOnIdle!!.uastParent as UElement
                }
              } else if (
                foundOnIdle != null && node.matchMethodSignature(ON_VIEW_METHOD_SIGNATURE)
              ) {
                // Found the onView() call and the last call is onIdle(). Reports the warnings and
                // resets state. Deletes the statement of onIdle().
                val deleteFix = fix().replace().with("")
                // Includes the semicolon to delete.
                val sourcePsi = foundOnIdle!!.sourcePsi
                if (sourcePsi != null) {
                  var curr = sourcePsi.nextSibling
                  while (curr != null) {
                    if (curr is PsiJavaToken && curr.tokenType == JavaTokenType.SEMICOLON) {
                      deleteFix.range(context.getRangeLocation(sourcePsi, 0, curr, 0))
                      break
                    } else if (
                      curr !is PsiWhiteSpace
                    ) { // allow whitespace between expression and semicolon
                      break
                    }
                    curr = curr.nextSibling
                  }
                }
                // Reports the warning along with the auto-fix option.
                context.report(
                  issue,
                  foundOnIdle,
                  context.getLocation(foundOnIdle),
                  REPORT_MESSAGE,
                  deleteFix.build()
                )
              } else {
                // In other cases, simply resets the state. We do not allow other calls between
                // onIdle() and onView().
                foundOnIdle = null
              }
              return false
            }

            override fun visitElement(node: UElement): Boolean {
              // To ensure onIdle() and onView() are adjacent in terms of control flow, check types
              // of UAST nodes between onIdle() amd onView() against an allow list.
              if (foundOnIdle == null) {
                return false
              }
              if (
                node !is UReferenceExpression &&
                  node !is UCallExpression &&
                  node !is UDeclarationsExpression &&
                  node !is UVariable &&
                  node !is UIdentifier &&
                  node !is UBinaryExpression
              ) {
                foundOnIdle = null
              }
              return false
            }
          }
        node.accept(patternVisitor)
      }
    }

  private companion object {
    val REPORT_MESSAGE =
      """
      Calling `onIdle` before `onView` is unnecessary because `onView` does the same \
      synchronization as `onIdle` does internally.

      Please remove the redundant onIdle().
    """
        .trimIndent()
        .replace("\\\n", "")

    val ON_IDLE_METHOD_SIGNATURE =
      LintMethodSignature("androidx.test.espresso.Espresso", "onIdle", listOf())

    val ON_VIEW_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.espresso.Espresso",
        "onView",
        listOf("org.hamcrest.Matcher<android.view.View>")
      )

    const val ON_IDLE_IMPORT_NAME = "androidx.test.espresso.Espresso.onIdle"
  }
}
