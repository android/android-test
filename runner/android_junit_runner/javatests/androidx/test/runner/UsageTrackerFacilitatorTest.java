/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.runner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import androidx.test.InstrumentationRegistry;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.tracker.UsageTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link UsageTrackerFacilitator} */
@RunWith(AndroidJUnit4.class)
public class UsageTrackerFacilitatorTest {
  private static final String ARGUMENT_DISABLE_ANALYTICS = "disableAnalytics";
  private static final String VALUE_DISABLE_ANALYTICS_TRUE = "true";
  private static final String VALUE_DISABLE_ANALYTICS_FALSE = "false";

  static final String ARGUMENT_ORCHESTRATOR_SERVICE = "orchestratorService";
  static final String ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR = "listTestsForOrchestrator";

  private UsageTrackerFacilitator mAnalyticsFacilitator;

  @Mock private UsageTracker mUsageTracker;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void analyticsDisabled_shouldTrackUsage_ReturnsFalse() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withAnalyticsDisabled());

    assertThat(mAnalyticsFacilitator.shouldTrackUsage(), equalTo(false));
  }

  @Test
  public void analyticsEnabledShouldTrackUsage_ReturnsTrue() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withAnalyticsEnabled());

    assertThat(mAnalyticsFacilitator.shouldTrackUsage(), equalTo(true));
  }

  @Test
  public void analyticsEnabledDoesTrackUsage() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withAnalyticsEnabled());
    mAnalyticsFacilitator.registerUsageTracker(mUsageTracker);

    String usage = "SomeComponent";
    String version = "1.2.3";
    mAnalyticsFacilitator.trackUsage(usage, version);
    mAnalyticsFacilitator.sendUsages();

    verify(mUsageTracker).trackUsage(eq(usage), eq(version));
    verify(mUsageTracker).sendUsages();
  }

  @Test
  public void analyticsDisabledDoesNotTrackUsage() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withAnalyticsDisabled());
    mAnalyticsFacilitator.registerUsageTracker(mUsageTracker);

    String usage = "SomeComponent";
    String version = "1.2.3";
    mAnalyticsFacilitator.trackUsage(usage, version);
    mAnalyticsFacilitator.sendUsages();

    verify(mUsageTracker, never()).trackUsage(eq(usage), eq(version));
    verify(mUsageTracker, never()).sendUsages();
  }

  @Test
  public void orchestratorCollectionRunDoesTrackUsage() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withOrchestratorCollectionRun());
    mAnalyticsFacilitator.registerUsageTracker(mUsageTracker);

    String usage = "SomeComponent";
    String version = "1.2.3";
    mAnalyticsFacilitator.trackUsage(usage, version);
    mAnalyticsFacilitator.sendUsages();

    verify(mUsageTracker).trackUsage(eq(usage), eq(version));
    verify(mUsageTracker).sendUsages();
  }

  @Test
  public void orchestratorSecondaryRunDoesNotTrackUsage() {
    mAnalyticsFacilitator = new UsageTrackerFacilitator(withOrchestratorSecondaryRun());
    mAnalyticsFacilitator.registerUsageTracker(mUsageTracker);

    String usage = "SomeComponent";
    String version = "1.2.3";
    mAnalyticsFacilitator.trackUsage(usage, version);
    mAnalyticsFacilitator.sendUsages();

    verify(mUsageTracker, never()).trackUsage(eq(usage), eq(version));
    verify(mUsageTracker, never()).sendUsages();
  }

  private static RunnerArgs withOrchestratorCollectionRun() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "someOrchestratorValue");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "true");
    return new RunnerArgs.Builder()
        .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
        .build();
  }

  private static RunnerArgs withOrchestratorSecondaryRun() {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_ORCHESTRATOR_SERVICE, "someOrchestratorValue");
    bundle.putString(ARGUMENT_LIST_TESTS_FOR_ORCHESTRATOR, "false");
    return new RunnerArgs.Builder()
        .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
        .build();
  }

  private static RunnerArgs withAnalyticsDisabled(String disabledBooleanValue) {
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_DISABLE_ANALYTICS, disabledBooleanValue);
    return new RunnerArgs.Builder()
        .fromBundle(InstrumentationRegistry.getInstrumentation(), bundle)
        .build();
  }

  private static RunnerArgs withAnalyticsDisabled() {
    return withAnalyticsDisabled(VALUE_DISABLE_ANALYTICS_TRUE);
  }

  private static RunnerArgs withAnalyticsEnabled() {
    return withAnalyticsDisabled(VALUE_DISABLE_ANALYTICS_FALSE);
  }
}
