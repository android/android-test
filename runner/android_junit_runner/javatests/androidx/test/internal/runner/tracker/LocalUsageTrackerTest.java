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

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.services.storage.TestStorage;
import java.io.Serializable;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class LocalUsageTrackerTest {

  private LocalUsageTracker localUsageTracker;
  private TestStorage testStorage;

  @Before
  public void setUp() {
    testStorage =
        new TestStorage(
            InstrumentationRegistry.getInstrumentation().getContext().getContentResolver());
    localUsageTracker = new LocalUsageTracker(testStorage);
  }

  @Test
  public void trackUsage() throws Exception {
    Map<String, Serializable> properties = testStorage.getOutputProperties();
    localUsageTracker.trackUsage("usageType1", "version1");
    localUsageTracker.trackUsage("usageType2", "version2");
    localUsageTracker.sendUsages();

    properties.put("axt_internal.usageType1", "version1");
    properties.put("axt_internal.usageType2", "version2");
    assertThat(testStorage.getOutputProperties()).containsExactlyEntriesIn(properties);
  }

  @Test
  public void trackUsage_duplicateUsageType() throws Exception {
    Map<String, Serializable> properties = testStorage.getOutputProperties();
    localUsageTracker.trackUsage("usageType3", "version1");
    localUsageTracker.trackUsage("usageType3", "version2");
    localUsageTracker.sendUsages();

    properties.put("axt_internal.usageType3", "version2");
    assertThat(testStorage.getOutputProperties()).containsExactlyEntriesIn(properties);
  }
}
