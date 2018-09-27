/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.internal.platform;

import static com.google.common.truth.Truth.assertThat;

import android.os.StrictMode;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that using {@link ServiceLoaderWrapper} avoids StrictMode disk I/O errors in device
 * environments.
 */
@RunWith(AndroidJUnit4.class)
public class ServiceLoaderWrapperTest {

  @Test
  public void loadService_diskReadOn() throws IOException {
    disallowDiskReads();

    assertThat(ServiceLoaderWrapper.loadService(ServiceFixture.class).get(0))
        .isInstanceOf(ServiceFixtureImpl.class);
  }

  @Test
  public void loadService_noEntry() throws IOException {
    disallowDiskReads();

    assertThat(ServiceLoaderWrapper.loadService(ServiceLoaderWrapperTest.class)).isEmpty();
  }

  private static void disallowDiskReads() {
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectDiskReads().penaltyDeath().build());
  }
}
