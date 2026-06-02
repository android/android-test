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

import static com.google.common.truth.Truth.assertThat;

import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QueueIdleDetectorTest {

  @Test
  public void legacyLookahead_emptyQueue_isIdle() {
    QueueIdleDetector detector = new LegacyLookaheadDetector(15);
    assertThat(detector.isIdle(1000, null)).isTrue();
  }

  @Test
  public void legacyLookahead_soonDueTask_isBusy() {
    QueueIdleDetector detector = new LegacyLookaheadDetector(15);
    // Task due at 1010 (10ms from now)
    assertThat(detector.isIdle(1000, 1010L)).isFalse();
  }

  @Test
  public void legacyLookahead_farFutureTask_isIdle() {
    QueueIdleDetector detector = new LegacyLookaheadDetector(15);
    // Task due at 1020 (20ms from now)
    assertThat(detector.isIdle(1000, 1020L)).isTrue();
  }

  @Test
  public void unifiedRecentCount_emptyQueue_noHistory_isIdle() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    assertThat(detector.isIdle(1000, null)).isTrue();
  }

  @Test
  public void unifiedRecentCount_emptyQueue_highHistory_isBusy() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    Message m = new Message();
    // Record 5 dispatches in the last 50ms
    detector.recordDispatch(960, m);
    detector.recordDispatch(970, m);
    detector.recordDispatch(980, m);
    detector.recordDispatch(990, m);
    detector.recordDispatch(995, m);

    // Queue is empty, but we had 5 dispatches -> should be busy (not idle)
    assertThat(detector.isIdle(1000, null)).isFalse();
  }

  @Test
  public void unifiedRecentCount_emptyQueue_prunedHistory_isIdle() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    Message m = new Message();
    // Record dispatches older than 50ms
    detector.recordDispatch(940, m);
    detector.recordDispatch(945, m);
    detector.recordDispatch(948, m);
    detector.recordDispatch(949, m);
    detector.recordDispatch(950, m); // exactly 50ms ago

    // Queue is empty, old history is pruned -> should be idle
    assertThat(detector.isIdle(1000, null)).isTrue();
  }

  @Test
  public void unifiedRecentCount_soonDueTask_isBusy() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    // Next task at 1040 (40ms away, less than 50ms) -> should be busy regardless of history
    assertThat(detector.isIdle(1000, 1040L)).isFalse();
  }

  @Test
  public void unifiedRecentCount_farFutureTask_lowHistory_isIdle() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    Message m = new Message();
    detector.recordDispatch(980, m);
    detector.recordDispatch(990, m);

    // Next task at 1060 (60ms away) and 2 dispatches -> should be idle
    assertThat(detector.isIdle(1000, 1060L)).isTrue();
  }

  @Test
  public void unifiedRecentCount_farFutureTask_highHistory_isBusy() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);
    Message m = new Message();
    detector.recordDispatch(960, m);
    detector.recordDispatch(970, m);
    detector.recordDispatch(980, m);
    detector.recordDispatch(990, m);
    detector.recordDispatch(995, m);

    // Next task at 1060 (60ms away) but 5 dispatches -> should be busy
    assertThat(detector.isIdle(1000, 1060L)).isFalse();
  }

  @Test
  public void unifiedRecentCount_boundaryConditions_evaluatedCorrectly() {
    QueueIdleDetector detector = new UnifiedRecentCountDetector(50, 4);

    // Exactly 50ms away -> should be busy (headDelay must be strictly > 50ms to be idle)
    assertThat(detector.isIdle(1000, 1050L)).isFalse();

    // Exactly 51ms away -> should be idle (headDelay is 51ms, which is > 50ms)
    assertThat(detector.isIdle(1000, 1051L)).isTrue();
  }
}
