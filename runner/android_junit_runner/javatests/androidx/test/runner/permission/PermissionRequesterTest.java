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

package androidx.test.runner.permission;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.runner.permission.RequestPermissionCallable.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link PermissionRequester} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class PermissionRequesterTest {

  private static final String TARGET_PACKAGE = "androidx.test.rule";

  private static final String RUNTIME_PERMISSION1 = "android.permission.PERMISSION1";
  private static final String RUNTIME_PERMISSION2 = "android.permission.PERMISSION2";
  private static final String RUNTIME_PERMISSION3 = "android.permission.PERMISSION3";

  @Rule public ExpectedException expected = ExpectedException.none();

  @Mock public Context targetContext;

  @Mock public ShellCommand shellCommand;

  private PermissionRequester permissionRequester;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    withStubbedTargetPackage();
    permissionRequester = new PermissionRequester(targetContext);
  }

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void permissionAddsPermissionToSet() {
    RequestPermissionCallable requestPermissionCallable1 =
        withGrantPermissionCallable(RUNTIME_PERMISSION1);
    RequestPermissionCallable requestPermissionCallable2 =
        withGrantPermissionCallable(RUNTIME_PERMISSION2);
    RequestPermissionCallable requestPermissionCallable3 =
        withGrantPermissionCallable(RUNTIME_PERMISSION3);

    permissionRequester.addPermissions(RUNTIME_PERMISSION1);
    permissionRequester.addPermissions(RUNTIME_PERMISSION2);
    permissionRequester.addPermissions(RUNTIME_PERMISSION3);

    assertThat(permissionRequester.requestedPermissions, hasSize(3));
    assertThat(
        permissionRequester.requestedPermissions,
        containsInAnyOrder(
            equalTo(requestPermissionCallable1),
            equalTo(requestPermissionCallable2),
            equalTo(requestPermissionCallable3)));
  }

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void duplicatePermissionThrows() {
    expected.expect(IllegalStateException.class);
    permissionRequester.addPermissions(RUNTIME_PERMISSION1, RUNTIME_PERMISSION1);
  }

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void requestPermission_SuccessInGrantingPermissionRunsTest() throws Throwable {
    RequestPermissionCallable stubbedCallable = withStubbedCallable(Result.SUCCESS);

    permissionRequester.requestPermissions();

    verify(stubbedCallable).call();
  }

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void failureInGrantingPermissionFailsTest() throws Throwable {
    expected.expect(AssertionError.class);

    RequestPermissionCallable stubbedCallable = withStubbedCallable(Result.FAILURE);

    permissionRequester.requestPermissions();

    verify(stubbedCallable).call();
  }

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void callableThrowsExceptionFailsTest() throws Throwable {
    expected.expect(AssertionError.class);

    RequestPermissionCallable stubbedCallable = withStubbedCallable(Result.FAILURE);
    when(stubbedCallable.call()).thenThrow(Exception.class);

    permissionRequester.requestPermissions();

    verify(stubbedCallable).call();
  }

  @Test
  public void deviceNotSupportsPermissionSkipsTest() throws Throwable {
    withRuntimeVersion(Build.VERSION_CODES.LOLLIPOP_MR1);
    permissionRequester.addPermissions(RUNTIME_PERMISSION1);

    assertThat(permissionRequester.requestedPermissions, hasSize(0));
  }

  private void withRuntimeVersion(int sdkInt) {
    assertThat(sdkInt, is(greaterThan(Build.VERSION_CODES.FROYO)));
    permissionRequester.setAndroidRuntimeVersion(sdkInt);
  }

  private void withStubbedTargetPackage() {
    when(targetContext.getPackageName()).thenReturn(TARGET_PACKAGE);
  }

  private RequestPermissionCallable withGrantPermissionCallable(String permission) {
    return new GrantPermissionCallable(shellCommand, targetContext, permission);
  }

  private RequestPermissionCallable withStubbedCallable(Result result) throws Exception {
    RequestPermissionCallable mockCallable = mock(RequestPermissionCallable.class);
    when(mockCallable.call()).thenReturn(result);
    permissionRequester.requestedPermissions.add(mockCallable);
    return mockCallable;
  }
}
