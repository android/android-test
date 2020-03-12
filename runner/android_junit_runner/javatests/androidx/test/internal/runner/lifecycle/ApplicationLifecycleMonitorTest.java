/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.internal.runner.lifecycle;

import static org.junit.Assert.assertEquals;

import android.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.runner.lifecycle.ApplicationStage;
import androidx.test.testing.fixtures.AppLifecycleListener;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Simple test to check that registered ApplicationLifecycleCallbacks are performed */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationLifecycleMonitorTest {

  @Test
  public void testCallbacks() {
    assertEquals(2, AppLifecycleListener.stages.size());
    Pair<ApplicationStage, ApplicationStage> firstStagePair = AppLifecycleListener.stages.get(0);
    assertEquals(ApplicationStage.PRE_ON_CREATE, firstStagePair.first);
    assertEquals(ApplicationStage.PRE_ON_CREATE, firstStagePair.second);

    Pair<ApplicationStage, ApplicationStage> secondStagePair = AppLifecycleListener.stages.get(1);
    assertEquals(ApplicationStage.CREATED, secondStagePair.first);
    assertEquals(ApplicationStage.CREATED, secondStagePair.second);
  }
}
