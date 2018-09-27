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

package androidx.test.services.speakeasy.client;

import android.os.IBinder;

/** Represents a connection to SpeakEasy - the trivial binder registry. */
public interface Connection {

  /**
   * Add a binder to the SpeakEasy binder registry.
   *
   * @param binder The IBinder to publish.
   * @param rr the PublishResultReceiver to consume responses with.
   */
  public void publish(IBinder binder, PublishResultReceiver rr);

  /**
   * Search for a binder in SpeakEasy.
   *
   * @param key the Key to search under.
   * @param rr the FindResultReceiver to consume responses with.
   */
  public void find(String key, FindResultReceiver rr);
}
