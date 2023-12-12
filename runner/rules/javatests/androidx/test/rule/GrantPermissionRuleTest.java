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

import static android.content.Context.POWER_SERVICE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertThrows;

import android.Manifest.permission;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

/** Tests for {@link GrantPermissionRule} */
@RunWith(AndroidJUnit4.class)
public class GrantPermissionRuleTest {

  @Test
  public void newWakelock_permissionGranted() {
    WakeLock wakeLock = createWakeLock();
    assertThrows(SecurityException.class, wakeLock::acquire);

    GrantPermissionRule rule = GrantPermissionRule.grant(permission.WAKE_LOCK);
    rule.apply(new EmptyStatement(), Description.EMPTY);
    // now should succeed
    wakeLock.acquire();
    wakeLock.release();
  }

  /**
   * Simple test that external storage permission can be granted without error.
   *
   * <p>This is useful since libraries such as androidx.benchmark depend on this functionality. See
   * b/268058721
   */
  @Test
  public void externalStorage_granted() {
    GrantPermissionRule rule = GrantPermissionRule.grant(permission.WRITE_EXTERNAL_STORAGE);
    rule.apply(new EmptyStatement(), Description.EMPTY);
  }

  private static WakeLock createWakeLock() {
    PowerManager powerManager =
        (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
    return powerManager.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK, "GrantPermissionRuleTest::MyWakelockTag");
  }

  private static class EmptyStatement extends Statement {
    @Override
    public void evaluate() throws Throwable {}
  }
}
