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
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UDeclarationsExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UMethod
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
 * Calling `onIdle` before `onView` is unnecessary because `onView` does the same synchronization
 * as `onIdle` does internally. In this case, the call to `onIdle` should be removed.
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
        val inMethodVisitor =
          object : AbstractUastVisitor() {
            // The UAST node recording the location of found onIdle().
            private var foundOnIdle: UCallExpression? = null

            override fun visitCallExpression(node: UCallExpression): Boolean {
              if (node.matchMethodSignature(ON_IDLE_METHOD_SIGNATURE)) {
                // Found the onIdle() call. Records the node.
                foundOnIdle = node
              } else if (
                foundOnIdle != null && node.matchMethodSignature(ON_VIEW_METHOD_SIGNATURE)
              ) {
                // Found the onView() call and the last call is onIdle(). Reports the warnings and
                // resets state.
                context.report(
                  issue,
                  foundOnIdle,
                  context.getNameLocation(foundOnIdle!!),
                  REPORT_MESSAGE
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
        node.accept(inMethodVisitor)
      }
    }

  private companion object {
    val REPORT_MESSAGE =
      """
      Calling `onIdle` before `onView` is unnecessary because `onView` does the same synchronization
      as `onIdle` does internally.

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
  }
}
