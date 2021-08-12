/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.espresso.internal.data.model

/**
 * Represents a gallery of the test output metadata. e.g. the screens visited, and actions
 * performed, in an Espresso test.
 */
class TestFlow {
  // A reference to the first [ScreenData] node.
  var head: ScreenData? = null
  // A reference to the last [ScreenData] node.
  var tail: ScreenData? = null
  // A reference to all ScreenData nodes in the graph.
  private val allScreenData: ArrayList<ScreenData> = ArrayList()

  /** Adds a [ScreenData] object to the graph. */
  fun addScreen(screen: ScreenData) {
    allScreenData.add(screen)
    if (head == null) {
      head = screen
    }
    tail?.addAction(ActionData(tail!!, screen))
    tail = screen
  }

  /** Adds a [ScreenData] object to the graph with existing ActionData. */
  fun addScreen(screen: ScreenData, action: ActionData) {
    allScreenData.add(screen)
    if (head == null) {
      head = screen
    }
    tail!!.addAction(action)
    tail = screen
  }

  fun getSize(): Int {
    return allScreenData.size
  }

  /** Resets each [ScreenData] object. */
  fun resetTraversal() {
    for (screenData in allScreenData) {
      screenData.actionIndex = 0
    }
  }

  /** Gets the [ActionData] that has a certain index. Null if not found. */
  fun getEdge(index: Int): ActionData? {
    if (head == null) {
      return null
    }
    var curr: ScreenData = head!!
    resetTraversal()
    while (curr.getActions().isNotEmpty()) {
      val nextAction: ActionData = curr.getActions()[curr.actionIndex]
      curr.actionIndex++
      if (nextAction.index != null && index == nextAction.index) {
        return nextAction
      }
      curr = nextAction.dest
    }
    return null
  }
}
