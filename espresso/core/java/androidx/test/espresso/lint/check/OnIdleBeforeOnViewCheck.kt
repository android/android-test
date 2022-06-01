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

package third_party.android.androidx_test.espresso.core.java.androidx.test.espresso.lint.check

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import third_party.android.androidx_test.espresso.core.java.androidx.test.espresso.lint.handler.OnIdleBeforeOnViewHandler

// TODO(b/234640486) Implementation and deployment of checks for external uses.
@Suppress("UnstableApiUsage", "DetectorIsMissingAnnotations")
class OnIdleBeforeOnViewCheck : Detector(), Detector.UastScanner {
  override fun getApplicableUastTypes(): List<Class<out UElement?>> {
    return listOf(UCallExpression::class.java)
  }

  // JavaContext is used regardless of language
  override fun createUastHandler(context: JavaContext): UElementHandler =
    OnIdleBeforeOnViewHandler(context, ISSUE)

  companion object {
    /** Issue describing the problem and pointing to the detector implementation. */
    @JvmField
    val ISSUE: Issue =
      Issue.create(
        // ID: used in @SuppressLint warnings etc
        id = "OnIdleBeforeOnView",
        // Title -- shown in the IDE's preference dialog, as category headers in the
        // Analysis results window, etc
        briefDescription = "Calling onIdle() before onView() is unnecessary",
        // Full explanation of the issue; you can use some markdown markup such as
        // `monospace`, *italic*, and **bold**.
        explanation =
          """
                    N/A
                    """, // no need to .trimIndent(), lint
        // does that automatically
        category = Category.CORRECTNESS,
        priority = 6,
        severity = Severity.WARNING,
        implementation =
          Implementation(OnIdleBeforeOnViewCheck::class.java, Scope.JAVA_FILE_SCOPE)
      )
  }
}
