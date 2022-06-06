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

package androidx.test.espresso.lint.detector

import androidx.test.espresso.lint.handler.OnViewInOnActivityHandler
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

// TODO(b/234640486) Deployment of checks for external uses. Currently, the detector is not used.
/**
 * A lint detector that reports a warning if it detects the incorrect Java/Kotlin code pattern of
 * calling the [Espresso#onView] method in [ActivityScenario#onActivity] code block.
 *
 * For example,
 * ```java
 *     scenario.onActivity( activity -> {
 *         onView(...).perform(...);
 *     });
 * ```
 *
 * The code pattern may cause runtime failures since [ActivityScenario#onActivity] will be running
 * on the main thread but `Espresso#onView` must be on the test thread. This handler reports
 * warnings when it detects the code pattern.
 *
 * The detector can be used by Android Studio or Android developers to customize their Android Lint
 * checks. For implementation of the detection algorithm, please refer to
 * `androidx.test.espresso.lint.handler.OnViewInOnActivityHandler`.
 */
@Suppress("UnstableApiUsage", "DetectorIsMissingAnnotations")
class OnViewInOnActivityCheck : Detector(), Detector.UastScanner {
  override fun getApplicableUastTypes(): List<Class<out UElement?>> {
    return listOf(UCallExpression::class.java)
  }

  override fun createUastHandler(context: JavaContext): UElementHandler =
    OnViewInOnActivityHandler(context, ISSUE)

  companion object {
    /** Issue describing the problem and pointing to the detector implementation. */
    @JvmField
    val ISSUE: Issue =
      Issue.create(
        id = "OnViewInOnActivityCheck",
        briefDescription = "Warn about calling `onView` in `onActivity`",
        explanation =
          """
        The code block passed into the `ActivityScenario#onActivity` method will be running on the \
        main thread, while you are only supposed to call the `onView` method on the test thread in \
        an instrumentation test.
        
        Please move the `onView` call out of `ActivityScenario#onActivity` to avoid failures and \
        make your tests compatible with the instrumentation test environment.
        """,
        category = Category.CORRECTNESS,
        priority = 6,
        severity = Severity.WARNING,
        implementation = Implementation(OnViewInOnActivityCheck::class.java, Scope.JAVA_FILE_SCOPE)
      )
  }
}
