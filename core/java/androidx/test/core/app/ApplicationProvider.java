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
package androidx.test.core.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

/**
 * Provides ability to retrieve the current application {@link Context} in tests.
 *
 * <p>This can be useful if you need to access the application assets (eg
 * <i>getApplicationContext().getAssets()</i>), preferences (eg
 * <i>getApplicationContext().getSharedPreferences()</i>), file system (eg
 * <i>getApplicationContext().getDir()</i>) or one of the many other context APIs in test.
 */
public final class ApplicationProvider {

  private ApplicationProvider() {}

  /**
   * Returns the application {@link Context} for the application under test.
   *
   * @see {@link Context#getApplicationContext()}
   */
  @SuppressWarnings("unchecked")
  public static <T extends Context> T getApplicationContext() {
    return (T)
        InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
  }
}
