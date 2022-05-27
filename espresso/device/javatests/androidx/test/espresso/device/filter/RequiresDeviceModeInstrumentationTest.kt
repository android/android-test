package androidx.test.espresso.device.filter

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
  @RequiresDeviceMode(mode = 0)
  fun requiresDeviceMode() {
    assertThat(true).isTrue()
  }

  @Test
  @RequiresDeviceMode(mode = 0)
  @RequiresDeviceMode(mode = 1)
  @RequiresDeviceMode(mode = 2)
  fun requiresMultipleDeviceModes() {
    assertThat(true).isTrue()
  }

  @RequiresDeviceMode(mode = -1)
  @Test
  fun requiresDeviceModeWithInvalidMode_shouldNeverRun() {
    assertThat(false).isTrue()
  }
}
