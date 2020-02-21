/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.test.internal.runner.tracker;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class CompositeUsageTrackerTest {

  private UsageTracker usageTracker1;
  private UsageTracker usageTracker2;

  @Before
  public void setUp() {
    usageTracker1 = mock(UsageTracker.class);
    usageTracker2 = mock(UsageTracker.class);
  }

  @Test
  public void trackUsage() {
    CompositeUsageTracker usageTracker = new CompositeUsageTracker(usageTracker1, usageTracker2);
    usageTracker.trackUsage("usage_type", "version");

    verify(usageTracker1).trackUsage(eq("usage_type"), eq("version"));
    verify(usageTracker2).trackUsage(eq("usage_type"), eq("version"));
  }

  @Test
  public void addTracker() {
    CompositeUsageTracker usageTracker = new CompositeUsageTracker();
    usageTracker.addTracker(usageTracker1);
    usageTracker.trackUsage("usage_type", "version");

    verify(usageTracker1).trackUsage(eq("usage_type"), eq("version"));
  }

  @Test
  public void sendUsages() {
    CompositeUsageTracker usageTracker = new CompositeUsageTracker();
    usageTracker.addTracker(usageTracker1);
    usageTracker.addTracker(usageTracker2);
    usageTracker.sendUsages();

    verify(usageTracker1).sendUsages();
    verify(usageTracker2).sendUsages();
  }
}
