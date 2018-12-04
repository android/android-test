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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.Manifest.permission;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.runner.permission.PermissionRequester;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link GrantPermissionRule} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class GrantPermissionRuleTest {

  private static final String RUNTIME_PERMISSION1 = "android.permission.PERMISSION1";

  @Mock public PermissionRequester permissionRequester;

  private GrantPermissionRule grantPermissionRule;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    grantPermissionRule = GrantPermissionRule.grant(RUNTIME_PERMISSION1);
    grantPermissionRule.setPermissionGranter(permissionRequester);
  }

  @Test
  public void applyingRuleRunsPermissionRunsTest() throws Throwable {
    Statement mockBaseStatement = mock(Statement.class);
    Description mockDescription = mock(Description.class);

    grantPermissionRule.apply(mockBaseStatement, mockDescription).evaluate();

    verify(mockBaseStatement).evaluate();
    verify(permissionRequester).requestPermissions();
  }

  @Test
  public void requestingWriteExternalStoragePermission_addsReadExternalStoragePermission()
      throws Throwable {
    GrantPermissionRule grantPermissionRule = new GrantPermissionRule(permissionRequester);
    Set<String> permissions =
        grantPermissionRule.satisfyPermissionDependencies(permission.WRITE_EXTERNAL_STORAGE);
    assertThat(permissions, hasSize(2));
    assertThat(
        permissions,
        containsInAnyOrder(permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE));
  }

  @Test
  public void requestingPermission_doesNotAddReadExternalStoragePermission() throws Throwable {
    GrantPermissionRule grantPermissionRule = new GrantPermissionRule(permissionRequester);
    assertThat(
        grantPermissionRule.satisfyPermissionDependencies(RUNTIME_PERMISSION1),
        not(contains(permission.READ_EXTERNAL_STORAGE)));
  }
}
