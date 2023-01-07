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
@file:JvmName("IterablesKt")

package androidx.test.espresso.util

import androidx.annotation.RestrictTo
import org.hamcrest.Matcher

/**
 * A wrapper around kotlin' filter extension that takes a Matcher.
 *
 * @param iterable the sequence of items to filter
 * @param matcher the [Matcher] to perform the filtering.
 * @return subset of iterable that matched
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> filter(iterable: Iterable<T>, matcher: Matcher<T>): Iterable<T> {
  return iterable.filter { matcher.matches(it) }
}

/**
 * A variant of [filter] that returns a [List]
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> filterToList(iterable: Iterable<T>, matcher: Matcher<T>): List<T> {
  return filter(iterable, matcher).toList()
}
