package androidx.test.espresso.device.filters

import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@SdkSuppress(minSdkVersion = 24)
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
