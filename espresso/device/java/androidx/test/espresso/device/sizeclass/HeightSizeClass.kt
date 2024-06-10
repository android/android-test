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

import androidx.test.platform.app.InstrumentationRegistry
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass

/**
 * A class to create buckets for the height of a window.
 *
 * For details on window size classes, see
 * https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes.
 */
public class HeightSizeClass
private constructor(private val windowHeightSizeClass: WindowHeightSizeClass) {
  public companion object {
    /** A bucket to represent a compact height. One use-case is a phone that is in landscape. */
    @JvmField public val COMPACT: HeightSizeClass = HeightSizeClass(WindowHeightSizeClass.COMPACT)
    /** A bucket to represent a medium height. One use-case is a phone in portrait or a tablet. */
    @JvmField public val MEDIUM: HeightSizeClass = HeightSizeClass(WindowHeightSizeClass.MEDIUM)
    /**
     * A bucket to represent an expanded height window. One use-case is a tablet or a desktop app.
     */
    @JvmField public val EXPANDED: HeightSizeClass = HeightSizeClass(WindowHeightSizeClass.EXPANDED)

    /**
     * Returns a recommended [HeightSizeClass] for the height of a window given the height in DP.
     *
     * @param dpHeight the height of the window in DP
     * @return A recommended size class for the height
     * @throws IllegalArgumentException if the height is negative
     */
    @JvmStatic
    public fun compute(dpHeight: Int): HeightSizeClass {
      if (dpHeight < 0) {
        throw IllegalArgumentException("Negative size: $dpHeight")
      }
      val instrumentation = InstrumentationRegistry.getInstrumentation()
      val displayMetrics = instrumentation.getTargetContext().getResources().displayMetrics
      val dpWidth = displayMetrics.widthPixels / displayMetrics.density
      val heightSizeClass =
        WindowSizeClass.compute(dpWidth, dpHeight.toFloat()).windowHeightSizeClass
      return when (heightSizeClass) {
        WindowHeightSizeClass.COMPACT -> COMPACT
        WindowHeightSizeClass.MEDIUM -> MEDIUM
        else -> EXPANDED
      }
    }

    /**
     * Returns a recommended height of a window in DP given the [HeightSizeClass].
     *
     * @param sizeClass the size class
     * @return A recommended height in DP in this size class
     */
    @JvmStatic
    public fun getHeightDpInSizeClass(sizeClass: HeightSizeClass): Int {
      return when (sizeClass) {
        HeightSizeClass.COMPACT -> 400
        HeightSizeClass.MEDIUM -> 700
        else -> 1000 // HeightSizeClass.EXPANDED
      }
    }

    /**
     * Returns a [HeightSizeClassEnum] given the [HeightSizeClass].
     *
     * @param sizeClass the size class
     * @return the relevant HeightSizeClassEnum
     */
    @JvmStatic
    public fun getEnum(sizeClass: HeightSizeClass): HeightSizeClassEnum {
      return when (sizeClass) {
        HeightSizeClass.COMPACT -> HeightSizeClassEnum.COMPACT
        HeightSizeClass.MEDIUM -> HeightSizeClassEnum.MEDIUM
        else -> HeightSizeClassEnum.EXPANDED
      }
    }

    public enum class HeightSizeClassEnum(val description: String) {
      COMPACT("COMPACT"),
      MEDIUM("MEDIUM"),
      EXPANDED("EXPANDED"),
    }
  }
}
