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

package androidx.test.espresso;

import android.view.View;
import org.hamcrest.Matcher;

/**
 * Responsible for performing an interaction on the given View element.<br>
 *
 * <p>This is part of the test framework public API - developers are free to write their own
 * ViewAction implementations when necessary. When implementing a new ViewAction, follow these
 * rules:
 *
 * <ul>
 *   <li>Inject motion events or key events via the UiController to simulate user interactions.
 *   <li>Do not mutate the view directly via setter methods and other state changing methods on the
 *       view parameter.
 *   <li>Do not throw AssertionErrors. Assertions belong in ViewAssertion classes.
 *   <li>View action code will executed on the UI thread, therefore you should not block, perform
 *       sleeps, or perform other expensive computations.
 *   <li>The test framework will wait for the UI thread to be idle both before and after perform()
 *       is called. This means that the action is guaranteed to be synchronized with any other view
 *       operations.
 *   <li>Downcasting the View object to an expected subtype is allowed, so long as the object
 *       expresses the subtype matches the constraints as specified in {@code getConstraints}.
 * </ul>
 */
public interface ViewAction {

  /**
   * A mechanism for ViewActions to specify what type of views they can operate on.
   *
   * <p>A ViewAction can demand that the view passed to perform meets certain constraints. For
   * example it may want to ensure the view is already in the viewable physical screen of the device
   * or is of a certain type.
   *
   * @return a <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   *     <code>Matcher</code></a> that will be tested prior to calling perform.
   */
  public Matcher<View> getConstraints();

  /**
   * Returns a description of the view action. The description should not be overly long and should
   * fit nicely in a sentence like: "performing %description% action on view with id ..."
   */
  public String getDescription();

  /**
   * Performs this action on the given view.
   *
   * @param uiController the controller to use to interact with the UI.
   * @param view the view to act upon. never null.
   */
  public void perform(UiController uiController, View view);
}
