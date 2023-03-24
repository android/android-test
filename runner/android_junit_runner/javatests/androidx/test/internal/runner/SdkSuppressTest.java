/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.internal.runner.TestRequestBuilder.DeviceBuild;
import java.util.List;
import kotlin.collections.CollectionsKt;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SdkSuppressTest {

  public static class SampleSdkSuppress {
    @SdkSuppress(minSdkVersion = 15)
    @Test
    public void min15() {
      fail("min15");
    }

    @SdkSuppress(minSdkVersion = 16)
    @Test
    public void min16() {
      fail("min16");
    }

    @SdkSuppress(minSdkVersion = 17)
    @Test
    public void min17() {
      fail("min17");
    }

    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }

    @SdkSuppress(maxSdkVersion = 19)
    @Test
    public void max19() {
      fail("max19");
    }

    @SdkSuppress(excludedSdks = {16, 17})
    @Test
    public void excludedSdks1617() {
      fail("excludedSdks1617");
    }

    @SdkSuppress(excludedSdks = {19})
    @Test
    public void excludedSdks19() {
      fail("excludedSdks19");
    }

    @SdkSuppress(excludedSdks = {19, 20, 21})
    @Test
    public void excludedSdks192021() {
      fail("excludedSdks192021");
    }

    @SdkSuppress(
        minSdkVersion = 14,
        maxSdkVersion = 20,
        excludedSdks = {16, 17})
    @Test
    public void min14max20excludedSdks1617() {
      fail("min14max20excludedSdks1617");
    }

    @SdkSuppress(minSdkVersion = 17, maxSdkVersion = 19)
    @Test
    public void min17max19() {
      fail("min17max19");
    }

    @SdkSuppress(minSdkVersion = 14, maxSdkVersion = 16)
    @Test
    public void min14max16() {
      fail("min14max16");
    }

    @SdkSuppress(minSdkVersion = 29, codeName = "R")
    @Test
    public void min29CodeNameR() {
      fail("min29CodeNameR");
    }

    @SdkSuppress(minSdkVersion = 20, codeName = "R")
    @Test
    public void min20CodeNameR() {
      fail("min20CodeNameR");
    }
  }

  @SdkSuppress(minSdkVersion = 17)
  public static class SampleSdkSuppressOnClass {
    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }
  }

  @SdkSuppress(minSdkVersion = 17)
  public static class SampleSdkSuppressOnClassAndMethod {
    @Test
    @SdkSuppress(minSdkVersion = 18)
    public void min18() {
      fail("min18");
    }
  }

  @SdkSuppress(minSdkVersion = 17)
  public static class SampleSdkSuppressOnClassAndMethodMaxMin {
    // the method annotation here will completely take precedence over the class level annotation
    @Test
    @SdkSuppress(maxSdkVersion = 18)
    public void maxSdk18() {
      fail("maxSdk18");
    }

    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }
  }

  private static class FakeDeviceBuild implements DeviceBuild {

    private final int sdkVersion;
    private final String codeName;

    FakeDeviceBuild(int sdkVersion) {
      this(sdkVersion, "REL");
    }

    FakeDeviceBuild(int sdkVersion, String codeName) {
      this.sdkVersion = sdkVersion;
      this.codeName = codeName;
    }

    @Override
    public int getSdkVersionInt() {
      return sdkVersion;
    }

    @Override
    public String getHardware() {
      return "goldfish";
    }

    @Override
    public String getCodeName() {
      return codeName;
    }
  }

  private static TestRequestBuilder createBuilder(DeviceBuild deviceBuild) {
    return new TestRequestBuilder(deviceBuild, getInstrumentation(), getArguments());
  }

  /** Test that {@link SdkSuppress} filters tests as appropriate */
  @Test
  public void testSdkSuppress() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(16));
    Request request = builder.addTestClass(SampleSdkSuppress.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods =
        CollectionsKt.map(
            result.getFailures(), failure -> failure.getDescription().getMethodName());

    assertThat(failingMethods)
        .containsExactly(
            "min15",
            "min16",
            "noSdkSuppress",
            "max19",
            "min14max16",
            "excludedSdks19",
            "excludedSdks192021");
  }

  /** Test that {@link SdkSuppress} filters tests as appropriate when codeName specified */
  @Test
  public void testSdkSuppress_codeName() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(29, "R"));
    Request request = builder.addTestClass(SampleSdkSuppress.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods =
        CollectionsKt.map(
            result.getFailures(), failure -> failure.getDescription().getMethodName());

    assertThat(failingMethods)
        .containsExactly(
            "min29CodeNameR",
            "min20CodeNameR",
            "noSdkSuppress",
            "min15",
            "min16",
            "min17",
            "excludedSdks1617",
            "excludedSdks19",
            "excludedSdks192021");
  }

  @Test
  public void testSdkSuppress_classAllFiltered() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(16));
    Request request = builder.addTestClass(SampleSdkSuppressOnClass.class.getName()).build();
    Result result = new JUnitCore().run(request);

    assertThat(result.getFailureCount()).isEqualTo(0);
  }

  @Test
  public void testSdkSuppress_classAllNotFiltered() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(17));
    Request request = builder.addTestClass(SampleSdkSuppressOnClass.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods =
        CollectionsKt.map(
            result.getFailures(), failure -> failure.getDescription().getMethodName());

    assertThat(failingMethods).containsExactly("noSdkSuppress");
  }

  @Test
  public void testSdkSuppress_classAndMethod() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(17));
    Request request =
        builder.addTestClass(SampleSdkSuppressOnClassAndMethod.class.getName()).build();
    Result result = new JUnitCore().run(request);

    assertThat(result.getFailureCount()).isEqualTo(0);
  }

  @Test
  public void testSdkSuppress_classAndMethodMaxMin() throws Exception {
    TestRequestBuilder builder = createBuilder(new FakeDeviceBuild(16));
    Request request =
        builder.addTestClass(SampleSdkSuppressOnClassAndMethodMaxMin.class.getName()).build();
    Result result = new JUnitCore().run(request);

    List<String> failingMethods =
        CollectionsKt.map(
            result.getFailures(), failure -> failure.getDescription().getMethodName());

    assertThat(failingMethods).containsExactly("maxSdk18");
  }
}
