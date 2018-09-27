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

package androidx.test.espresso.intent;

import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.intent.Checks.checkState;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.RuntimePermissionsStubber.Matchers.anyPermission;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import androidx.test.annotation.Beta;
import androidx.test.runner.intent.IntentStubberRegistry;
import androidx.test.runner.permission.PermissionRequester;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Helper class that makes it easy to stub runtime permissions for UI testing on Android M (API 23)
 * and above.
 *
 * <p>When used in a test method this class will stub the granting/revoking of a runtime permission
 * without showing Android's permissions dialog and therefore not block the App's UI.
 *
 * <p>These APIs are just a wrapper around Intents and are equivalent to the following Intento
 * stubbing code: <code>
 *   intending(PermissionMatcher).respondWith(PermissionActivityResult);
 * </code>
 *
 * <p>Note, a prerequisite to using this API is that every test needs to run in a single
 * Instrumentation and <code>adb shell pm clear</code> must be run before or after each test. The
 * reason for this limitation is how the Android framework revokes permissions at runtime. After a
 * permission is granted for one test, it will remain granted for all other tests. This is
 * problematic because the internal stubbing logic relies on intercepting {@link
 * Activity#requestPermissions(String[], int)} which will be bypassed for subsequent tests. Thus, a
 * previously granted permissions must be revoked after test execution is finished. However, the
 * Android framework will kill the Instrumentation process after handling a permission revoking
 * request which will abort all further test execution.
 *
 * <p>This class contains two APIs that can be used to stub runtime permissions. {@link
 * #intendingGrantedPermissions(String...)} and {@link #intendingRevokedPermissions(String...)} To
 * grant a permission use:
 *
 * <p><code>
 *   intendingGrantedPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION);
 * </code>
 *
 * <p>Revoking a permission using {@link #intendingRevokedPermissions(String...)} works very similar
 * to granting a permission and will return the appropriate {@link ActivityResult} to make your app
 * believe the runtime permission was revoked. To revoke a permission use:
 *
 * <p><code>
 *   intendingRevokedPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION);
 * </code>
 *
 * <p><b>This API is currently in beta.</b>
 */
@TargetApi(value = 23)
@Beta
public final class RuntimePermissionsStubber {

  /**
   * Internal Android extra which used as a key for permission names in an {@link Intent} bundle.
   */
  public static final String EXTRA_REQUEST_PERMISSIONS_NAMES =
      "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES";

  private static final String[] EMPTY = new String[0];

  /**
   * Intercepts the request permission {@link Intent} and returns a grant permission {@link
   * ActivityResult} back to your app.
   *
   * <p>Stubbing a permission grant using this method, will not show the Android permissions dialog
   * and return an {@link ActivityResult} to {@link Activity#onRequestPermissionsResult(int,
   * String[], int[])} which will make your app code believe the permission was actually granted. In
   * order to avoid {@link SecurityException}s when invoking the method protected by a permission
   * this helper will also physically grant the permission under the hood.
   *
   * <p>Calling this method on lower API levels than Android M (API 23) will have no effect.
   *
   * @param grantPermissions a list of permission to grant
   */
  public static final void intendingGrantedPermissions(@NonNull String... grantPermissions) {
    checkNotNull(grantPermissions, "grantPermissions cannot be null");
    // Don't show the permission dialog but still grant the permission under the hood
    PermissionRequester permissionRequester = new PermissionRequester();
    permissionRequester.addPermissions(grantPermissions);
    permissionRequester.requestPermissions();

    requestPermissions(grantPermissions, EMPTY, Activity.RESULT_OK);
  }

  /**
   * Intercepts the request permission {@link Intent} and returns a revocation permission {@link
   * ActivityResult} back to your app.
   *
   * <p>Revoking a permission using this method will not show the Android permissions dialog and
   * return an {@link ActivityResult} to {@link Activity#onRequestPermissionsResult(int, String[],
   * int[])} which will make your app code believe the permission was actually revoked.
   *
   * <p>Calling this method on lower API levels than Android M (API 23) will have no effect.
   *
   * @param revokePermissions a list of permissions to revoke
   */
  public static final void intendingRevokedPermissions(@NonNull String... revokePermissions) {
    checkNotNull(revokePermissions, "grantPermissions cannot be null");
    requestPermissions(EMPTY, revokePermissions, Activity.RESULT_OK);
  }

  private static final void requestPermissions(
      String[] grantPermissions, String[] revokePermissions, int resultCode) {
    checkState(
        IntentStubberRegistry.isLoaded(),
        "This Api is build on top of Intents. Did you" + "call Intents.init()");

    if (VERSION.SDK_INT < 23) {
      return;
    }

    ActivityResult requestPermissionResult =
        new ActivityResultBuilder()
            .withGrantedPermissions(grantPermissions)
            .withRevokedPermissions(revokePermissions)
            .withResultCode(resultCode)
            .build();

    intending(anyPermission()).respondWith(requestPermissionResult);
  }

  /**
   * A collection of hamcrest matchers to help with runtime permission stubbing.
   *
   * <p>Usually you will not need to use these matchers directly and purely rely on: {@link
   * #intendingGrantedPermissions(String...)} or {@link #intendingRevokedPermissions(String...)}
   */
  public static class Matchers {

    /** Internal Android intent action which is used to request runtime permissions. */
    public static final String ACTION_REQUEST_PERMISSIONS =
        "android.content.pm.action.REQUEST_PERMISSIONS";

    /** Matches any runtime permission {@link Intent}. */
    public static Matcher<Intent> anyPermission() {
      return requestedPermission();
    }

    /** Matches runtime permission {@link Intent}s for a particular permission. */
    public static Matcher<Intent> hasPermission(String permission) {
      return hasPermissions(permission);
    }

    /** Matches runtime permission {@link Intent}s for a list of permissions. */
    public static Matcher<Intent> hasPermissions(String... permissions) {
      return hasPermissions(hasItems(permissions));
    }

    /**
     * Matches runtime permission {@link Intent}s for a list of permissions.
     *
     * @param permissionsMatcher that matches a permission string
     */
    public static Matcher<Intent> hasPermissions(
        final Matcher<Iterable<String>> permissionsMatcher) {
      checkNotNull(permissionsMatcher);

      return new TypeSafeMatcher<Intent>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("has permission: ");
          description.appendDescriptionOf(permissionsMatcher);
        }

        @Override
        public boolean matchesSafely(Intent intent) {
          String[] permissions = intent.getStringArrayExtra(EXTRA_REQUEST_PERMISSIONS_NAMES);
          if (null == permissions) {
            return false;
          }
          return allOf(notNullValue(), permissionsMatcher).matches(Arrays.asList(permissions))
              && requestedPermission().matches(intent);
        }
      };
    }

    private static Matcher<Intent> requestedPermission() {

      return new TypeSafeMatcher<Intent>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("requestedPermission");
        }

        @Override
        public boolean matchesSafely(Intent intent) {
          return hasAction(ACTION_REQUEST_PERMISSIONS).matches(intent);
        }
      };
    }
  }

  /** Builds an {@link ActivityResult} for a corresponding permission request. */
  public static class ActivityResultBuilder {

    /**
     * Internal Android extra which is used as a key for permission results in an {@link Intent}
     * bundle.
     */
    private static final String EXTRA_REQUEST_PERMISSIONS_RESULTS =
        "android.content.pm.extra.REQUEST_PERMISSIONS_RESULTS";

    private final Set<String> rejected = new HashSet<>();
    private final Set<String> grantedPermissions = new HashSet<>();

    private int resultCode = Activity.RESULT_OK;

    private static void noOverlap(Set<String> grantedOrRejected, String[] candidatePermissions) {
      grantedOrRejected = Collections.unmodifiableSet(grantedOrRejected);
      Set<String> permissions = new HashSet<>(Arrays.asList(candidatePermissions));

      // Check if set already contains candidate permissions. After the removeAll() call the
      // permissions Set will only contain non duplicate permissions, which we take advantage of
      // below.
      if (permissions.removeAll(grantedOrRejected)) {
        Set<String> overlappingPermissions = new HashSet<>(Arrays.asList(candidatePermissions));
        // Removes all non duplicate and leaves us with a set of overlapping permissions.
        overlappingPermissions.removeAll(permissions);
        StringBuilder errorMessage =
            new StringBuilder("The following permissions are granted & rejected: ");
        for (String duplicate : overlappingPermissions) {
          errorMessage.append("'");
          errorMessage.append(duplicate);
          errorMessage.append("'");
          errorMessage.append(",");
        }
        throw new IllegalArgumentException(errorMessage.toString());
      }
    }

    /**
     * Adds a list permissions to the set of granted permissions for this {@link ActivityResult}.
     *
     * @return ActivityResultBuilder for fluent interface
     */
    public ActivityResultBuilder withGrantedPermissions(String... permissions) {
      noOverlap(rejected, permissions);
      grantedPermissions.addAll(Arrays.asList(permissions));
      return this;
    }

    /**
     * Adds a list permissions to the set of revoked permissions for this {@link ActivityResult}.
     *
     * @return ActivityResultBuilder for fluent interface
     */
    public ActivityResultBuilder withRevokedPermissions(String... permissions) {
      noOverlap(grantedPermissions, permissions);
      rejected.addAll(Arrays.asList(permissions));
      return this;
    }

    /**
     * Adds a result code, ie. {@link Activity#RESULT_OK} to this {@link ActivityResult}.
     *
     * @return ActivityResultBuilder for fluent interface
     */
    public ActivityResultBuilder withResultCode(int activityResultCode) {
      resultCode = activityResultCode;
      return this;
    }

    /** @return runtime permission {@link ActivityResult} */
    public ActivityResult build() {
      if (grantedPermissions.isEmpty() && rejected.isEmpty()) {
        throw new IllegalStateException("no granted or rejected permissions!");
      }
      return new ActivityResult(resultCode, createResultData());
    }

    private Intent createResultData() {
      Intent resultData = new Intent();

      int[] grantResults = new int[grantedPermissions.size() + rejected.size()];
      String[] permissionNames = new String[grantedPermissions.size() + rejected.size()];

      int index = 0;
      for (String p : grantedPermissions) {
        grantResults[index] = PackageManager.PERMISSION_GRANTED;
        permissionNames[index] = p;
        index++;
      }

      for (String p : rejected) {
        grantResults[index] = PackageManager.PERMISSION_DENIED;
        permissionNames[index] = p;
        index++;
      }

      resultData.putExtra(EXTRA_REQUEST_PERMISSIONS_RESULTS, grantResults);
      resultData.putExtra(EXTRA_REQUEST_PERMISSIONS_NAMES, permissionNames);

      return resultData;
    }
  }
}
