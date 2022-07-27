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

import androidx.test.core.lint.ActivityScenarioConstants.ACTIVITY_SCENARIO_CLASS_NAME
import androidx.test.tools.lint.LintMethodSignature
import com.android.tools.lint.checks.DataFlowAnalyzer
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve

object ActivityScenarioConstants {
  const val ACTIVITY_SCENARIO_CLASS_NAME = "androidx.test.core.app.ActivityScenario"
  val LAUNCH_METHOD_SIGNATURE =
    LintMethodSignature(ACTIVITY_SCENARIO_CLASS_NAME, "launch", listOf("java.lang.Class<A>"))
  val CLOSE_METHOD_SIGNATURE = LintMethodSignature(ACTIVITY_SCENARIO_CLASS_NAME, "close", listOf())
}

/** Checks whether the call is under a `TestRule` class like `ActivityScenarioRule`. */
fun inActivityScenarioRule(node: UCallExpression) =
  node.getParentOfType<UClass>()?.qualifiedName ==
    "androidx.test.ext.junit.rules.ActivityScenarioRule"

/** Checks whether the call is within Java's try-with-resources. */
fun closedWithTryWithResources(launchedScenario: UElement): Boolean {
  if (launchedScenario !is UVariable) return false
  val tryNode = launchedScenario.uastParent
  if (tryNode !is UTryExpression) return false
  return launchedScenario in tryNode.resourceVariables
}

/** Checks whether the call is using Kotlin's `use` function. */
fun closedWithRunBlock(launchCall: UCallExpression): Boolean {
  val method = launchCall.getParentOfType<UMethod>() ?: return false
  var matched = false
  val searchUseCallVisitor =
    object : DataFlowAnalyzer(setOf(launchCall as UElement)) {
      override fun receiver(call: UCallExpression) {
        // Matches only once.
        if (matched) return
        // Since the receiver type is ActivityScenario for sure, we simply match method names
        // instead of method signatures.
        if (call.methodName == "use") {
          matched = true
        }
      }
    }
  method.accept(searchUseCallVisitor)
  return matched
}

/** Checks whether the call is closed manually. */
fun closedManually(
  launchedScenario: UElement?,
  launchCall: UCallExpression,
  closedScenarioReferences: List<PsiElement>,
  closeCalls: List<UCallExpression>
): UCallExpression? {
  if (launchedScenario == null || launchedScenario is ULocalVariable) {
    // The ActivityScenario.launch() is in the declaration of a local variable.
    // We can use Lint's data flow analyzer in this case.
    val method = launchCall.getParentOfType<UMethod>() ?: return null
    var matchedCloseCall: UCallExpression? = null
    val searchCloseCallVisitor =
      object : DataFlowAnalyzer(setOf(launchCall as UElement)) {
        override fun receiver(call: UCallExpression) {
          if (matchedCloseCall != null) return
          if (call.methodName == "close") {
            matchedCloseCall = call
          }
        }
      }
    method.accept(searchCloseCallVisitor)
    return matchedCloseCall
  }

  // In other cases, we need to manually check whether launch() and close() refer to the same
  // ActivityScenario instance.
  // Tries to resolve the returned instance of the launch() call.
  var referenceOfLaunch: PsiElement? = null
  if (launchedScenario is UField) {
    // The ActivityScenario.launch() is in a field. Itself is a declaration.
    referenceOfLaunch = launchedScenario.sourcePsi
  } else if (launchedScenario is USimpleNameReferenceExpression) {
    // The ActivityScenario.launch() is in an assignment. Resolves its declaration.
    referenceOfLaunch = launchedScenario.tryResolve()
  }
  // The reference resolving is failed.
  if (referenceOfLaunch == null) return null

  // Checks whether there is a pair of launch() and close() which refers to the same
  // ActivityScenario instance.
  val matchedIndex = closedScenarioReferences.indexOf(referenceOfLaunch)
  return if (matchedIndex >= 0) {
    closeCalls[matchedIndex]
  } else {
    null
  }
}

/** Finds the ActivityScenario instance that ActivityScenario.launch() returns to. */
fun getActivityScenarioByLaunchCall(launchCall: UCallExpression): UElement? {
  // The ActivityScenario.launch() is in declaration of a field or a local variable.
  val variableNode = launchCall.getParentOfType<UVariable>()
  if (
    variableNode != null &&
      variableNode.type.canonicalText.matches(Regex("$ACTIVITY_SCENARIO_CLASS_NAME<.*>"))
  ) {
    return variableNode
  }

  // The ActivityScenario.launch() is in an assignment.
  val assignmentNode = launchCall.getParentOfType<UBinaryExpression>()
  if (assignmentNode != null) {
    val assignmentType = assignmentNode.leftOperand.getExpressionType()
    if (
      assignmentType != null &&
        assignmentType.canonicalText.matches(Regex("$ACTIVITY_SCENARIO_CLASS_NAME<.*>"))
    ) {
      val leftValue = assignmentNode.leftOperand
      if (leftValue is USimpleNameReferenceExpression) {
        return leftValue
      } else if (
        leftValue is UQualifiedReferenceExpression &&
          leftValue.selector is USimpleNameReferenceExpression
      ) {
        return leftValue.selector
      }
    }
  }
  return null
}

/** Finds the ActivityScenario instance that ActivityScenario.close() uses. */
fun getActivityScenarioByCloseCall(closeCall: UCallExpression): UElement? {
  val parentNodeOfClose = closeCall.uastParent
  if (
    parentNodeOfClose is UQualifiedReferenceExpression && parentNodeOfClose.selector === closeCall
  ) {
    val receiverNode = parentNodeOfClose.receiver
    if (receiverNode is USimpleNameReferenceExpression) {
      return receiverNode
    } else if (
      receiverNode is UQualifiedReferenceExpression &&
        receiverNode.selector is USimpleNameReferenceExpression
    ) {
      return receiverNode.selector
    }
  }
  return null
}
