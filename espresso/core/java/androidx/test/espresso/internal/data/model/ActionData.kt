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

import androidx.test.espresso.ViewAction

/**
 * [ActionData] stores metadata of [androidx.test.espresso.ViewAction].
 *
 * @param source the [ScreenData] node this ActionData object belongs to.
 * @param dest the [ScreenData] node this ActionData object points to.
 * @param viewActionData Data pertaining to a ViewAction.
 */
data class ActionData(
  val index: Int?,
  val name: String?,
  val desc: String?,
  val constraints: String?
) {
  lateinit var source: ScreenData
  lateinit var dest: ScreenData
  constructor(
    source: ScreenData,
    dest: ScreenData,
  ) : this(null, null, null, null) {
    this.source = source
    this.dest = dest
  }

  constructor(
    index: Int,
    viewAction: ViewAction
  ) : this(
    index,
    viewAction.javaClass.simpleName,
    viewAction.description,
    viewAction.constraints.toString()
  )
}
