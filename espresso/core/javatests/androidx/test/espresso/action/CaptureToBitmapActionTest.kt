package androidx.test.espresso.action

import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.captureToBitmap
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ui.app.MainActivity
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** A simple scuba test to ensure captureToImage works from hjava */
@RunWith(AndroidJUnit4::class)
class CaptureToBitmapActionTest {
  @JvmField @Rule var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  @Throws(IOException::class)
  fun captureToBitmapAndSave() {
    onView(ViewMatchers.isRoot())
      .perform(captureToBitmap { bitmap -> bitmap.writeToTestStorage("captureToBitmapAndSave") })
  }
}
