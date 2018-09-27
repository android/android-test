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

/**
 * A default implementation of an IdleNotifier which is always idle and invokes a runnable on
 * transition to idle.
 */
class NoopRunnableIdleNotifier implements IdleNotifier<Runnable> {

  @Override
  public boolean isIdleNow() {
    return true;
  }

  @Override
  public void cancelCallback() {}

  @Override
  public void registerNotificationCallback(Runnable r) {
    r.run();
  }
}
