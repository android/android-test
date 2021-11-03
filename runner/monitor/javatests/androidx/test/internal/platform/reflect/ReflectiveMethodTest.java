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

/** Unit tests for {@link ReflectiveMethod}. */
@RunWith(AndroidJUnit4.class)
public class ReflectiveMethodTest {

  private static class Fixture {
    private int someMethod() {
      return 42;
    }

    private static int someStaticMethod() {
      return 43;
    }

    private int someMethod(int i) {
      return i + 42;
    }

    private static int someStaticMethod(int i) {
      return i + 43;
    }
  }

  @Test
  public void invoke() throws ReflectionException {
    Fixture f = new Fixture();
    int value = new ReflectiveMethod<Integer>(f.getClass().getName(), "someMethod").invoke(f);
    assertThat(value).isEqualTo(42);
  }

  @Test
  public void invokeStatic() throws ReflectionException {
    int value =
        new ReflectiveMethod<Integer>(Fixture.class.getName(), "someStaticMethod").invokeStatic();
    assertThat(value).isEqualTo(43);
  }

  @Test
  public void invoke_params() throws ReflectionException {
    Fixture f = new Fixture();
    int value =
        new ReflectiveMethod<Integer>(f.getClass().getName(), "someMethod", int.class).invoke(f, 5);
    assertThat(value).isEqualTo(47);
  }

  @Test
  public void invokeStatic_params() throws ReflectionException {
    int value =
        new ReflectiveMethod<Integer>(Fixture.class.getName(), "someStaticMethod", int.class)
            .invokeStatic(5);
    assertThat(value).isEqualTo(48);
  }

  @Test
  public void invokeStatic_nonExistent() {
    assertThrows(
        ReflectionException.class,
        () ->
            new ReflectiveMethod<Integer>(
                    Fixture.class.getName(), "someMethod", int.class, boolean.class)
                .invokeStatic());
  }
}
