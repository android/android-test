/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.PickerActions.setDate;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.widget.DatePicker;
import android.widget.TimePicker;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link PickerActions}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PickerActionsTest {

  @Before
  public void setUp() throws Exception {
    ActivityScenario.launch(PickersActivity.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSetDateInDatePicker() {
    // Show the date picker
    onView(withId(R.id.button_pick_date)).perform(click());

    // Sets a date on the date picker widget
    onView(isAssignableFrom(DatePicker.class)).perform(setDate(1980, 10, 30));

    // Confirm the selected date. This example uses a standard DatePickerDialog
    // which uses
    // android.R.id.button1 for the positive button id.
    onView(withId(android.R.id.button1)).perform(click());

    // Check if the selected date is correct and is displayed in the Ui.
    onView(withId(R.id.text_view_picked_date)).check(matches(allOf(withText("10/30/1980"),
        isDisplayed())));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSetTimeInTimePicker() {
     // Show the date picker
     onView(withId(R.id.button_pick_time)).perform(click());

     // Sets a time in a view picker widget
     onView(isAssignableFrom(TimePicker.class)).perform(setTime(12, 36));

     // Confirm the time
     onView(withId(android.R.id.button1)).perform(click());

     // Check if the date result is displayed.
     onView(withId(R.id.text_view_picked_time)).check(matches(allOf(withText("12:36"),
       isDisplayed())));
   }

}

