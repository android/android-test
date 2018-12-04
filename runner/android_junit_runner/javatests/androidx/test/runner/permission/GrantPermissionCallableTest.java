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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.runner.permission.RequestPermissionCallable.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link GrantPermissionCallable} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class GrantPermissionCallableTest {

  private static final String TARGET_PACKAGE = "androidx.test.rule";

  private static final String RUNTIME_PERMISSION1 = "android.permission.PERMISSION1";

  @Mock public Context targetContext;

  @Mock public ShellCommand shellCommand;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    withStubbedTargetPackage();
  }

  @Test
  public void grantingSuccess() throws Exception {
    RequestPermissionCallable grantCallable = withGrantPermissionCallable(RUNTIME_PERMISSION1);
    when(targetContext.checkCallingOrSelfPermission(RUNTIME_PERMISSION1))
        .thenReturn(PackageManager.PERMISSION_GRANTED);

    assertThat(grantCallable.call(), equalTo(Result.SUCCESS));
  }

  @Test
  public void grantPermissionCallable_grantingFailure() throws Exception {
    RequestPermissionCallable grantCallable = withGrantPermissionCallable(RUNTIME_PERMISSION1);
    when(targetContext.checkCallingOrSelfPermission(RUNTIME_PERMISSION1))
        .thenReturn(PackageManager.PERMISSION_DENIED);

    assertThat(grantCallable.call(), equalTo(Result.FAILURE));
  }

  private RequestPermissionCallable withGrantPermissionCallable(String permission) {
    return new GrantPermissionCallable(shellCommand, targetContext, permission);
  }

  private void withStubbedTargetPackage() {
    when(targetContext.getPackageName()).thenReturn(TARGET_PACKAGE);
  }
}
