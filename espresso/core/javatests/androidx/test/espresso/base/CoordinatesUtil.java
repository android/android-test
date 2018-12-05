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

package androidx.test.espresso.base;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ActivityScenario.ActivityAction;
import androidx.test.ui.app.R;

class CoordinatesUtil {

  static int[] getCoordinatesInMiddleOfSendButton(
      Activity activity, Instrumentation instrumentation) {
    final View sendButton = activity.findViewById(R.id.send_button);
    final int[] xy = new int[2];
    instrumentation.runOnMainSync(
        new Runnable() {
          @Override
          public void run() {
            sendButton.getLocationOnScreen(xy);
          }
        });
    int x = xy[0] + (sendButton.getWidth() / 2);
    int y = xy[1] + (sendButton.getHeight() / 2);
    int[] xyMiddle = {x, y};
    return xyMiddle;
  }

  static int[] getCoordinatesInMiddleOfSendButton(ActivityScenario activityScenario) {
    final int[] xyMiddle = new int[2];
    activityScenario.onActivity(
        new ActivityAction() {
          @Override
          public void perform(Activity activity) {
            final View sendButton = activity.findViewById(R.id.send_button);
            final int[] xy = new int[2];
            sendButton.getLocationOnScreen(xy);
            xyMiddle[0] = xy[0] + (sendButton.getWidth() / 2);
            xyMiddle[1] = xy[1] + (sendButton.getHeight() / 2);
          }
        });
    return xyMiddle;
  }

  static float[][] getCoordinatesToDrag() {
    float[][] coords = {
      {200, 500},
      {200, 450},
      {200, 400},
      {200, 350},
      {200, 300},
      {200, 250},
      {200, 200},
      {200, 150},
      {200, 100},
      {200, 50},
      {200, 10},
    };
    return coords;
  }
}
