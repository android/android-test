/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.internal.runner.filters;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestsRegExFilterTest {

  /** a dummy class to build up test descriptions */
  public static class TestFixture {}

  @Test
  public void emptyPasses() {
    TestsRegExFilter filter = new TestsRegExFilter();
    Description d = Description.createTestDescription(TestFixture.class, "any");
    ;
    assertThat(filter.evaluateTest(d)).isTrue();
  }

  @Test
  public void partialClassName() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("TestFixture");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isTrue();
  }

  @Test
  public void partialClassName_noMatch() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("NotTheClassImLookingFor");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isFalse();
  }

  @Test
  public void packageMatch() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("androidx.test.internal.runner.filters");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isTrue();
  }

  @Test
  public void package_noMatch() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("androidx.test.internal.runner.filters.notit");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isFalse();
  }

  @Test
  public void methodMatch() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("TestFixture#any");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isTrue();

    Description notit = Description.createTestDescription(TestFixture.class, "excluded");

    assertThat(filter.evaluateTest(notit)).isFalse();
  }

  @Test
  public void methodOrMatch() {
    TestsRegExFilter filter = new TestsRegExFilter();
    filter.setPattern("TestFixture#any|TestFixture#excluded");
    Description d = Description.createTestDescription(TestFixture.class, "any");

    assertThat(filter.evaluateTest(d)).isTrue();

    Description notit = Description.createTestDescription(TestFixture.class, "excluded");

    assertThat(filter.evaluateTest(notit)).isTrue();
  }
}
