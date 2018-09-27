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

package androidx.test.espresso.base;

import androidx.test.espresso.IdlingResource;

/** An {@link IdlingResource} for testing that becomes idle on demand. */
public class OnDemandIdlingResource implements IdlingResource {
  private final String name;

  private boolean isIdle = false;
  private ResourceCallback callback;

  public OnDemandIdlingResource(String name) {
    this.name = name;
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }

  @Override
  public boolean isIdleNow() {
    return isIdle;
  }

  @Override
  public String getName() {
    return name;
  }

  public void forceIdleNow() {
    isIdle = true;
    if (callback != null) {
      callback.onTransitionToIdle();
    }
  }

  public void reset() {
    isIdle = false;
  }

  @Override
  public String toString() {
    return "ODIR: " + name;
  }
}
