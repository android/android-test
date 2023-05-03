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

package androidx.test.espresso.device.sizeclass

import androidx.test.annotation.ExperimentalTestApi

/** A class to create buckets for the width of a window. */
@ExperimentalTestApi
public class WidthSizeClass
private constructor(
  private val lowerBound: Int,
  internal val upperBound: Int,
  private val description: String
) {
  override fun toString(): String {
    return description
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as WidthSizeClass
    if (lowerBound != other.lowerBound) return false
    if (upperBound != other.upperBound) return false
    if (description != other.description) return false
    return true
  }

  override fun hashCode(): Int {
    var result = lowerBound
    result = 31 * result + upperBound
    result = 31 * result + description.hashCode()
    return result
  }

  public companion object {
    /** A bucket to represent a compact width window. One use-case is a phone in portrait. */
    @JvmField public val COMPACT: WidthSizeClass = WidthSizeClass(0, 600, "COMPACT")
    /**
     * A bucket to represent a medium width window. Some use-cases are a phone in landscape or a
     * tablet.
     */
    @JvmField public val MEDIUM: WidthSizeClass = WidthSizeClass(600, 840, "MEDIUM")
    /** A bucket to represent an expanded width window. One use-case is a desktop app. */
    @JvmField public val EXPANDED: WidthSizeClass = WidthSizeClass(840, Int.MAX_VALUE, "EXPANDED")

    /**
     * Returns a recommended [WidthSizeClass] for the width of a window given the width in DP.
     *
     * @param dpWidth the width of the window in DP
     * @return A recommended size class for the width
     * @throws IllegalArgumentException if the width is negative
     */
    @JvmStatic
    public fun compute(dpWidth: Int): WidthSizeClass {
      return when {
        dpWidth < COMPACT.lowerBound -> {
          throw IllegalArgumentException("Negative size: $dpWidth")
        }
        dpWidth < COMPACT.upperBound -> COMPACT
        dpWidth < MEDIUM.upperBound -> MEDIUM
        else -> EXPANDED
      }
    }

    /**
     * Returns a recommended width of a window in DP given the [WidthSizeClass].
     *
     * @param sizeClass the size class
     * @return A recommended width in DP in this size class
     */
    @JvmStatic
    public fun getWidthDpInSizeClass(sizeClass: WidthSizeClass): Int {
      return when (sizeClass) {
        WidthSizeClass.COMPACT -> 400
        WidthSizeClass.MEDIUM -> 700
        else -> 1000 // WidthSizeClass.EXPANDED
      }
    }

    /**
     * Returns a [WidthSizeClassEnum] given the [WidthSizeClass].
     *
     * @param sizeClass the size class
     * @return the relevant WidthSizeClassEnum
     */
    @JvmStatic
    public fun getEnum(sizeClass: WidthSizeClass): WidthSizeClassEnum {
      return when (sizeClass) {
        WidthSizeClass.COMPACT -> WidthSizeClassEnum.COMPACT
        WidthSizeClass.MEDIUM -> WidthSizeClassEnum.MEDIUM
        else -> WidthSizeClassEnum.EXPANDED
      }
    }

    public enum class WidthSizeClassEnum(val description: String) {
      COMPACT("COMPACT"),
      MEDIUM("MEDIUM"),
      EXPANDED("EXPANDED"),
    }
  }
}
