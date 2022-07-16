package androidx.test.core.lint

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
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * A lint detector that reports a warning if it detects an
 * [androidx.test.core.app.ActivityScenario.onActivity] call is redundant because the callback
 * parameter in type of [android.app.Activity] is never used.
 *
 * We recommend to move the body in `ActivityScenario.onActivity` out and delete the
 * `ActivityScenario.onActivity` call.
 */
@Suppress("DetectorIsMissingAnnotations")
open class UnusedOnActivityCheck : Detector(), SourceCodeScanner {

  // TODO(b/234640486): This issue is not used since the check is not implemented.
  protected open val issue: Issue =
    Issue.create(
      id = "UnusedOnActivity",
      briefDescription = "Warn about redundant ActivityScenario.onActivity",
      explanation = REPORT_MESSAGE,
      moreInfo = "",
      category = Category.CORRECTNESS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(UnusedOnActivityCheck::class.java, Scope.JAVA_FILE_SCOPE)
    )

  override fun getApplicableMethodNames() = listOf("onActivity")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    // Matches the signature of onActivity().
    if (method.getMethodSignature() != ON_ACTIVITY_METHOD_SIGNATURE) return

    // We assume the parameter of onActivity is a lambda expression with one parameter.
    val lambdaNode = node.getArgumentForParameter(0) ?: return
    if (lambdaNode !is ULambdaExpression) return
    if (lambdaNode.parameters.size != 1) return
    val lambdaParameter = lambdaNode.parameters[0]
    val lambdaBody = lambdaNode.body

    // Searches the parameter of the lambda in the lambda body.
    var matched = false
    val identifierVisitor =
      object : AbstractUastVisitor() {
        override fun visitSimpleNameReferenceExpression(
          node: USimpleNameReferenceExpression
        ): Boolean {
          if (
            !matched &&
              node.resolvedName == lambdaParameter.name &&
              node.tryResolve() == lambdaParameter.sourcePsi
          ) {
            matched = true
          }
          return false
        }
      }
    lambdaBody.accept(identifierVisitor)
    if (matched) return

    // Reports the warning and provides fix suggestions.
    // Removes the block expression of the lambda body.
    var lambdaBodyString = lambdaBody.asSourceString().trim().removeSurrounding("{", "}").trim()
    // Removes the invalid return after the block is removed.
    lambdaBodyString = lambdaBodyString.removePrefix("return").trim()
    // For java code, the fix may introduce an extra semicolon.
    lambdaBodyString = lambdaBodyString.removeSuffix(";").trim()

    context.report(
      issue,
      node,
      context.getLocation(node),
      REPORT_MESSAGE,
      fix().replace().with(lambdaBodyString).reformat(true).build()
    )
  }

  private companion object {
    val REPORT_MESSAGE =
      """
      The `ActivityScenario.onActivity` is redundant because the parameter of its callback is \
      never used. Please remove the `ActivityScenario.onActivity` call.
    """
        .trimIndent()
        .replace("\\\n", "")

    val ON_ACTIVITY_METHOD_SIGNATURE =
      LintMethodSignature(
        "androidx.test.core.app.ActivityScenario",
        "onActivity",
        listOf("androidx.test.core.app.ActivityScenario.ActivityAction<A>")
      )
  }
}
