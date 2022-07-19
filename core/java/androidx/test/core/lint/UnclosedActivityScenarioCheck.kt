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
import com.android.tools.lint.detector.api.isJava
import com.android.tools.lint.detector.api.isKotlin
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.tryResolve

/**
 * A lint detector that reports a warning if it detects an instance of
 * [androidx.test.core.app.ActivityScenario] is created but not closed by
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
 *
 * Lastly, if all above solutions do no apply, the `ActivityScenario` instance must be closed by
 * `Activity.close()` manually. For example,
 * ```java
 *     ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class));
 *     scenario.close().
 * ```
 */
@Suppress("DetectorIsMissingAnnotations")
open class UnclosedActivityScenarioCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "UnclosedActivityScenario",
      briefDescription = "Warn about unclosed ActivityScenario",
      explanation = UnclosedActivityScenarioCheck.REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation =
        Implementation(UnclosedActivityScenarioCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableUastTypes() = listOf(UClass::class.java)

  override fun createUastHandler(context: JavaContext) =
    object : UElementHandler() {
      override fun visitClass(node: UClass) {
        val launchCalls = searchCalls(node, ActivityScenarioConstants.LAUNCH_METHOD_SIGNATURE)
        val closeCalls = searchCalls(node, ActivityScenarioConstants.CLOSE_METHOD_SIGNATURE)

        val closedScenarioReferences: MutableList<PsiElement> = mutableListOf()
        val resolvedCloseCalls: MutableList<UCallExpression> = mutableListOf()
        for (closeCall in closeCalls) {
          getActivityScenarioByCloseCall(closeCall)?.tryResolve()?.let {
            closedScenarioReferences.add(it)
            resolvedCloseCalls.add(closeCall)
          }
        }

        for (launchCall in launchCalls) {
          val launchedScenario = getActivityScenarioByLaunchCall(launchCall) ?: continue
          if (
            !inActivityScenarioRule(launchCall) &&
              !(isJava(node.sourcePsi) && closedWithTryWithResources(launchedScenario)) &&
              !(isKotlin(node.sourcePsi) && closedWithRunBlock(launchCall)) &&
              closedManually(
                launchedScenario,
                launchCall,
                closedScenarioReferences,
                resolvedCloseCalls
              ) == null
          ) {
            context.report(issue, launchCall, context.getLocation(launchCall), REPORT_MESSAGE)
          }
        }
      }
    }

  private companion object {
    val REPORT_MESSAGE =
      """
      The ActivityScenario instance must be closed. Please close the instance using \
      ActivityScenarioRule, Java's try-with-resources, Kotlin's use function, or by manually \
      calling ActivityScenario.close().
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
