/*
 * Copyright (C) 3023 The Android Open Source Project
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
package androidx.test.internal.runner;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Build;
import androidx.test.filters.SdkSuppress;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class SdkSuppressTest {

  public static class SampleSdkSuppress {
    @SdkSuppress(minSdkVersion = 25)
    @Test
    public void min25() {
      fail("min25");
    }

    @SdkSuppress(minSdkVersion = 26)
    @Test
    public void min26() {
      fail("min26");
    }

    @SdkSuppress(minSdkVersion = 27)
    @Test
    public void min27() {
      fail("min27");
    }

    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }

    @SdkSuppress(maxSdkVersion = 29)
    @Test
    public void max29() {
      fail("max29");
    }

    @SdkSuppress(excludedSdks = {26, 27})
    @Test
    public void excludedSdks2627() {
      fail("excludedSdks2627");
    }

    @SdkSuppress(excludedSdks = {29})
    @Test
    public void excludedSdks29() {
      fail("excludedSdks29");
    }

    @SdkSuppress(excludedSdks = {29, 30, 31})
    @Test
    public void excludedSdks293031() {
      fail("excludedSdks293031");
    }

    @SdkSuppress(
        minSdkVersion = 24,
        maxSdkVersion = 30,
        excludedSdks = {26, 27})
    @Test
    public void min24max30excludedSdks2627() {
      fail("min24max30excludedSdks2627");
    }

    @SdkSuppress(minSdkVersion = 27, maxSdkVersion = 29)
    @Test
    public void min27max29() {
      fail("min27max29");
    }

    @SdkSuppress(minSdkVersion = 24, maxSdkVersion = 26)
    @Test
    public void min24max26() {
      fail("min24max26");
    }

    @SdkSuppress(minSdkVersion = 35, codeName = "R")
    @Test
    public void min35CodeNameR() {
      fail("min35CodeNameR");
    }

    @SdkSuppress(minSdkVersion = 36, codeName = "R")
    @Test
    public void min36CodeNameR() {
      fail("min36CodeNameR");
    }
  }

  @SdkSuppress(minSdkVersion = 27)
  public static class SampleSdkSuppressOnClass {
    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }
  }

  @SdkSuppress(minSdkVersion = 27)
  public static class SampleSdkSuppressOnClassAndMethod {
    @Test
    @SdkSuppress(minSdkVersion = 28)
    public void min28() {
      fail("min28");
    }
  }

  @SdkSuppress(minSdkVersion = 27)
  public static class SampleSdkSuppressOnClassAndMethodMaxMin {
    // the method annotation here will completely take precedence over the class level annotation
    @Test
    @SdkSuppress(maxSdkVersion = 28)
    public void maxSdk28() {
      fail("maxSdk28");
    }

    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }
  }

  private static TestRequestBuilder createBuilder() {
    return new TestRequestBuilder();
  }

  /** Test that {@link SdkSuppress} filters tests as appropriate */
  @Test
  @Config(sdk = 26)
  public void testSdkSuppress() throws Exception {
    TestRequestBuilder builder = createBuilder();
    Request request = builder.addTestClass(SampleSdkSuppress.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods = new ArrayList<>();
    for (Failure failure : result.getFailures()) {
      failingMethods.add(failure.getDescription().getMethodName());
    }

    assertThat(failingMethods)
        .containsExactly(
            "min25",
            "min26",
            "noSdkSuppress",
            "max29",
            "min24max26",
            "excludedSdks29",
            "excludedSdks293031");
  }

  /** Test that {@link SdkSuppress} filters tests as appropriate when codeName specified */
  @Test
  @Config(sdk = 35)
  public void testSdkSuppress_codeName() throws Exception {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "CODENAME", "R");
    TestRequestBuilder builder = createBuilder();
    Request request = builder.addTestClass(SampleSdkSuppress.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods = new ArrayList<>();
    for (Failure failure : result.getFailures()) {
      failingMethods.add(failure.getDescription().getMethodName());
    }

    assertThat(failingMethods)
        .containsExactly(
            "min35CodeNameR",
            "min36CodeNameR",
            "noSdkSuppress",
            "min25",
            "min26",
            "min27",
            "excludedSdks2627",
            "excludedSdks29",
            "excludedSdks293031");
  }

  @Test
  @Config(sdk = 26)
  public void testSdkSuppress_classAllFiltered() throws Exception {
    TestRequestBuilder builder = createBuilder();
    Request request = builder.addTestClass(SampleSdkSuppressOnClass.class.getName()).build();
    Result result = new JUnitCore().run(request);

    assertThat(result.getFailureCount()).isEqualTo(0);
  }

  @Test
  @Config(sdk = 27)
  public void testSdkSuppress_classAllNotFiltered() throws Exception {
    TestRequestBuilder builder = createBuilder();
    Request request = builder.addTestClass(SampleSdkSuppressOnClass.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods = new ArrayList<>();
    for (Failure failure : result.getFailures()) {
      failingMethods.add(failure.getDescription().getMethodName());
    }

    assertThat(failingMethods).containsExactly("noSdkSuppress");
  }

  @Test
  @Config(sdk = 27)
  public void testSdkSuppress_classAndMethod() throws Exception {
    TestRequestBuilder builder = createBuilder();
    Request request =
        builder.addTestClass(SampleSdkSuppressOnClassAndMethod.class.getName()).build();
    Result result = new JUnitCore().run(request);

    assertThat(result.getFailureCount()).isEqualTo(0);
  }

  @Test
  @Config(sdk = 26)
  public void testSdkSuppress_classAndMethodMaxMin() throws Exception {
    TestRequestBuilder builder = createBuilder();
    Request request =
        builder.addTestClass(SampleSdkSuppressOnClassAndMethodMaxMin.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods = new ArrayList<>();
    for (Failure failure : result.getFailures()) {
      failingMethods.add(failure.getDescription().getMethodName());
    }

    assertThat(failingMethods).containsExactly("maxSdk28");
  }
}
