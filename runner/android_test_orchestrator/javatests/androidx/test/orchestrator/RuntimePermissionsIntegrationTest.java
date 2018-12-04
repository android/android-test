/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.orchestrator;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link AndroidTestOrchestrator} runtime permission handling */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class RuntimePermissionsIntegrationTest {

  private static final String ORCHESTRATOR_PACKAGE = "androidx.test.orchestrator";

  @Test
  @SdkSuppress(minSdkVersion = 24)
  public void verifyEssentialRuntimePermissionsAreGranted() {
    assertThat(
        getInstrumentation()
            .getContext()
            .getPackageManager()
            .checkPermission(permission.READ_EXTERNAL_STORAGE, ORCHESTRATOR_PACKAGE),
        equalTo(PackageManager.PERMISSION_GRANTED));
    assertThat(
        getInstrumentation()
            .getContext()
            .getPackageManager()
            .checkPermission(permission.WRITE_EXTERNAL_STORAGE, ORCHESTRATOR_PACKAGE),
        equalTo(PackageManager.PERMISSION_GRANTED));
  }
}
