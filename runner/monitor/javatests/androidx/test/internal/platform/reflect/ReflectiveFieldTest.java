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

package androidx.test.internal.platform.reflect;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ReflectiveField}. */
@RunWith(AndroidJUnit4.class)
public class ReflectiveFieldTest {

  private static class Fixture {
    private final int someField = 42;
  }

  @Test
  public void get() throws ReflectionException {
    Fixture f = new Fixture();
    int value = new ReflectiveField<Integer>(f.getClass().getName(), "someField").get(f);
    assertThat(value).isEqualTo(42);
  }

  @Test
  public void get_nonExistent() {
    assertThrows(
        ReflectionException.class,
        () ->
            new ReflectiveField<Integer>(Fixture.class.getName(), "someMissingField")
                .get(new Fixture()));
  }
}
