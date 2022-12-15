package androidx.test.espresso.device.rules

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.action.setDisplaySize
import androidx.test.espresso.device.controller.DeviceMode
import androidx.test.espresso.device.controller.PhysicalDeviceController
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.util.getDeviceApiLevel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.TestRequestBuilder
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.ui.app.ScreenOrientationActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkipUnsupportedDeviceActionsRuleTest {
  class DisplaySizeTest {
    @get:Rule
    val skipUnsupportedDeviceActionsRule: SkipUnsupportedDeviceActionsRule =
      SkipUnsupportedDeviceActionsRule()

    @Test
    fun testSetDisplaySze() {
      onDevice().perform(setDisplaySize(WidthSizeClass.COMPACT, HeightSizeClass.COMPACT))
    }
  }

  class ExpandedDisplaySizeTest {
    @get:Rule
    val skipUnsupportedDeviceActionsRule: SkipUnsupportedDeviceActionsRule =
      SkipUnsupportedDeviceActionsRule()

    @Test
    fun testSetExpandedDisplaySze() {
      ActivityScenario.launch(ScreenOrientationActivity::class.java).use {
        onDevice().perform(setDisplaySize(WidthSizeClass.EXPANDED, HeightSizeClass.EXPANDED))
      }
    }
  }

  class PhysicalDeviceControllerTest {
    @get:Rule
    val skipUnsupportedDeviceActionsRule: SkipUnsupportedDeviceActionsRule =
      SkipUnsupportedDeviceActionsRule()

    @Test
    fun setScreenOrientationOnPhysicalDevice() {
      val controller = PhysicalDeviceController()
      controller.setScreenOrientation(ScreenOrientation.LANDSCAPE.orientation)
    }

    @Test
    fun setFlatModeOnPhysicalDevice() {
      val controller = PhysicalDeviceController()
      controller.setDeviceMode(DeviceMode.FLAT.mode)
    }
  }

  @Test
  fun skipTestsWithDisplaySizeActionsOnUnsupportedApiLevels() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(DisplaySizeTest::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (getDeviceApiLevel() > 23) {
      assertEquals(0, result.assumptionFailureCount)
    } else {
      assertEquals(1, result.assumptionFailureCount)
    }
  }

  @Test
  fun skipTestsRequestedExpandedDisplaySizeOnUnsupportedDevice() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(ExpandedDisplaySizeTest::class.java.name).build()
    val result: Result = testRunner.run(request)

    if (android.os.Build.MODEL.equals("Generic Foldable (Android)") || getDeviceApiLevel() == 33) {
      assertEquals(0, result.assumptionFailureCount)
    } else {
      assertEquals(1, result.assumptionFailureCount)
    }
  }

  @Test
  fun skipTestsUsingPhysicalDeviceController() {
    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(PhysicalDeviceControllerTest::class.java.name).build()
    val result: Result = testRunner.run(request)

    assertEquals(2, result.assumptionFailureCount)
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
