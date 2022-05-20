package androidx.test.espresso.device.filters

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RequiresDeviceModeTest {
  @Test
  fun alwaysRun() {
    // Keep tests that run on non-foldable emulators from failing with no tests found to run.
    assertTrue(true)
  }

  @Test
  @RequiresDeviceMode(mode = 0)
  fun requiresDeviceMode() {
    assertTrue(true)
  }

  @Test
  @RequiresDeviceMode(mode = 0)
  @RequiresDeviceMode(mode = 1)
  @RequiresDeviceMode(mode = 2)
  fun requiresMultipleDeviceModes() {
    assertTrue(true)
  }

  @RequiresDeviceMode(mode = -1)
  @Test
  fun requiresDeviceModeWithInvalidMode_shouldNeverRun() {
    assertTrue(false)
  }
}
