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
@file:JvmName("StringJoinerKt")

package androidx.test.espresso.util

import androidx.annotation.RestrictTo

/**
 * A simplification wrapper around kotlin's joinToString.
 *
 * kotlin's joinToString has many parameters and thus is a bit unwieldy to call directly from java.
 * This method exposes just the needed parameters for most cases.
 *
 * @param the [Iterable] whose elements should be joined
 * @param delimiter the separator to add between each element
 * @return the combined [String]
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun joinToString(iterable: Iterable<kotlin.Any>, delimiter: String): String {
  return iterable.joinToString(separator = delimiter)
}
