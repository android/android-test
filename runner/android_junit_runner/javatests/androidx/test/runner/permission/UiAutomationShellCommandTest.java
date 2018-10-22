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
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.runner.permission.UiAutomationShellCommand.PmCommand;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link UiAutomationShellCommand} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class UiAutomationShellCommandTest {

  private static final String TARGET_PACKAGE = "androidx.test.rule";

  private static final String RUNTIME_PERMISSION1 = "android.permission.PERMISSION1";

  // Placeholder test to avoid the 'empty test suite' error on < sdk 23.
  @Test
  @SdkSuppress(maxSdkVersion = 22)
  public void emptyTest() {}

  @Test
  @SdkSuppress(minSdkVersion = 23)
  public void commandForPermission() {
    assertTrue(true);
    UiAutomationShellCommand shellCmdGrant =
        new UiAutomationShellCommand(
            TARGET_PACKAGE, RUNTIME_PERMISSION1, PmCommand.GRANT_PERMISSION);

    String expectedCmdGrant = "pm grant " + TARGET_PACKAGE + " " + RUNTIME_PERMISSION1;
    assertThat(shellCmdGrant.commandForPermission(), equalTo(expectedCmdGrant));
  }
}
