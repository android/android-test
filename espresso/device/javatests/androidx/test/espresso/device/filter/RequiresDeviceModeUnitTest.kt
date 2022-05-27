package androidx.test.espresso.device.filter

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
    @RequiresDeviceMode(0) @Test fun testRequireFlatMode() {}

    @RequiresDeviceMode(1) @Test fun testRequireTabletopMode() {}

    @RequiresDeviceMode(2) @Test fun testRequireBookMode() {}

    @Test fun testAlwaysRun() {}
  }

  class RequiresMultipleDeviceModesClass {
    @RequiresDeviceMode(0) @RequiresDeviceMode(1) @Test fun testRequireInvalidMode() {}

    @Test fun testAlwaysRun() {}
  }

  class RequiresDeviceModeClassWithInvalidDeviceModeClass {
    @RequiresDeviceMode(-1) @Test fun testRequireInvalidMode() {}

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

  @Test
  fun requiresDeviceMode_invalidDeviceMode() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request =
      b.addTestClass(RequiresDeviceModeClassWithInvalidDeviceModeClass::class.java.name).build()
    val result: Result = testRunner.run(request)

    assertEquals(1, result.getRunCount())
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
