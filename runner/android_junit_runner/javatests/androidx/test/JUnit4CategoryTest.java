/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Category;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class JUnit4CategoryTest {

  public interface FastTests {
    /* category marker */
  }

  public interface SlowTests {
    /* category marker */
  }

  public static class A {
    @Test
    public void a() {
      fail();
    }

    @Category(SlowTests.class)
    @Test
    public void b() {}
  }

  @Category({SlowTests.class, FastTests.class})
  public static class B {
    @Test
    public void c() {}
  }

  @RunWith(Categories.class)
  @Categories.IncludeCategory(SlowTests.class)
  @Suite.SuiteClasses({A.class, B.class}) // Note that Categories is a kind of Suite
  public static final class SlowTestSuite {
    // Will run A.b and B.c, but not A.a
  }

  @RunWith(Categories.class)
  @Categories.IncludeCategory(SlowTests.class)
  @Categories.ExcludeCategory(FastTests.class)
  @Suite.SuiteClasses({A.class, B.class}) // Note that Categories is a kind of Suite
  public static final class SlowTestNoFastTestSuite {
    // Will run A.b, but not A.a or B.c
  }

  /** This test is mentioned in {@code Categories} and any changes must be reflected. */
  @Test
  public void runSlowTestSuite() {
    // Targeting Test:
    Result testResult = JUnitCore.runClasses(SlowTestSuite.class);

    assertThat("unexpected run count", testResult.getRunCount(), is(equalTo(2)));
    assertThat("unexpected failure count", testResult.getFailureCount(), is(equalTo(0)));
    assertThat("unexpected failure count", testResult.getIgnoreCount(), is(equalTo(0)));
  }

  @Test
  public void runSlowTestNoFastTestSuite() {
    // Targeting Test:
    Result testResult = JUnitCore.runClasses(SlowTestNoFastTestSuite.class);

    assertThat("unexpected run count", testResult.getRunCount(), is(equalTo(1)));
    assertThat("unexpected failure count", testResult.getFailureCount(), is(equalTo(0)));
    assertThat("unexpected failure count", testResult.getIgnoreCount(), is(equalTo(0)));
  }
}
