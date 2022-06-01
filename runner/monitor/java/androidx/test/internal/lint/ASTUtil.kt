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

package third_party.android.androidx_test.runner.monitor.java.androidx.test.internal.lint

import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.AbstractUastVisitor

/** Given a UAST element, visit its ancestors (exclude itself). */
fun UElement.visitAncestors(action: (UElement) -> Boolean) {
  var parent = this.uastParent
  var stop: Boolean
  while (parent != null) {
    stop = action(parent)
    if (stop) break
    parent = parent.uastParent
  }
}

/** Given a UAST element, visit its descendants (exclude itself). */
fun UElement.visitDescendants(action: (UElement) -> Boolean) {
  val visitor =
    object : AbstractUastVisitor() {
      override fun visitElement(node: UElement): Boolean {
        return if (node === this@visitDescendants) {
          false
        } else {
          action(node)
        }
      }
    }
  this.accept(visitor)
}

/** Given a UAST element, visit its children. */
fun UElement.visitChildren(action: (UElement) -> Unit) {
  val visitChildrenAction =
    fun(node: UElement): Boolean {
      if (node.uastParent === this@visitChildren) {
        action(node)
      }
      return true
    }
  this.visitDescendants(visitChildrenAction)
}

/** Given a UAST element, return its next sibling in the AST (return null if not found). */
fun UElement.getNextSibling(): UElement? {
  var state = 0
  var result: UElement? = null
  val action =
    fun(node: UElement) {
      if (node === this@getNextSibling && state == 0) {
        state = 1
      } else if (state == 1) {
        result = node
        state = 2
      }
    }
  this.uastParent?.visitChildren(action)
  return result
}

/** Given a UAST element, return the statement it belongs to (return null if not found). */
fun UElement.getStatement(): UElement? {
  if (this.uastParent is UBlockExpression) {
    return this
  }
  var statementNode: UElement? = null
  val action =
    fun(node: UElement): Boolean {
      return if (node.uastParent == null) {
        true
      } else if (node.uastParent is UBlockExpression) {
        statementNode = node
        true
      } else {
        false
      }
    }
  this.visitAncestors(action)
  return statementNode
}
