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

package androidx.test.espresso.intent.matcher;

import static androidx.test.espresso.intent.RuntimePermissionsStubber.EXTRA_REQUEST_PERMISSIONS_NAMES;
import static androidx.test.espresso.intent.RuntimePermissionsStubber.Matchers.ACTION_REQUEST_PERMISSIONS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import android.Manifest.permission;
import android.content.Intent;
import androidx.test.espresso.intent.RuntimePermissionsStubber.Matchers;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for runtime permission matchers. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RuntimePermissionMatchersTest {

  @Test
  public void anyPermissionMatcher_MatchesAnyPermissionIntent() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    Matcher<Intent> anyPermissionMatcher = Matchers.anyPermission();

    assertThat(anyPermissionMatcher.matches(permissionIntent), equalTo(true));
  }

  @Test
  public void anyPermissionMatcher_DoesNotMatchNonPermissionIntent() {
    Intent permissionIntent = new Intent();
    Matcher<Intent> anyPermissionMatcher = Matchers.anyPermission();

    assertThat(anyPermissionMatcher.matches(permissionIntent), equalTo(false));
  }

  @Test
  public void hasPermissionMatcher_MatchesPermissionIntent() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    permissionIntent.putExtra(
        EXTRA_REQUEST_PERMISSIONS_NAMES, new String[] {permission.READ_PHONE_STATE});

    Matcher<Intent> anyPermissionMatcher = Matchers.hasPermission(permission.READ_PHONE_STATE);

    assertThat(anyPermissionMatcher.matches(permissionIntent), equalTo(true));
  }

  @Test
  public void hasPermissionMatcher_DoesNotMatchFalsePermission() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    permissionIntent.putExtra(
        EXTRA_REQUEST_PERMISSIONS_NAMES, new String[] {permission.READ_PHONE_STATE});

    Matcher<Intent> anyPermissionMatcher = Matchers.hasPermission(permission.GET_ACCOUNTS);

    assertThat(anyPermissionMatcher.matches(permissionIntent), equalTo(false));
  }

  @Test
  public void hasPermissionMatcher_MatchesIntentThatContainsPermission() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    permissionIntent.putExtra(
        EXTRA_REQUEST_PERMISSIONS_NAMES,
        new String[] {permission.READ_PHONE_STATE, permission.GET_ACCOUNTS});

    Matcher<Intent> readPhonePermissionMatcher =
        Matchers.hasPermission(permission.READ_PHONE_STATE);
    assertThat(readPhonePermissionMatcher.matches(permissionIntent), equalTo(true));

    Matcher<Intent> getAccountsPermissionMatcher = Matchers.hasPermission(permission.GET_ACCOUNTS);
    assertThat(getAccountsPermissionMatcher.matches(permissionIntent), equalTo(true));

    Matcher<Intent> callLogPermissionMatcher = Matchers.hasPermission("not a permission");
    assertThat(callLogPermissionMatcher.matches(permissionIntent), equalTo(false));
  }

  @Test
  public void hasPermissionsMatcher_MatchesAllPermissions() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    permissionIntent.putExtra(
        EXTRA_REQUEST_PERMISSIONS_NAMES,
        new String[] {permission.READ_PHONE_STATE, permission.GET_ACCOUNTS});

    Matcher<Intent> readPhonePermissionMatcher =
        Matchers.hasPermissions(permission.READ_PHONE_STATE, permission.GET_ACCOUNTS);
    assertThat(readPhonePermissionMatcher.matches(permissionIntent), equalTo(true));
  }

  @Test
  public void hasPermissionsMatcher_MatchesSubsetOfPermissions() {
    Intent permissionIntent = new Intent(ACTION_REQUEST_PERMISSIONS);
    permissionIntent.putExtra(
        EXTRA_REQUEST_PERMISSIONS_NAMES,
        new String[] {
          permission.READ_PHONE_STATE, permission.GET_ACCOUNTS, permission.READ_CONTACTS
        });

    Matcher<Intent> readPhonePermissionMatcher =
        Matchers.hasPermissions(permission.READ_PHONE_STATE, permission.GET_ACCOUNTS);
    assertThat(readPhonePermissionMatcher.matches(permissionIntent), equalTo(true));
  }
}
