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
package androidx.test.platform.app;

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** {@link InstrumentationRegistry} tests. */
@RunWith(AndroidJUnit4.class)
public class InstrumentationRegistryTest {

  @Test
  public void argumentsArePopulated() {
    assertThat(InstrumentationRegistry.getArguments()).isNotNull();
  }

  @Test
  public void instrumentationRegistered() {
    assertThat(InstrumentationRegistry.getInstrumentation()).isNotNull();
  }

  @Test
  public void getArgumentsReturnsIndependentCopies() {
    Bundle readArguments = InstrumentationRegistry.getArguments();
    int originalSize = readArguments.size();

    readArguments.putString("mykey", "myvalue");

    assertThat(readArguments.size()).isGreaterThan(originalSize);
    // Subsequent reads should not be affected by the local modifications.
    assertThat(InstrumentationRegistry.getArguments().size()).isEqualTo(originalSize);
  }

  @Test
  public void registerInstanceCopiesArguments() {
    Bundle setArguments = new Bundle();
    setArguments.putString("entry", "val");
    int originalSize = setArguments.size();
    InstrumentationRegistry.registerInstance(
        InstrumentationRegistry.getInstrumentation(), setArguments);
    Bundle readArguments = InstrumentationRegistry.getArguments();
    assertThat(readArguments.size()).isEqualTo(originalSize);

    readArguments.putString("mykey", "myvalue");

    // Subsequent reads should not be affected by the local modifications.
    assertThat(InstrumentationRegistry.getArguments().size()).isEqualTo(originalSize);
  }
}
