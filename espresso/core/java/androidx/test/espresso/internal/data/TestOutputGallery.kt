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

package androidx.test.espresso.internal.data

/**
 * Represents a gallery of the test output metadata.
 *  e.g. the screens visited, and actions performed, in an Espresso test.
 */
class TestOutputGallery {
  // A reference to the first [ScreenData] node.
  var head: ScreenData? = null
  // A reference to the last [ScreenData] node.
  var tail: ScreenData? = null

  /**
   * Adds a [ScreenData] object to the graph.
   */
  fun addScreen(screen: ScreenData) {
    if (head == null) {
      head = screen
    } else {
      tail!!.addAction(ActionData(tail!!, screen))
    }
    tail = screen
  }
}
