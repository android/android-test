/*
 * Copyright (C) 2026 The Android Open Source Project
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

import android.os.Message;

/** Interface for deciding if a MessageQueue under interrogation is idle. */
interface QueueIdleDetector {

  /** Records that a message has been dispatched at the given time. */
  void recordDispatch(long now, Message m);

  /**
   * Evaluates whether the queue is idle.
   *
   * @param now the current uptime millis
   * @param headWhen the execution uptime millis of the next message in the queue, or null if empty
   * @return true if the queue should be considered idle, false otherwise.
   */
  boolean isIdle(long now, Long headWhen);
}
