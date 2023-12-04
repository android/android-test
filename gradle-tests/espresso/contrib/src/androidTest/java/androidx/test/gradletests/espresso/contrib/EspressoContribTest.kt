package androidx.test.gradletests.espresso.contrib

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EspressoContribTest {
  @get:Rule val activityScenarioRule = ActivityScenarioRule(EspressoContribActivity::class.java)

  @Test
  fun scrollToItemBelowFold_checkItsText() {
    onView(withId(R.id.recyclerView))
      .perform(
        RecyclerViewActions.actionOnItemAtPosition<CustomAdapter.ViewHolder>(
          ITEM_BELOW_THE_FOLD,
          click()
        )
      )

    val itemElementText =
      ApplicationProvider.getApplicationContext<Context>()
        .getResources()
        .getString(R.string.item_element_text) + ITEM_BELOW_THE_FOLD.toString()
    onView(withText(itemElementText)).check(matches(isDisplayed()))
  }

  companion object {
    private const val ITEM_BELOW_THE_FOLD = 40
  }
}
