package androidx.test.eventto;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.eventto.fixtures.SimpleActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventtoTest {

  @Rule
  public ActivityScenarioRule<SimpleActivity> activityScenarioRule =
      new ActivityScenarioRule<>(SimpleActivity.class);

  // @Test
  // public void eventto_find() throws Throwable {
  //   Eventto.onView(withText("Text View")).check(matches(isDisplayed()));
  // }

  @Test
  public void eventto_click() throws Throwable {
    Eventto.onView(withText("Launch delayed")).perform(click());
  }

  //
  // @Test
  // public void espresso_click() throws Throwable {
  //   onView(withText("Launch delayed")).perform(click());
  //
  //   // expect another activity launch
  //   onView(withText("Delayed Activity")).check(matches(isDisplayed()));
  // }

  // @Test
  // public void eventto_findFails() throws Throwable {
  //   onViewWithText("Text View not there").assertThat(isDisplayed());
  // }

  // @Test
  // public void espressoFind() {
  //   onView(withText("Text View")).check(matches(isDisplayed()));
  // }

}
