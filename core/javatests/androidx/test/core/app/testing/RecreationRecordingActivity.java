/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.core.app.testing;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.Window;

/**
 * A minimum activity to demonstrate unit testing relating to Activity's life cycle events. It
 * records the number of times itself is recreated by the lifecycle event.
 */
public class RecreationRecordingActivity extends Activity {

  private static final String KEY_NUM_OF_CREATION = "NUM_OF_CREATION";

  private int numOfCreation = 0;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    if (savedInstanceState != null) {
      numOfCreation = savedInstanceState.getInt(KEY_NUM_OF_CREATION, 0);
    } else {
      numOfCreation = 0;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(KEY_NUM_OF_CREATION, numOfCreation + 1);
  }

  public int getNumberOfRecreations() {
    return numOfCreation;
  }
}
