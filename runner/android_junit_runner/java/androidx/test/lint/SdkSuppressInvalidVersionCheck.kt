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

package androidx.test.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression

/**
 * A lint detector that reports a warning if it detects invalid SDK version code is passed to
 * attributes `minSdkVersion` or `maxSdkVersion` in annotation `SdkSuppress`. The invalid attribute
 * values include the integer 10000 and equivalent constants (e.g.,
 * [android.os.Build.VERSION_CODES.CUR_DEVELOPMENT]), which represent the under-development SDK, as
 * comparing the SDK version with the under-development version code doesn't bring any value.
 *
 * For example,
 * ```java
 *     @SdkSuppress(minSdkVersion = android.os.Build.VERSION_CODES.CUR_DEVELOPMENT)
 * ```
 *
 * The under-development version code should be replaced by specific finalized SDKs versions. If you
 * want your tests to run on an under-development SDK version, you can use attribute `codeName`. For
 * example,
 * ```java
 *     @SdkSuppress(minSdkVersion = 31, codeName = 'S')
 * ```
 * This would work before and after SDK version S is released if S is finalized as 31.
 */
@Suppress("DetectorIsMissingAnnotations")
open class SdkSuppressInvalidVersionCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "SdkSuppressInvalidVersion",
      briefDescription = "Warn about invalid SDK versions in SdkSuppress",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation =
        Implementation(SdkSuppressInvalidVersionCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableUastTypes() = listOf(UAnnotation::class.java)

  override fun createUastHandler(context: JavaContext) =
    object : UElementHandler() {
      override fun visitAnnotation(node: UAnnotation) {
        if (node.uastParent !is UMethod || node.qualifiedName != ANNOTATION_NAME) return
        ATTRIBUTE_NAMES.forEach { attributeName ->
          node.findAttributeValue(attributeName)?.let { attributeValue ->
            if (checkInvalidSdkVersion(attributeValue)) {
              context.report(
                issue,
                attributeValue,
                context.getNameLocation(attributeValue),
                REPORT_MESSAGE.replace("ATTRIBUTE_PLACEHOLDER", attributeName)
                  .replace("VERSION_PLACEHOLDER", attributeValue.asSourceString())
              )
            }
          }
        }
      }
    }

  private fun checkInvalidSdkVersion(node: UElement) =
    (node is ULiteralExpression && node.value is Int && node.value in INVALID_VERSION_INT_LIST) ||
      (node is UQualifiedReferenceExpression &&
        node.asSourceString() in INVALID_VERSION_STRING_LIST)

  companion object {
    private val REPORT_MESSAGE =
      """
        You have an invalid SDK version VERSION_PLACEHOLDER for attribute ATTRIBUTE_PLACEHOLDER in \
        annotation `SdkSuppress`.

        In annotation `SdkSuppress`, attributes `minSdkVersion` or `maxSdkVersion` should not \
        accept the value of 10000. This may be caused by using the version code of an \
        under-development SDK from `android.os.Build.VERSION_CODES`.

        Please replace the invalid version code with versions of finalized SDKs or set attribute \
        `codeName` for under-development SDK versions.
    """
        .trimIndent()
        .replace("\\\n", "")

    private const val ANNOTATION_NAME = "androidx.test.filters.SdkSuppress"

    private val ATTRIBUTE_NAMES = listOf("minSdkVersion", "maxSdkVersion")

    // Cannot directly import VERSION_CODES because Lint checks are JVM libraries while
    // VERSION_CODES are in Android libraries.
    private val INVALID_VERSION_INT_LIST = listOf(10000)

    // android.os.Build.VERSION_CODES.CUR_DEVELOPMENT = 10000
    // There should be another version code equal to CUR_DEVELOPMENT but it is not available in
    // lint detector.
    private val INVALID_VERSION_STRING_LIST =
      listOf(
        "android.os.Build.VERSION_CODES.CUR_DEVELOPMENT",
        "Build.VERSION_CODES.CUR_DEVELOPMENT",
        "VERSION_CODES.CUR_DEVELOPMENT",
        "CUR_DEVELOPMENT"
      )
  }
}
