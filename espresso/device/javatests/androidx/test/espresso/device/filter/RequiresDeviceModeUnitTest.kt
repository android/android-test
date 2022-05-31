package androidx.test.espresso.device.filter

import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.TestRequestBuilder
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequiresDeviceModeUnitTest {
  class SampleRequiresDeviceModeClass {
    @RequiresDeviceMode(DeviceMode.FLAT) @Test fun testRequireFlatMode() {}

    @RequiresDeviceMode(DeviceMode.TABLETOP) @Test fun testRequireTabletopMode() {}

    @RequiresDeviceMode(DeviceMode.BOOK) @Test fun testRequireBookMode() {}

    @Test fun testAlwaysRun() {}
  }

  class RequiresMultipleDeviceModesClass {
    @RequiresDeviceMode(DeviceMode.FLAT)
    @RequiresDeviceMode(DeviceMode.TABLETOP)
    @Test
    fun testRequireInvalidMode() {}

    @Test fun testAlwaysRun() {}
  }

  @Test
  fun requiresDeviceMode() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(SampleRequiresDeviceModeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (android.os.Build.VERSION.SDK_INT > 31 &&
        android.os.Build.MODEL.equals("Generic Foldable (Android)")
    ) {
      assertEquals(4, result.getRunCount())
    } else {
      assertEquals(1, result.getRunCount())
    }
  }

  @Test
  fun requiresMultipleDeviceModes() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(RequiresMultipleDeviceModesClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (android.os.Build.VERSION.SDK_INT > 31 &&
        android.os.Build.MODEL.equals("Generic Foldable (Android)")
    ) {
      assertEquals(2, result.getRunCount())
    } else {
      assertEquals(1, result.getRunCount())
    }
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
