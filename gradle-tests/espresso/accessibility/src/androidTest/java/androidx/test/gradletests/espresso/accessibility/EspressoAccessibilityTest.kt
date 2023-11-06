package androidx.test.gradletests.espresso.accessibility

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

/** Basic test using AccessibilityChecks. */
@RunWith(AndroidJUnit4::class)
@LargeTest
class EspressoAccessibilityTest {
  @Test
  fun accessibilityChecks() {
    AccessibilityChecks.enable()
    ActivityScenario.launch(EspressoAccessibilityActivity::class.java).use {
      onView(withId(R.id.openBrowserButton)).perform(click())
    }
  }
}
