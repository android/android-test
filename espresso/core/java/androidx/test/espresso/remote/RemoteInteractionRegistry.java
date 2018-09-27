/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */

package androidx.test.espresso.remote;

import java.util.concurrent.atomic.AtomicReference;

/** An exposed registry instance to make it easy for callers to get a hold of Remote Interaction */
public class RemoteInteractionRegistry {

  private static final AtomicReference<RemoteInteraction> sInstance =
      new AtomicReference<RemoteInteraction>(new NoopRemoteInteraction());

  private RemoteInteractionRegistry() {
    // singleton - disallow creation
  }

  /**
   * Returns the {@link RemoteInteraction}
   *
   * <p>This remote interaction is not guaranteed to be present under all instrumentations.
   *
   * @return RemoteInteraction the remote interaction for this application. If no remote interaction
   *     has been registered a {@link NoopRemoteInteraction} will be returned.
   */
  public static RemoteInteraction getInstance() {
    return sInstance.get();
  }

  /**
   * Stores the remote interaction in the registry.
   *
   * <p>This is a global registry - so be aware of the impact of calling this method!
   *
   * @param remoteInteraction the remote interaction for this application. <@code>Null</@code>
   *     de-registers any existing monitor.
   */
  public static void registerInstance(RemoteInteraction remoteInteraction) {
    if (null == remoteInteraction) {
      sInstance.set(new NoopRemoteInteraction());
    } else {
      sInstance.set(remoteInteraction);
    }
  }
}
