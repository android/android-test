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
 * [androidx.test.core.app.ActivityScenario] is created but will not be automatically closed by
 * [androidx.test.core.app.ActivityScenario.close].
 *
 * For example,
 * ```java
 *     ActivityScenario<MyActivity> scenario = ActivityScenario.launch(MyActivity.class));
 *     scenario.close().
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
open class ActivityScenarioNoAutoCloseCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check for external use is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "ActivityScenarioNoAutoClose",
      briefDescription = "Warn about ActivityScenario without automated close",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation =
        Implementation(ActivityScenarioNoAutoCloseCheck::class.java, Scope.JAVA_FILE_SCOPE)
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
          // Ignores the check on ActivityScenarioRule classes.
          if (inActivityScenarioRule(launchCall)) continue
          // The instance is closed by Kotlin's use.
          if (isKotlin(node.sourcePsi) && closedWithRunBlock(launchCall)) continue

          val launchedScenario = getActivityScenarioByLaunchCall(launchCall)
          // The instance is closed by Java's try-with-resources.
          if (
            launchedScenario != null &&
              isJava(node.sourcePsi) &&
              closedWithTryWithResources(launchedScenario)
          )
            continue

          val closeCall =
            closedManually(
              launchedScenario,
              launchCall,
              closedScenarioReferences,
              resolvedCloseCalls
            )
          if (closeCall != null) {
            // The instance is manually closed.
            context.report(issue, closeCall, context.getLocation(closeCall), REPORT_MESSAGE)
          }
        }
      }
    }

  private companion object {
    val REPORT_MESSAGE =
      """
      We recommend to create an ActivityScenario instance using mechanisms which automatically \
      close the instance. Please consider to use ActivityScenarioRule, Java's try-with-resources, \
      or Kotlin's use function.

      Note: if you need to set up Dagger modules in your test, you may disregard this warning. \
      See http://go/hilt/instrumentation-testing#modules-and-activityscenariorule for details.
    """
        .trimIndent()
        .replace("\\\n", "")
  }
}
