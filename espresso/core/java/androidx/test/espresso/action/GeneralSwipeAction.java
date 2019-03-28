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

package androidx.test.espresso.action;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

import android.view.View;
import android.view.ViewConfiguration;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import java.util.Locale;
import org.hamcrest.Matcher;

/** Enables swiping across a view. */
public final class GeneralSwipeAction implements ViewAction {

  /** Maximum number of times to attempt sending a swipe action. */
  private static final int MAX_TRIES = 3;

  /** The minimum amount of a view that must be displayed in order to swipe across it. */
  private static final int VIEW_DISPLAY_PERCENTAGE = 90;

  final CoordinatesProvider startCoordinatesProvider;
  final CoordinatesProvider endCoordinatesProvider;
  final Swiper swiper;
  final PrecisionDescriber precisionDescriber;

  public GeneralSwipeAction(
      Swiper swiper,
      CoordinatesProvider startCoordinatesProvider,
      CoordinatesProvider endCoordinatesProvider,
      PrecisionDescriber precisionDescriber) {
    this.swiper = swiper;
    this.startCoordinatesProvider = startCoordinatesProvider;
    this.endCoordinatesProvider = endCoordinatesProvider;
    this.precisionDescriber = precisionDescriber;
  }

  @Override
  public Matcher<View> getConstraints() {
    return isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE);
  }

  @Override
  public void perform(UiController uiController, View view) {
    float[] startCoordinates = startCoordinatesProvider.calculateCoordinates(view);
    float[] endCoordinates = endCoordinatesProvider.calculateCoordinates(view);
    float[] precision = precisionDescriber.describePrecision();

    Swiper.Status status = Swiper.Status.FAILURE;

    for (int tries = 0; tries < MAX_TRIES && status != Swiper.Status.SUCCESS; tries++) {
      try {
        status = swiper.sendSwipe(uiController, startCoordinates, endCoordinates, precision);
      } catch (RuntimeException re) {
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(re)
            .build();
      }

      int duration = ViewConfiguration.getPressedStateDuration();
      // ensures that all work enqueued to process the swipe has been run.
      if (duration > 0) {
        uiController.loopMainThreadForAtLeast(duration);
      }
    }

    if (status == Swiper.Status.FAILURE) {
      throw new PerformException.Builder()
          .withActionDescription(getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(
              new RuntimeException(
                  String.format(
                      Locale.ROOT,
                      "Couldn't swipe from: %s,%s to: %s,%s precision: %s, %s . Swiper: %s "
                          + "start coordinate provider: %s precision describer: %s. Tried %s times",
                      startCoordinates[0],
                      startCoordinates[1],
                      endCoordinates[0],
                      endCoordinates[1],
                      precision[0],
                      precision[1],
                      swiper,
                      startCoordinatesProvider,
                      precisionDescriber,
                      MAX_TRIES)))
          .build();
    }
  }

  @Override
  public String getDescription() {
    return swiper.toString().toLowerCase() + " swipe";
  }
}
