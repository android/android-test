/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.platform.graphics;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple unit test for {@link HardwareRendererCompat} for platforms where the HardwareRenderer
 * drawing enabled apis are not available.
 */
@RunWith(AndroidJUnit4.class)
public class HardwareRendererCompatUnsupportedTest {

  @Test
  public void setDrawingEnabled() {
    assertThat(HardwareRendererCompat.isDrawingEnabled()).isTrue();
    HardwareRendererCompat.setDrawingEnabled(false);
    // verify ignored
    assertThat(HardwareRendererCompat.isDrawingEnabled()).isTrue();
  }
}
