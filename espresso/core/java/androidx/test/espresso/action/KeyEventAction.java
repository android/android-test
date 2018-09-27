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

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import androidx.test.espresso.UiController;

/** Enables pressing KeyEvents on views. */
public final class KeyEventAction extends KeyEventActionBase {

  public KeyEventAction(EspressoKey espressoKey) {
    super(espressoKey);
  }

  @Override
  public void perform(UiController uiController, View view) {
    Activity initialActivity = getCurrentActivity();

    super.perform(uiController, view);

    if (this.espressoKey.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      // Wait for a Stage change of the initial activity.
      waitForStageChangeInitialActivity(uiController, initialActivity);
      // Wait until there are no other pending activities in a foreground stage.
      waitForPendingForegroundActivities(uiController, true /*conditional*/);
    }
  }
}
