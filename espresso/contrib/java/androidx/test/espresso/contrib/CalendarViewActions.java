/*
 * Copyright (C) 2014, 2022 The Android Open Source Project
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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.widget.CalendarView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;

/** Espresso action for interacting with {@link CalendarView}. */
public final class CalendarViewActions {

  // Non-instantiable utility class
  private CalendarViewActions() {}

  /** Returns a {@link ViewAction} that sets a date on a {@link CalendarView}. */
  public static ViewAction setDate(final long epochMilli) {

    return new ViewAction() {

      @Override
      public void perform(UiController uiController, View view) {
        final CalendarView calendarView = (CalendarView) view;
        calendarView.setDate(epochMilli);
      }

      @Override
      public String getDescription() {
        return "set date";
      }

      @Override
      public Matcher<View> getConstraints() {
        return allOf(isAssignableFrom(CalendarView.class), isDisplayed());
      }
    };
  }
}
