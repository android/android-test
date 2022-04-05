package androidx.test.espresso.device.filters

import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@SdkSuppress(minSdkVersion = 17)
class RequiresDisplayTest {
  @Test
  @RequiresDisplay(1, 240)
  fun requiresDisplay() {
    assertTrue(true)
  }

  @Test
  @RequiresDisplay(2, 420)
  fun requiresDisplayOnLargeDevice() {
    assertTrue(true)
  }

  @Test
  @RequiresDisplay(1, -1)
  fun requiresDisplayWithInvalidDensity_shouldNeverRun() {
    assertTrue(false)
  }

  @Test
  @RequiresDisplay(-1, 240)
  fun requiresDisplayWithInvalidScreenSize_shouldNeverRun() {
    assertTrue(false)
  }
}
