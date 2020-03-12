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
import android.util.Pair;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.lifecycle.ApplicationStage;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ApplicationLifecycleCallback} fixture used to ensure ApplicationLifecycleCallback
 * callbacks happen appropriately
 */
public class AppLifecycleListener implements ApplicationLifecycleCallback {

  /**
   * List of ApplicationStage received from ApplicationLifecycleCallback, vs ApplicationStage via
   * MyApplication. They should always match.
   */
  public static List<Pair<ApplicationStage, ApplicationStage>> stages =
      new ArrayList<Pair<ApplicationStage, ApplicationStage>>();

  @Override
  public void onApplicationLifecycleChanged(Application app, ApplicationStage stage) {
    stages.add(new Pair(stage, MyApplication.getStage()));
  }
}
