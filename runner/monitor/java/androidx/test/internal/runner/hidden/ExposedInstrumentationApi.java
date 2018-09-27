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

package androidx.test.internal.runner.hidden;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

/**
 * Exposes select hidden android apis to the compiler and to enable recording and stubbing of
 * intents that pass through the execStartActivity methods. These methods are stripped from the
 * android.jar sdk compile time jar, however are called at runtime (and exist in the android.jar on
 * the device).
 *
 * <p>This class will actually never be included in our .aar!
 */
public class ExposedInstrumentationApi extends Instrumentation {

  /**
   * This API was removed in Android API 15 (ICE_CREAM_SANDWICH_MR1). We need to keep it around in
   * case the code is compiled against API 15.
   */
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode) {
    throw new RuntimeException("Stub!");
  }

  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options) {
    throw new RuntimeException("Stub!");
  }

  public void execStartActivities(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent[] intents,
      Bundle options) {
    throw new RuntimeException("Stub!");
  }

  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Fragment target,
      Intent intent,
      int requestCode,
      Bundle options) {
    throw new RuntimeException("Stub!");
  }

  /** This API was added in Android API 23 (M) */
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String target,
      Intent intent,
      int requestCode,
      Bundle options) {
    throw new RuntimeException("Stub!");
  }

  /** This API was added in Android API 17 (JELLY_BEAN_MR1) */
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options,
      UserHandle user) {
    throw new RuntimeException("Stub!");
  }
}
