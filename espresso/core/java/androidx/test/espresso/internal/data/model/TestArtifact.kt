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
 * An abstraction for a test artifact representing a file to be included in a [ScreenData] node.
 *
 * @param filepath a String containing the path to the desired test output artifact file.
 * @param contentType the type of the file such as: .png, .txt, .webm.
 */
data class TestArtifact(
  val filepath: String,
  val contentType: String,
)
