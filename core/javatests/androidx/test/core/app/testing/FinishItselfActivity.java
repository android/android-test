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

/**
 * An activity that finishes itself in {@link #onCreate(Bundle)}.
 *
 * <p>Note: When you call {@link #finish()} in {@link #onCreate(Bundle)}, the specialized lifecycle
 * transition is applied and {@link #onDestroy()} is called immediately after {@link
 * #onCreate(Bundle)}. {@link #onStart()} and {@link #onResume()} are never be called in this
 * scenario.
 *
 * <p>This activity is used to test {@link androidx.test.core.app.ActivityScenario#launch(Class)}
 * ensuring it handles this special lifecycle transitions properly.
 */
public class FinishItselfActivity extends Activity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    finish();
  }
}
