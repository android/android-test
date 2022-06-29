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
import androidx.test.tools.lint.getMethodSignature
import androidx.test.tools.lint.matchMethodSignature
import androidx.test.tools.lint.searchCalls
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * A lint detector that reports a warning if it detects the incorrect Java/Kotlin code pattern of
 * calling the [androidx.test.espresso.Espresso.onView] method in
 * [androidx.test.core.app.ActivityScenario.onActivity] code block.
 *
 * For example,
 * ```java
 *     scenario.onActivity( activity -> {
 *         onView(...).perform(...);
 *     });
 * ```
 *
 * The code pattern may cause runtime failures since the `ActivityScenario.onActivity` code block
 * will be running on the main thread but the `Espresso.onView` must be called on the test thread in
 * an instrumentation test.
 */
@Suppress("DetectorIsMissingAnnotations")
open class OnViewInOnActivityCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used.
  protected open val issue: Issue =
    Issue.create(
      id = "OnViewInOnActivity",
      briefDescription = "Warn about calling onView in ActivityScenario.onActivity",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(OnViewInOnActivityCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableUastTypes(): List<Class<out UElement>>? {
    return listOf(UFile::class.java)
  }

  override fun createUastHandler(context: JavaContext) =
    object : UElementHandler() {
      override fun visitFile(node: UFile) {
        val allowEnterMethodSignatures: MutableSet<LintMethodSignature> = mutableSetOf()
        // Records the methods inside the current file. The later searchCalls() only works on this
        // limited set of methods.
        val findMethodsVisitor =
          object : AbstractUastVisitor() {
            override fun visitMethod(node: UMethod): Boolean {
              allowEnterMethodSignatures.add(node.getMethodSignature())
              // Stops to search deeper if a method is found (ignores inline methods).
              return true
            }
          }
        node.accept(findMethodsVisitor)

        val findPatternVisitor =
          object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
              if (!node.matchMethodSignature(ON_ACTIVITY_METHOD_SIGNATURE)) return false

              node.getArgumentForParameter(0)?.let { firstParameter ->
                // Our check only works on the situation where the first parameter of onActivity()
                // is a lambda expression.
                if (firstParameter is ULambdaExpression) {
                  // Catches onView call.
                  // The searchCalls() has a maximum search depth. Call graph deeper than
                  // SEARCH_CALLS_MAX_DEPTH will be ignored.
                  val onViewNodes =
                    searchCalls(
                      firstParameter,
                      ON_VIEW_METHOD_SIGNATURE,
                      allowEnterMethodSignatures = allowEnterMethodSignatures,
                      maxDepth = SEARCH_CALLS_MAX_DEPTH
                    )
                  for (onViewNode: UCallExpression in onViewNodes) {
                    context.report(
                      issue,
                      onViewNode,
                      context.getNameLocation(onViewNode),
                      REPORT_MESSAGE
                    )
                  }
                }
              }
              // Stops to search deeper if matched onActivity().
              return true
            }
          }
        node.accept(findPatternVisitor)
      }
    }

  private companion object {
    const val SEARCH_CALLS_MAX_DEPTH = 3
    val REPORT_MESSAGE =
      """
        The code block passed into the `ActivityScenario.onActivity` method will be running on the \
        main thread, while you are only supposed to call the `onView` method on the test thread in \
        an instrumentation test.

        Please move the `onView` call out of `ActivityScenario.onActivity` to avoid failures and \
        make your tests compatible with the instrumentation test environment.
    """
        .trimIndent()
        .replace("\\\n", "")

    val ON_ACTIVITY_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.core.app.ActivityScenario",
        "onActivity",
        listOf("androidx.test.core.app.ActivityScenario.ActivityAction<A>")
      )
    val ON_VIEW_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.espresso.Espresso",
        "onView",
        listOf("org.hamcrest.Matcher<android.view.View>")
      )
  }
}
