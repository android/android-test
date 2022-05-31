package androidx.test.espresso.device.filter

import androidx.test.espresso.device.controller.DeviceMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RequiresDeviceModeInstrumentationTest {
  @Test
  fun alwaysRun() {
    // Keep tests that run on non-foldable emulators from failing with no tests found to run.
    assertThat(true).isTrue()
  }

  @Test
  @RequiresDeviceMode(mode = DeviceMode.FLAT)
  fun requiresDeviceMode() {
    assertThat(true).isTrue()
  }

  @Test
  @RequiresDeviceMode(mode = DeviceMode.FLAT)
  @RequiresDeviceMode(mode = DeviceMode.TABLETOP)
  @RequiresDeviceMode(mode = DeviceMode.BOOK)
  fun requiresMultipleDeviceModes() {
    assertThat(true).isTrue()
  }
}
