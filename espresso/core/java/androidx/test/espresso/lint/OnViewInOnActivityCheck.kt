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
import org.jetbrains.uast.getParentOfType

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
  open val issue: Issue =
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

  override fun getApplicableMethodNames() = listOf("onView")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    // Catches onView call.
    if (method.getMethodSignature() != ON_VIEW_METHOD_SIGNATURE) return
    node.getParentOfType<UCallExpression>()?.resolve()?.let { onActivityNode ->
      // Catches onActivity call.
      if (onActivityNode.getMethodSignature() != ON_ACTIVITY_METHOD_SIGNATURE) return
      context.report(issue, node, context.getNameLocation(node), REPORT_MESSAGE)
    }
  }

  private companion object {

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
