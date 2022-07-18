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

import androidx.test.tools.lint.LintMethodSignature
import androidx.test.tools.lint.getMethodSignature
import androidx.test.tools.lint.searchUses
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression

/**
 * A lint detector that reports a warning if it detects an
 * [androidx.test.core.app.ActivityScenario.onActivity] call is unnecessary because the callback
 * parameter in type of [android.app.Activity] is never used.
 *
 * We recommend to move the body in `ActivityScenario.onActivity` out and delete the
 * `ActivityScenario.onActivity` call.
 */
@Suppress("DetectorIsMissingAnnotations")
open class UnusedOnActivityCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "UnusedOnActivity",
      briefDescription = "Warn about unnecessary ActivityScenario.onActivity",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(UnusedOnActivityCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableMethodNames() = listOf("onActivity")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    // Matches the signature of onActivity().
    if (method.getMethodSignature() != ON_ACTIVITY_METHOD_SIGNATURE) return

    // We assume the parameter of onActivity is a lambda expression with one parameter.
    val lambdaNode = node.getArgumentForParameter(0) ?: return
    if (lambdaNode !is ULambdaExpression) return
    if (lambdaNode.parameters.size != 1) return
    val lambdaParameter = lambdaNode.parameters[0]
    val lambdaBody = lambdaNode.body

    // Searches the uses of the lambda parameter in the lambda body. If the parameter is used, there
    // will be no warning.
    if (searchUses(lambdaBody, lambdaParameter).isNotEmpty()) return

    // Reports the warning and provides fix suggestions.
    // Removes the block expression of the lambda body.
    var lambdaBodyString = lambdaBody.asSourceString().trim().removeSurrounding("{", "}").trim()
    // Removes the invalid return after the block is removed.
    lambdaBodyString = lambdaBodyString.removePrefix("return").trim()
    // For java code, the fix may introduce an extra semicolon.
    lambdaBodyString = lambdaBodyString.removeSuffix(";").trim()

    context.report(
      issue,
      node,
      context.getLocation(node),
      REPORT_MESSAGE,
      fix().replace().with(lambdaBodyString).reformat(true).build()
    )
  }

  private companion object {
    val REPORT_MESSAGE =
      """
      The `ActivityScenario.onActivity` is unnecessary if you don't make use of the activity \
      instance within the code block. For example, you don't have to call `Espresso.onView` within \
      the `onActivity` block. Please consider removing `ActivityScenario.onActivity` call.
    """
        .trimIndent()
        .replace("\\\n", "")

    val ON_ACTIVITY_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.core.app.ActivityScenario",
        "onActivity",
        listOf("androidx.test.core.app.ActivityScenario.ActivityAction<A>")
      )
  }
}
