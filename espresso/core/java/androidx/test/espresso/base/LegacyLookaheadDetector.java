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

/** Legacy lookahead-based idle detector. */
final class LegacyLookaheadDetector implements QueueIdleDetector {

  private final int lookaheadMillis;

  LegacyLookaheadDetector(int lookaheadMillis) {
    this.lookaheadMillis = lookaheadMillis;
  }

  @Override
  public void recordDispatch(long now, Message m) {
    // No-op. Legacy detector does not track dispatch history.
  }

  @Override
  public boolean isIdle(long now, Long headWhen) {
    if (headWhen == null) {
      return true;
    }
    long nowFuz = now + lookaheadMillis;
    return nowFuz <= headWhen;
  }
}
