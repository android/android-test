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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Demonstrates dealing with multiple windows.
 *
 * <p>Espresso provides the ability to switch the default window matcher used in both onView and
 * onData interactions.
 *
 * @see androidx.test.espresso.Espresso#onView
 * @see androidx.test.espresso.Espresso#onData
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MultipleWindowTest {

  @Before
  public void setUp() throws Exception {
    // Espresso will not launch our activity for us, we must launch it via ActivityScenario.launch.
    ActivityScenario.launch(SendActivity.class);
  }

  // Froyo's AutoCompleteTextBox is broken - do not bother testing with it.
  // b/18916590
  @Test
  @SdkSuppress(minSdkVersion = 24)
  public void testInteractionsWithAutoCompletePopup() {
    // Android's Window system allows multiple view hierarchies to layer on top of each other.
    //
    // A real world analogy would be an overhead projector with multiple transparencies placed
    // on top of each other. Each Window is a transparency, and what is drawn on top of this
    // transparency is the view hierarchy.
    //
    // By default Espresso uses a heuristic to guess which Window you intend to interact with.
    // This heuristic is normally 'good enough' however if you want to interact with a Window
    // that it does not select then you'll have to swap in your own root window matcher.


    // Initially we only have 1 window, but by typing into the auto complete text view another
    // window will be layered on top of the screen. Espresso ignore's this layer because it is
    // not connected to the keyboard/ime.
    onView(withId(R.id.auto_complete_text_view))
        .perform(scrollTo())
        .perform(typeText("So"));

    // As you can see, we continue typing oblivious to the new window on the screen.
    // At the moment there should be 2 completions (South China Sea and Southern Ocean)
    // Lets narrow that down to 1 completion.
    onView(withId(R.id.auto_complete_text_view))
        .perform(typeTextIntoFocusedView("uth "));

    // Now we may want to explicitly tap on a completion. We must override Espresso's
    // default window selection heuristic with our own.
    onView(withText("South China Sea"))
        .inRoot(isPlatformPopup())
        .perform(click());

    // And by clicking on the auto complete term, the text should be filled in.
    onView(withId(R.id.auto_complete_text_view))
        .check(matches(withText("South China Sea")));


    // NB: The autocompletion box is implemented with a ListView, so the preferred way
    // to interact with it is onData(). We can use inRoot here too!
    onView(withId(R.id.auto_complete_text_view))
        .perform(clearText())
        .perform(typeText("S"));

    // Which is useful because some of the completions may not be part of the View Hierarchy
    // unless you scroll around the list.
    onData(allOf(instanceOf(String.class), is("Baltic Sea")))
        .inRoot(isPlatformPopup())
        .perform(click());

    onView(withId(R.id.auto_complete_text_view))
        .check(matches(withText("Baltic Sea")));
  }

}


