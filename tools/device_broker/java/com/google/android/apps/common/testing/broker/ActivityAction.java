/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Intent actions that can be used to start activities.
 *
 * Largely cribbed from android's Intent class, use custom for custom actions.
 *
 */
public interface ActivityAction {

  /**
   * Returns the name of this activity.
   */
  public String getActionName();


  /**
   * Use in the case where there are custom (non-android sdk) actions.
   */
  public static class Custom implements ActivityAction {
    private final String customName;

    public Custom(String customName) {
      this.customName = checkNotNull(customName);
    }

    @Override
    public String getActionName() {
      return customName;
    }

    @Override
    public String toString() {
      return "CustomAction: " + customName;
    }
  }

  /**
   * Mirrors the standard android sdk intent actions for activities.
   */
  public enum Standard implements ActivityAction {
    MAIN, VIEW, ATTACH_DATA, EDIT, PICK, CHOOSER, GET_CONTENT, DIAL, CALL, SEND, SENDTO, ANSWER,
    INSERT, DELETE, RUN, SYNC, PICK_ACTIVITY, SEARCH, WEB_SEARCH;

    @Override
    public String getActionName() {
      return "android.intent.action." + name();
    }
  }
}
