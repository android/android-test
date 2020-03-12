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

package androidx.test.testing.fixtures;

import android.app.Application;
import androidx.test.runner.lifecycle.ApplicationStage;

/**
 * Application fixture used to verify {@link
 * androidx.test.runner.lifecycle.ApplicationLifecycleCallback}
 */
public class MyApplication extends Application {
  private static volatile ApplicationStage stage = ApplicationStage.PRE_ON_CREATE;

  @Override
  public void onCreate() {
    super.onCreate();
    stage = ApplicationStage.CREATED;
  }

  public static ApplicationStage getStage() {
    return stage;
  }
}
