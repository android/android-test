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
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.getMethodSignature
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.getNextSibling
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.getStatement
import third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint.searchCall

class OnIdleBeforeOnViewHandler(private val context: JavaContext, private val issue: Issue) :
  UElementHandler() {
  override fun visitCallExpression(node: UCallExpression) {
    node.resolve()?.let { callee ->
      // Catch onIdle call.
      if (callee.getMethodSignature() == "androidx.test.espresso.Espresso::onIdle()") {
        node.getStatement()?.getNextSibling()?.let { next ->
          // Catch onView call.
          val onViewCalls =
            next.searchCall(
              "androidx.test.espresso.Espresso::onView(org.hamcrest.Matcher<android.view.View>)",
              maxDepth = 0
            )
          if (onViewCalls.isNotEmpty()) {
            // TODO(b/234640851) Make warning messages comprehensive and actionable.
            context.report(
              issue,
              node,
              context.getNameLocation(node),
              """
                Calling onIdle before onView is unnecessary.
                Please remove onIdle().
              """.trimIndent(),
            )
          }
        }
      }
    }
  }
}
