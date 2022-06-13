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

import androidx.test.tools.lint.LintMethodSignature
import androidx.test.tools.lint.getMethodSignature
import androidx.test.tools.lint.searchCalls
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression

/**
 * A lint handler that reports a warning if it detects the incorrect Java/Kotlin code pattern of
 * calling the `androidx.test.espresso.Espresso.onView` method in
 * `androidx.test.core.app.ActivityScenario.onActivity` code block.
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
class OnViewInOnActivityHandler(private val context: JavaContext, private val issue: Issue) :
  UElementHandler() {

  companion object {
    private const val SEARCH_CALLS_MAX_DEPTH = 10

    private val REPORT_MESSAGE =
      """
        The code block passed into the `ActivityScenario.onActivity` method will be running on the \
        main thread, while you are only supposed to call the `onView` method on the test thread in \
        an instrumentation test.
        
        Please move the `onView` call out of `ActivityScenario.onActivity` to avoid failures and \
        make your tests compatible with the instrumentation test environment.
    """.trimIndent().replace("\\\n", "")
  }

  override fun visitCallExpression(node: UCallExpression) {
    node.resolve()?.let { callee ->
      // Catches onActivity call.
      if (
        callee.getMethodSignature() ==
          LintMethodSignature(
            "androidx.test.core.app.ActivityScenario",
            "onActivity",
            listOf("androidx.test.core.app.ActivityScenario.ActivityAction<A>")
          )
      ) {
        node.getArgumentForParameter(0)?.let { firstParameter ->
          // Our check only works on the situation where the first parameter of onActivity() is a
          // lambda expression.
          if (firstParameter is ULambdaExpression) {
            // Catches onView call.
            // The searchCalls() has a maximum search depth. Call graph deeper than
            // SEARCH_CALLS_MAX_DEPTH will be ignored.
            val callNodes =
              searchCalls(
                firstParameter,
                LintMethodSignature(
                  "androidx.test.espresso.Espresso",
                  "onView",
                  listOf("org.hamcrest.Matcher<android.view.View>")
                ),
                maxDepth = SEARCH_CALLS_MAX_DEPTH
              )
            for (callNode: UCallExpression in callNodes) {
              context.report(issue, callNode, context.getNameLocation(callNode), REPORT_MESSAGE)
            }
          }
        }
      }
    }
  }
}
