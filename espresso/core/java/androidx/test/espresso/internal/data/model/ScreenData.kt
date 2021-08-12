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

/** Stores metadata for a visited screen in an Espresso test. */
class ScreenData() {
  // List of ActionData objects containing metadata for actions originating on this screen.
  private val actions: ArrayList<ActionData> = ArrayList()
  // Artifacts such as screenshots, HSV files, logcat snippets captured on this screen.
  private val artifacts: ArrayList<TestArtifact> = ArrayList()
  // View data objects contained within the screen.
  private val views: ArrayList<ViewData> = ArrayList()
  // Used for traversals. Increment during traversal to avoid cycling.
  var actionIndex = 0

  /** Adds an ActionData with this as source and a given [ScreenData] as the destination. */
  fun addAction(action: ActionData) {
    actions.add(action)
  }

  /** Gets the list of [ActionData] objects corresponding to actions taken from this screen. */
  fun getActions(): List<ActionData> {
    return actions.toList()
  }

  /** Creates and appends an artifact to this [ScreenData] object. */
  fun addArtifact(artifact: TestArtifact) {
    artifacts.add(artifact)
  }

  /** Gets the list of [TestArtifact] objects on this screen. */
  fun getArtifacts(): List<TestArtifact> {
    return artifacts.toList()
  }

  /** Creates and appends a [ViewData] to this object. */
  fun addViewData(viewData: ViewData) {
    views.add(viewData)
  }

  /** Gets the list of [ViewData] objects on this screen. */
  fun getViews(): List<ViewData> {
    return views.toList()
  }
}
