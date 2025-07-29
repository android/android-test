/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.Manifest.permission;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.test.internal.platform.ServiceLoaderWrapper;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.runner.permission.PermissionRequester;
import androidx.test.runner.permission.UiAutomationPermissionGranter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * The {@code GrantPermissionRule} Rule allows granting of runtime permissions on Android M (API 23)
 * and above. Use this {@code Rule} when a test requires a runtime permission to do its work.
 *
 * <p>When applied to a test class this Rule attempts to grant all requested runtime permissions.
 * The requested permissions will then be granted on the device and will take immediate effect.
 * Permissions can only be requested on Android M (API 23) or above and will be ignored on all other
 * API levels. Once a permission is granted it will apply for all tests running in the current
 * Instrumentation. There is no way of revoking a permission after it was granted. Attempting to do
 * so will crash the Instrumentation process.
 *
 * <p>Note, this Rule is usually used to grant runtime permissions to avoid the permission dialog
 * from showing up and blocking the App's Ui. This is especially helpful for Ui-Testing to avoid
 * losing control over the app under test.
 *
 * <p>The requested permissions will be granted for all test methods in the test class. Use {@link
 * #grant(String...)} static factory method to request a variable number of permissions.
 *
 * <p>Usage:
 *
 * <pre>
 * &#064;Rule
 * public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
 *     .grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
 * </pre>
 *
 * <p>
 *
 * <p>Note: As per <a
 * href=https://developer.android.com/reference/android/Manifest.permission.html#READ_EXTERNAL_STORAGE>
 * the documentation</a> this rule will automatically grant {@link
 * android.Manifest.permission#READ_EXTERNAL_STORAGE} when {@link
 * android.Manifest.permission#WRITE_EXTERNAL_STORAGE} is requested.
 *
 * <p>See <a href="https://developer.android.com/training/permissions/requesting">Request App
 * Permissions</a> for more details on runtime permissions.
 *
 * <p>For tests running on Android SDKs >= API 28, use <br>
 * {@link android.app.UiAutomation#grantRuntimePermission(String, String)} instead.
 */
public class GrantPermissionRule implements TestRule {

  private PermissionGranter permissionGranter;

  private GrantPermissionRule() {
    PermissionGranter granter =
        ServiceLoaderWrapper.loadSingleServiceOrNull(PermissionGranter.class);
    if (granter == null) {
      if (VERSION.SDK_INT >= 28) {
        granter = new UiAutomationPermissionGranter();
      } else {
        // use the shell permission requester
        granter = new PermissionRequester();
      }
    }

    setPermissionGranter(granter);
  }

  @VisibleForTesting
  GrantPermissionRule(@NonNull PermissionGranter permissionGranter) {
    setPermissionGranter(permissionGranter);
  }

  /**
   * Static factory method that grants the requested permissions.
   *
   * <p>Permissions will be granted before any methods annotated with {@code &#64;Before} but before
   * any test method execution.
   *
   * @param permissions a variable list of Android permissions
   * @return {@link GrantPermissionRule}
   * @see android.Manifest.permission
   */
  public static GrantPermissionRule grant(String... permissions) {
    GrantPermissionRule grantPermissionRule = new GrantPermissionRule();
    grantPermissionRule.grantPermissions(permissions);
    return grantPermissionRule;
  }

  public static void grantImmediately(String... permissions) {
    GrantPermissionRule grantPermissionRule = new GrantPermissionRule();
    grantPermissionRule.grantPermissions(permissions);
    grantPermissionRule.permissionGranter.requestPermissions();
  }

  private void grantPermissions(String... permissions) {
    Set<String> permissionSet = satisfyPermissionDependencies(permissions);
    permissionGranter.addPermissions(permissionSet.toArray(new String[permissionSet.size()]));
  }

  private Set<String> satisfyPermissionDependencies(String... permissions) {
    Set<String> permissionList = new LinkedHashSet<>(Arrays.asList(permissions));
    // Explicitly grant READ_EXTERNAL_STORAGE permission when WRITE_EXTERNAL_STORAGE was requested.
    if (permissionList.contains(permission.WRITE_EXTERNAL_STORAGE)) {
      permissionList.add(permission.READ_EXTERNAL_STORAGE);
    }
    return permissionList;
  }

  @Override
  public final Statement apply(final Statement base, Description description) {
    return new RequestPermissionStatement(base);
  }

  private void setPermissionGranter(PermissionGranter permissionGranter) {
    this.permissionGranter = checkNotNull(permissionGranter, "permissionRequester cannot be null!");
  }

  private class RequestPermissionStatement extends Statement {

    private final Statement base;

    public RequestPermissionStatement(Statement base) {
      this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
      permissionGranter.requestPermissions();
      base.evaluate();
    }
  }
}
