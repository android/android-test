package androidx.test.espresso.device.rules

import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.setDisplaySize
import androidx.test.espresso.device.sizeclass.HeightSizeClass
import androidx.test.espresso.device.sizeclass.WidthSizeClass
import androidx.test.espresso.device.util.executeShellCommand
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.TestRequestBuilder
import androidx.test.multiwindow.app.MultiWindowActivity
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DisplaySizeRuleTest {

  class DisplaySizeRuleTest {
    private val activityRule: ActivityScenarioRule<MultiWindowActivity> =
      ActivityScenarioRule(MultiWindowActivity::class.java)
    private val displaySizeRule: DisplaySizeRule = DisplaySizeRule()

    @get:Rule val ruleChain: RuleChain = RuleChain.outerRule(activityRule).around(displaySizeRule)

    @Test
    fun testSetDisplaySizeToCompact() {
      onDevice().perform(setDisplaySize(WidthSizeClass.COMPACT, HeightSizeClass.COMPACT))
    }
  }

  @Test
  fun testDisplaySizeIsRestoredAfterTest() {
    val startingSize = executeShellCommand("wm size")

    val b: TestRequestBuilder = createBuilder()
    val testRunner: JUnitCore = JUnitCore()
    val request: Request = b.addTestClass(DisplaySizeRuleTest::class.java.name).build()
    testRunner.run(request)

    assertEquals(executeShellCommand("wm size"), startingSize)
  }

  private fun createBuilder(): TestRequestBuilder {
    return TestRequestBuilder(getInstrumentation(), getArguments())
  }
}
