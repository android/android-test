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
import com.android.tools.lint.checks.DataFlowAnalyzer
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
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.getParentOfType

/**
 * A lint detector that reports a warning if it detects an instance of
 * [androidx.test.core.app.ActivityScenario] is created but will not be automatically closed by
 * [androidx.test.core.app.ActivityScenario.close].
 *
 * For example,
 * ```java
 *     ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class));
 *     // Without scenario.close().
 * ```
 *
 * To close the `ActivityScenario` safely, we recommend to use the automatic resource management
 * supported by Java/Kotlin. For example, we recommend to use try-with-resources introduced in Java
 * 7,
 * ```java
 *     try (ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class)) {...}
 * ```
 * or the `use` function in Kotlin's standard library,
 * ```kotlin
 *     ActivityScenario.launch(MyActivity::class.java).use { scenario -> ... }
 * ```
 *
 * Alternatively, we recommend to use [androidx.test.ext.junit.rules.ActivityScenarioRule] to manage
 * the scenario instance. For example,
 * ```java
 *     ActivityScenarioRule<MyActivity> rule = new ActivityScenarioRule<>(Activity.class);
 *     ActivityScenario<MyActivity> scenario = rule.getScenario();
 * ```
 */
@Suppress("DetectorIsMissingAnnotations")
open class UnclosedActivityScenarioCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used.
  protected open val issue: Issue =
    Issue.create(
      id = "UnclosedActivityScenario",
      briefDescription = "Warn about unclosed ActivityScenario",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation =
        Implementation(UnclosedActivityScenarioCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableMethodNames() = listOf("launch")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    if (method.getMethodSignature() != LAUNCH_METHOD_SIGNATURE) return
    if (checkTryWithResources(node)) return
    if (checkRunBlock(node)) return
    context.report(issue, node, context.getNameLocation(node), REPORT_MESSAGE)
  }

  private fun checkTryWithResources(node: UCallExpression): Boolean {
    val tryNode = node.getParentOfType<UTryExpression>() ?: return false
    if (!tryNode.hasResources) return false
    var parentNode = node.uastParent
    while (parentNode != null && parentNode !== tryNode) {
      if (parentNode in tryNode.resourceVariables) {
        return true
      } else {
        parentNode = parentNode.uastParent
      }
    }
    return false
  }

  private fun checkRunBlock(node: UCallExpression): Boolean {
    val method = node.getParentOfType<UMethod>() ?: return false
    var matched = false
    val searchUseCallVisitor =
      object : DataFlowAnalyzer(setOf(node as UElement)) {
        override fun receiver(call: UCallExpression) {
          // Matches only once.
          if (matched) return
          // Since the receiver type is ActivityScenario for sure, we simply match method names
          // instead of method signatures.
          if (call.methodName == "use") {
            matched = true
          }
        }
      }
    method.accept(searchUseCallVisitor)
    return matched
  }

  private companion object {
    val REPORT_MESSAGE =
      """
      The ActivityScenario instance will not be automatically closed.

      We recommend to implement the creation of ActivityScenario under automatic resource \
      management. Please consider to use Java's try-with-resources, Kotlin's use function, or \
      ActivityScenarioRule.
    """
        .trimIndent()
        .replace("\\\n", "")

    val LAUNCH_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.core.app.ActivityScenario",
        "launch",
        listOf("java.lang.Class<A>")
      )
  }
}
