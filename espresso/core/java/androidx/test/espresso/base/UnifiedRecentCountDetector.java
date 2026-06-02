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
import java.util.ArrayDeque;

/** Unified Recent Count heuristic detector (T=50ms, C=4). */
final class UnifiedRecentCountDetector implements QueueIdleDetector {

  private final long windowMs;
  private final int maxDispatches;
  private final ArrayDeque<Long> dispatchHistory = new ArrayDeque<>();

  UnifiedRecentCountDetector(long windowMs, int maxDispatches) {
    this.windowMs = windowMs;
    this.maxDispatches = maxDispatches;
  }

  @Override
  public synchronized void recordDispatch(long now, Message m) {
    dispatchHistory.addLast(now);
    pruneHistory(now);
  }

  @Override
  public synchronized boolean isIdle(long now, Long headWhen) {
    pruneHistory(now);
    long recentDispatches = dispatchHistory.size();

    boolean isIdleHeuristic = false;
    if (headWhen == null) {
      isIdleHeuristic = true;
    } else {
      long headDelay = headWhen - now;
      if (headDelay > windowMs) {
        isIdleHeuristic = true;
      }
    }

    return isIdleHeuristic && recentDispatches <= maxDispatches;
  }

  private void pruneHistory(long now) {
    long threshold = now - windowMs;
    while (!dispatchHistory.isEmpty() && dispatchHistory.peekFirst() < threshold) {
      dispatchHistory.removeFirst();
    }
  }
}
