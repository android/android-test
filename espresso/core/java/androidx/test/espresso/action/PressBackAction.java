/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.action;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import androidx.test.espresso.UiController;

/** Performs an Android press back action */
public final class PressBackAction extends KeyEventActionBase {

  private final boolean conditional;

  /**
   * Performs a press back action
   *
   * @param conditional Indicated whether or not to throw an exception when Espresso navigates
   *     outside the application or process under test. {@code true} will throw.
   */
  public PressBackAction(boolean conditional) {
    this(conditional, new EspressoKey.Builder().withKeyCode(KeyEvent.KEYCODE_BACK).build());
  }

  public PressBackAction(boolean conditional, EspressoKey espressoKey) {
    super(espressoKey);
    this.conditional = conditional;
  }

  @Override
  public void perform(UiController uiController, View view) {

    Activity initialActivity = getCurrentActivity();

    super.perform(uiController, view);

    // Wait for a Stage change of the initial activity.
    waitForStageChangeInitialActivity(uiController, initialActivity);
    // Wait until there are no other pending activities in a foreground stage.
    waitForPendingForegroundActivities(uiController, conditional);
  }
}
