package androidx.test.tools.releaseupdater

import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Unit tests around ReleaseUpdater's version validating logic */
@RunWith(JUnit4::class)
class ReleaseUpdaterTest {

  private val releaseUpdater = ReleaseUpdater()

  @Test
  fun testValidateVersionsSimple() {
    releaseUpdater.validateVersions("1.0.1-alpha01", "1.0.1-alpha02")
    releaseUpdater.validateVersions("1.0.1-beta01", "1.0.1-beta02")
    releaseUpdater.validateVersions("1.0.1-rc01", "1.0.1-rc02")
  }

  @Test
  fun testValidateVersionsSameSuffixIncrementToTen() {
    releaseUpdater.validateVersions("1.0.0-alpha09", "1.0.0-alpha10")
    releaseUpdater.validateVersions("1.0.0-beta09", "1.0.0-beta10")
    releaseUpdater.validateVersions("1.0.0-rc09", "1.0.0-rc10")
  }

  @Test
  fun testValidateVersionsFailsOnSuffixPlusTwo() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0-alpha01", "1.0.0-alpha03")
    }
  }

  @Test
  fun testValidateVersionsFailsOnDoubleIncrement() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0-alpha01", "1.1.0-alpha02")
    }
  }

  @Test
  fun testValidateVersionsStableToAlpha() {
    releaseUpdater.validateVersions("1.0.0", "1.1.0-alpha01")
    releaseUpdater.validateVersions("1.0.0", "2.0.0-alpha01")
  }

  @Test
  fun testValidateVersionsFailsOnStableToAlphaOnVersionPlusTwo() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.2.0-alpha01")
    }
  }

  @Test
  fun testValidateVersionsFailsOnStableToAlphaSuffixPlusTwo() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.1.0-alpha02")
    }
  }

  @Test
  fun testValidateVersionsFailsOnStableToAlphaOnPatchIncrement() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.0.1-alpha01")
    }
  }

  @Test
  fun testValidateVersionsFailsOnStableToAlphaOnNoIncrement() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.0.0-alpha01")
    }
  }

  @Test
  fun testValidateVersiosnFailsOnStableToAlphaOnBadIncrement() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "2.1.0-alpha01")
    }
  }

  @Test
  fun testValidateVersionsFailsOnStableToBeta() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.1.0-beta01")
    }
  }

  @Test
  fun testValidateVersionsFailsOnStableToRc() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.1.0-rc01")
    }
  }

  @Test
  fun testValidateVeresionsFailsOnStableToStable() {
    assertThrows(IllegalArgumentException::class.java) {
      releaseUpdater.validateVersions("1.0.0", "1.0.1")
    }
  }

  @Test
  fun testValidateVersionsAlphaToBeta() {
    releaseUpdater.validateVersions("1.0.1-alpha01", "1.0.1-beta01")
    releaseUpdater.validateVersions("1.0.1-alpha02", "1.0.1-beta01")
  }

  @Test
  fun testValidateVersionsBetaToRc() {
    releaseUpdater.validateVersions("1.0.1-beta01", "1.0.1-rc01")
    releaseUpdater.validateVersions("1.0.1-beta02", "1.0.1-rc01")
  }

  @Test
  fun testValidateVersionsRcToStable() {
    releaseUpdater.validateVersions("1.0.1-rc01", "1.0.1")
    releaseUpdater.validateVersions("1.0.1-rc02", "1.0.1")
  }
}
