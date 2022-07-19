package androidx.test.espresso.device.filter

import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.HeightSizeClass.Companion.HeightSizeClassEnum
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass.Companion.WidthSizeClassEnum
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.TestRequestBuilder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequiresDisplayUnitTest {
  class RequiresDisplayCompactSizeClass {
    @RequiresDisplay(
      widthSizeClass = WidthSizeClassEnum.COMPACT,
      heightSizeClass = HeightSizeClassEnum.COMPACT
    )
    @Test
    fun requireCompactWidthAndHeight() {}
  }

  class RequiresDisplayMediumSizeClass {
    @RequiresDisplay(
      widthSizeClass = WidthSizeClassEnum.MEDIUM,
      heightSizeClass = HeightSizeClassEnum.MEDIUM
    )
    @Test
    fun requireMediumWidthAndHeight() {}
  }

  class RequiresDisplayCompactWidthMediumHeightSizeClass {
    @RequiresDisplay(
      widthSizeClass = WidthSizeClassEnum.COMPACT,
      heightSizeClass = HeightSizeClassEnum.MEDIUM
    )
    @Test
    fun requireCompactWidthAndMediumHeight() {}
  }

  class RequiresDisplayMediumWidthCompactHeightSizeClass {
    @RequiresDisplay(
      widthSizeClass = WidthSizeClassEnum.MEDIUM,
      heightSizeClass = HeightSizeClassEnum.COMPACT
    )
    @Test
    fun requireCompactWidthAndMediumHeight() {}
  }

  class RequiresDisplayExpandedSizeClass {
    @RequiresDisplay(
      widthSizeClass = WidthSizeClassEnum.EXPANDED,
      heightSizeClass = HeightSizeClassEnum.EXPANDED
    )
    @Test
    fun requireExpandedWidthAndHeight() {}
  }

  @Test
  fun requiresCompactWidthAndHeight() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(RequiresDisplayCompactSizeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (
      getDeviceWidthSizeClass() == WidthSizeClass.COMPACT &&
        getDeviceHeightSizeClass() == HeightSizeClass.COMPACT
    ) {
      assertEquals(1, result.getRunCount())
    } else {
      assertEquals(0, result.getRunCount())
    }
  }

  @Test
  fun requiresMediumWidthAndHeight() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(RequiresDisplayMediumSizeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (
      getDeviceWidthSizeClass() == WidthSizeClass.MEDIUM &&
        getDeviceHeightSizeClass() == HeightSizeClass.MEDIUM
    ) {
      assertEquals(1, result.getRunCount())
    } else {
      assertEquals(0, result.getRunCount())
    }
  }

  @Test
  fun requiresCompactWidthAndMediumHeight() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request =
      b.addTestClass(RequiresDisplayCompactWidthMediumHeightSizeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (
      getDeviceWidthSizeClass() == WidthSizeClass.COMPACT &&
        getDeviceHeightSizeClass() == HeightSizeClass.MEDIUM
    ) {
      assertEquals(1, result.getRunCount())
    } else {
      assertEquals(0, result.getRunCount())
    }
  }

  @Test
  fun requiresMediumWidthAndCompactHeight() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request =
      b.addTestClass(RequiresDisplayMediumWidthCompactHeightSizeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (
      getDeviceWidthSizeClass() == WidthSizeClass.MEDIUM &&
        getDeviceHeightSizeClass() == HeightSizeClass.COMPACT
    ) {
      assertEquals(1, result.getRunCount())
    } else {
      assertEquals(0, result.getRunCount())
    }
  }

  @Test
  fun requiresExpandedWidthAndHeight() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(RequiresDisplayExpandedSizeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (
      getDeviceWidthSizeClass() == WidthSizeClass.EXPANDED &&
        getDeviceHeightSizeClass() == HeightSizeClass.EXPANDED
    ) {
      assertEquals(1, result.getRunCount())
    } else {
      assertEquals(0, result.getRunCount())
    }
  }

  private fun getDeviceWidthSizeClass(): WidthSizeClass {
    val displayMetrics =
      InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().displayMetrics
    val widthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    return WidthSizeClass.compute(widthDp)
  }

  private fun getDeviceHeightSizeClass(): HeightSizeClass {
    val displayMetrics =
      InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().displayMetrics
    val heightDp = (displayMetrics.heightPixels / displayMetrics.density).toInt()
    return HeightSizeClass.compute(heightDp)
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
