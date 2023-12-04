package androidx.test.gradletests.espresso.idling_resource

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class EspressoIdlingResourceTest {
  private lateinit var idlingResource: IdlingResource

  /**
   * Use {@link ActivityScenario to launch and get access to the activity. {@link
   * ActivityScenario#onActivity(ActivityScenario.ActivityAction)} provides a thread-safe mechanism
   * to access the activity.
   */
  @Before
  fun registerIdlingResource() {
    val activityScenario = ActivityScenario.launch(EspressoIdlingResourceActivity::class.java)
    activityScenario.onActivity(
      object : ActivityScenario.ActivityAction<EspressoIdlingResourceActivity> {
        override fun perform(activity: EspressoIdlingResourceActivity) {
          idlingResource = activity.idlingResource
          IdlingRegistry.getInstance().register(idlingResource)
        }
      }
    )
  }

  @Test
  fun changeText_sameActivity() {
    // Type text and then press the button.
    onView(withId(R.id.editTextUserInput))
      .perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard())
    onView(withId(R.id.changeTextBt)).perform(click())

    // Check that the text was changed.
    onView(withId(R.id.textToBeChanged)).check(matches(withText(STRING_TO_BE_TYPED)))
  }

  @After
  fun unregisterIdlingResource() {
    if (idlingResource != null) {
      IdlingRegistry.getInstance().unregister(idlingResource)
    }
  }

  companion object {
    private const val STRING_TO_BE_TYPED = "Espresso"
  }
}
