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

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.EspressoOptional;
import androidx.test.espresso.util.HumanReadables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Forces an AdapterView to ensure that the data matching a provided data matcher is loaded into the
 * current view hierarchy.
 */
public final class AdapterDataLoaderAction implements ViewAction {
  final Matcher<? extends Object> dataToLoadMatcher;
  final EspressoOptional<Integer> atPosition;
  final AdapterViewProtocol adapterViewProtocol;
  private AdapterViewProtocol.AdaptedData adaptedData;
  private boolean performed = false;
  private final Object dataLock = new Object();

  public AdapterDataLoaderAction(
      Matcher<? extends Object> dataToLoadMatcher,
      EspressoOptional<Integer> atPosition,
      AdapterViewProtocol adapterViewProtocol) {
    this.dataToLoadMatcher = checkNotNull(dataToLoadMatcher);
    this.atPosition = checkNotNull(atPosition);
    this.adapterViewProtocol = checkNotNull(adapterViewProtocol);
  }

  public AdapterViewProtocol.AdaptedData getAdaptedData() {
    synchronized (dataLock) {
      checkState(performed, "perform hasn't beenViewFinderImpl called yet!");
      return adaptedData;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return allOf(isAssignableFrom(AdapterView.class), isDisplayed());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void perform(UiController uiController, View view) {
    AdapterView<? extends Adapter> adapterView = (AdapterView<? extends Adapter>) view;
    List<AdapterViewProtocol.AdaptedData> matchedDataItems = Lists.newArrayList();

    for (AdapterViewProtocol.AdaptedData data :
        adapterViewProtocol.getDataInAdapterView(adapterView)) {

      if (dataToLoadMatcher.matches(data.getData())) {
        matchedDataItems.add(data);
      }
    }

    if (matchedDataItems.size() == 0) {
      StringDescription dataMatcherDescription = new StringDescription();
      dataToLoadMatcher.describeTo(dataMatcherDescription);

      if (matchedDataItems.isEmpty()) {
        dataMatcherDescription.appendText(" contained values: ");
        dataMatcherDescription.appendValue(adapterViewProtocol.getDataInAdapterView(adapterView));
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new RuntimeException("No data found matching: " + dataMatcherDescription))
            .build();
      }
    }

    synchronized (dataLock) {
      checkState(!performed, "perform called 2x!");
      performed = true;
      if (atPosition.isPresent()) {
        int matchedDataItemsSize = matchedDataItems.size() - 1;
        if (atPosition.get() > matchedDataItemsSize) {
          throw new PerformException.Builder()
              .withActionDescription(this.getDescription())
              .withViewDescription(HumanReadables.describe(view))
              .withCause(
                  new RuntimeException(
                      String.format(
                          Locale.ROOT,
                          "There are only %d elements that matched but requested %d element.",
                          matchedDataItemsSize,
                          atPosition.get())))
              .build();
        } else {
          adaptedData = matchedDataItems.get(atPosition.get());
        }
      } else {
        if (matchedDataItems.size() != 1) {
          StringDescription dataMatcherDescription = new StringDescription();
          dataToLoadMatcher.describeTo(dataMatcherDescription);
          throw new PerformException.Builder()
              .withActionDescription(this.getDescription())
              .withViewDescription(HumanReadables.describe(view))
              .withCause(
                  new RuntimeException(
                      "Multiple data elements "
                          + "matched: "
                          + dataMatcherDescription
                          + ". Elements: "
                          + matchedDataItems))
              .build();
        } else {
          adaptedData = matchedDataItems.get(0);
        }
      }
    }

    int requestCount = 0;
    while (!adapterViewProtocol.isDataRenderedWithinAdapterView(adapterView, adaptedData)) {
      if (requestCount > 1) {
        if ((requestCount % 50) == 0) {
          // sometimes an adapter view will receive an event that will block its attempts to scroll.
          adapterView.invalidate();
          adapterViewProtocol.makeDataRenderedWithinAdapterView(adapterView, adaptedData);
        }
      } else {
        adapterViewProtocol.makeDataRenderedWithinAdapterView(adapterView, adaptedData);
      }
      uiController.loopMainThreadForAtLeast(100);
      requestCount++;
    }
  }

  @Override
  public String getDescription() {
    return "load adapter data";
  }
}
