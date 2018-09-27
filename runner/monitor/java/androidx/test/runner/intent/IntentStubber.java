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

package androidx.test.runner.intent;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;

/**
 * Interface to intercept activity launch for a given {@link android.content.Intent} and stub {@link
 * ActivityResult} its response.
 *
 * <p>Retrieve instances of the stubber through {@link IntentStubberRegistry}
 *
 * <p>Stubbing intents requires support from Instrumentation, therefore do not expect an instance to
 * be present under any arbitrary instrumentation.
 */
public interface IntentStubber {

  /**
   * Returns the first matching stubbed result for the given activity if stubbed result was set by
   * test author. The method searches the list of existing matcher/response pairs in reverse order
   * of which they were entered; i.e. the last stubbing has the highest priority. If no stubbed
   * result matching the given intent is found, {@code null} is returned.
   *
   * <p>Must be called on main thread.
   */
  public ActivityResult getActivityResultForIntent(Intent intent);
}
