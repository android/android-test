/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.core.app;

import static com.google.common.truth.Truth.assertThat;

import androidx.lifecycle.Lifecycle.State;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Specialized test for ActivityScenario that shares an Activity across tests.
 *
 * <p>See b/72831103
 */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioSharedTest {

  private static ActivityScenario<RecreationRecordingActivity> scenario;

  @BeforeClass
  public static void launchActivity() {
    scenario = ActivityScenario.launch(RecreationRecordingActivity.class);
  }

  @AfterClass
  public static void closeActivity() {
    assertThat(scenario.getState()).isEqualTo(State.RESUMED);
    scenario.close();
  }

  @Test
  public void launchedActivity1() throws Exception {
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
  }

  @Test
  public void launchedActivity2() throws Exception {
      assertThat(scenario.getState()).isEqualTo(State.RESUMED);
  }
}
