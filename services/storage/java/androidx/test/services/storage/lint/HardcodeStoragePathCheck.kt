package androidx.test.services.storage.lint

import androidx.test.services.storage.TestStorageConstants
import androidx.test.tools.lint.LintMethodSignature
import androidx.test.tools.lint.matchMethodSignature
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULiteralExpression

/**
 * A lint detector that reports a warning if it detects hardcoded file/directory paths in Android
 * tests.
 *
 * We recommend to use APIs in [androidx.test.services.storage] instead of using
 * [android.os.Environment.getExternalStorageDirectory]. For example,
 * ```java
 *     File myFile = new File(Environment.getExternalStorageDirectory(), "...");
 * ```
 * should be replaced by
 * ```java
 *     TestStorage testStorage = new TestStorage();
 *     InputStream myInputStream = testStorage.openInputFile("...");
 * ```
 *
 * We also recommend to use constants in [androidx.test.services.storage.TestStorageConstants]
 * instead of hard coded strings. For example, string "googletest/" can be replaced by
 * `TestStorageConstants.ON_DEVICE_PATH_ROOT`.
 */
@Suppress("DetectorIsMissingAnnotations")
open class HardcodeStoragePathCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "HardcodeStoragePath",
      briefDescription = "Warn about unclosed ActivityScenario",
      explanation = "N/A",
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(HardcodeStoragePathCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  private val testStorageConstantsMap = mutableMapOf<String, String>()

  init {
    for (field in TestStorageConstants::class.java.declaredFields) {
      if (field.type == String::class.java) {
        val fieldName = field.name
        var fieldValue = (field.get(TestStorageConstants::class.java) as String)
        if (fieldValue.isNotEmpty() && fieldValue.last() == '/') {
          fieldValue = fieldValue.dropLast(1)
        }
        testStorageConstantsMap[fieldValue] = fieldName
      }
    }
  }

  override fun getApplicableUastTypes() =
    listOf(UCallExpression::class.java, ULiteralExpression::class.java)

  override fun createUastHandler(context: JavaContext) =
    object : UElementHandler() {
      override fun visitCallExpression(node: UCallExpression) {
        if (node.matchMethodSignature(GET_EXTERNAL_STORAGE_DIRECTORY_METHOD_SIGNATURE)) {
          context.report(issue, node, context.getLocation(node), REPORT_MESSAGE_TEST_STORAGE)
        }
      }

      override fun visitLiteralExpression(node: ULiteralExpression) {
        val literalValue = node.value
        if (literalValue is String) {
          testStorageConstantsMap.forEach { entry ->
            val (stringToMatch, constantToReplace) = entry
            if (literalValue.contains(stringToMatch)) {
              context.report(
                issue,
                node,
                context.getLocation(node),
                REPORT_MESSAGE_TEST_STORAGE_CONSTANTS.replace(
                    "STRING_PLACEHOLDER",
                    "$stringToMatch/"
                  )
                  .replace("CONSTANT_PLACEHOLDER", constantToReplace)
              )
            }
          }
        }
      }
    }

  private companion object {
    val REPORT_MESSAGE_TEST_STORAGE =
      """
        Used `getExternalStorageDirectory` to access external files in Android Test.

        Please replace `getExternalStorageDirectory` with `TestStorage` APIs to access files in \
        Android tests.
    """
        .trimIndent()
        .replace("\\\n", "")

    val REPORT_MESSAGE_TEST_STORAGE_CONSTANTS =
      """
        Used unnecessarily hardcoded directory/file paths in Android Test.

        Please replace the constant String "STRING_PLACEHOLDER" with \
        `TestStorageConstants.CONSTANT_PLACEHOLDER`.
    """
        .trimIndent()
        .replace("\\\n", "")

    val GET_EXTERNAL_STORAGE_DIRECTORY_METHOD_SIGNATURE =
      LintMethodSignature("android.os.Environment", "getExternalStorageDirectory", listOf())
  }
}
