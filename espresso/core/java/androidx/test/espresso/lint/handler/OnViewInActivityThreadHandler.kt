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

package third_party.android.androidx_test.espresso.core.java.androidx.test.espresso.lint.handler

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.getMethodSignature
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.searchCall

class OnViewInActivityThreadHandler(private val context: JavaContext, private val issue: Issue) :
  UElementHandler() {
  override fun visitCallExpression(node: UCallExpression) {
    node.resolve()?.let { callee ->
      // Catch onActivity call.
      if (callee.getMethodSignature() ==
          "androidx.test.core.app.ActivityScenario::onActivity" +
            "(androidx.test.core.app.ActivityScenario.ActivityAction<A>)"
      ) {
        node.getArgumentForParameter(0)?.let { lambdaElement ->
          // We assume the parameter of onActivity is a lambda expression.
          if (lambdaElement is ULambdaExpression) {
            // Catch onView call.
            val callNodes =
              lambdaElement.searchCall(
                "androidx.test.espresso.Espresso::onView" +
                  "(org.hamcrest.Matcher<android.view.View>)",
                maxDepth = 10
              )
            for (callNode: UCallExpression in callNodes) {
              context.report(
                issue,
                callNode,
                context.getNameLocation(callNode),
                // TODO(b/234640851) Make warning messages comprehensive and actionable.
                """
                  Calling onView in ActivityScenario.onActivity may cause failures.
                  Please move onView() out of ActivityScenario.onActivity.
                """.trimIndent(),
              )
            }
          }
        }
      }
    }
  }
}
