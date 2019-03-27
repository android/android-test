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
package androidx.test.core.app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ApplicationProviderTest {

  @Test
  public void getApplicationContext_asContext() {
    assertThat(getApplicationContext().getPackageName()).isEqualTo("androidx.test.core");
  }

  @Test
  public void getApplicationContext_asApplication() {
    Application a = getApplicationContext();
    a.registerComponentCallbacks(
        new ComponentCallbacks2() {
          @Override
          public void onTrimMemory(int level) {}

          @Override
          public void onConfigurationChanged(Configuration newConfig) {}

          @Override
          public void onLowMemory() {}
        });
  }
}
