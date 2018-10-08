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

package androidx.test.ui.app;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.Manifest.permission;
import androidx.test.espresso.intent.ResettingStubberImpl;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link ResettingStubberImpl}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class GrantPermissionRuleIntegrationTest {

  @Rule
  public ActivityTestRule<RuntimePermissionsActivity> mActivityTestRule =
      new ActivityTestRule<>(RuntimePermissionsActivity.class);

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE, permission.GET_ACCOUNTS);

  @Test
  public void grantPermissionFlow() {
    onView(withId(R.id.request_phone_state_permission)).perform(click());

    onView(withId(R.id.phone_state_permission_permission_status))
        .check(matches(withText(getTargetContext().getString(R.string
            .permissionGranted))));

    onView(withId(R.id.request_get_accounts_permission)).perform(click());

    onView(withId(R.id.get_accounts_permission_status))
        .check(matches(withText(getTargetContext().getString(R.string
            .permissionGranted))));
  }
}
