/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.internal.runner;

import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests running on local host for TestRequestBuilder. */
@RunWith(AndroidJUnit4.class)
public class TestRequestBuilderLocalTest {

  public static class TestFixture {

    @Test
    @SmallTest
    public void match() {}

    @Test
    public void noMatch() {}
  }

  public static class TestFixtureIgnored {
    @Ignore
    @Test
    public void ignored() {}

    @Test
    public void notIgnored() {}
  }

  @Mock private ClassPathScanner mockClassPathScanner;

  private TestRequestBuilder builder;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    builder =
        new TestRequestBuilder(getInstrumentation(), getArguments()) {
          @Override
          ClassPathScanner createClassPathScanner(List<String> paths) {
            return mockClassPathScanner;
          }
        };
  }

  @Test
  public void setTestsRegExFilter_withTestClass() {
    builder.setTestsRegExFilter("TestFixture#match");
    builder.addTestClass(TestFixture.class.getName());

    RecordingRunListener results = runRequest(builder.build());

    assertThat(results.testFinishedMethods).containsExactly(TestFixture.class.getName() + "#match");
    assertThat(results.testIgnoredMethods).hasSize(0);
    assertThat(results.runStartedMethods).containsExactlyElementsIn(results.testFinishedMethods);
  }

  @Test
  public void setTestsRegExFilter_withClassPathScanning() throws IOException {
    builder.setTestsRegExFilter("TestFixture#match").addPathToScan("foo");
    setClassPathScanningResults(TestFixture.class.getName(), "com.android.SomeOtherClass");

    RecordingRunListener results = runRequest(builder.build());

    assertThat(results.testFinishedMethods).containsExactly(TestFixture.class.getName() + "#match");
    assertThat(results.testIgnoredMethods).hasSize(0);
    assertThat(results.runStartedMethods).containsExactlyElementsIn(results.testFinishedMethods);
  }

  @Test
  public void addTestSizeFilter() throws IOException {
    builder.addTestSizeFilter(TestSize.SMALL);
    builder.addTestClass(TestFixture.class.getName());

    RecordingRunListener results = runRequest(builder.build());

    assertThat(results.testFinishedMethods).containsExactly(TestFixture.class.getName() + "#match");
    assertThat(results.testIgnoredMethods).hasSize(0);
    assertThat(results.runStartedMethods).containsExactlyElementsIn(results.testFinishedMethods);
  }

  @Test
  public void ignored() throws IOException {
    builder.addTestClass(TestFixtureIgnored.class.getName());

    RecordingRunListener results = runRequest(builder.build());

    assertThat(results.testFinishedMethods)
        .containsExactly(TestFixtureIgnored.class.getName() + "#notIgnored");
    assertThat(results.testIgnoredMethods)
        .containsExactly(TestFixtureIgnored.class.getName() + "#ignored");
    assertThat(results.runStartedMethods)
        .containsExactly(
            TestFixtureIgnored.class.getName() + "#ignored",
            TestFixtureIgnored.class.getName() + "#notIgnored");
  }

  private void setClassPathScanningResults(String... names) throws IOException {
    when(mockClassPathScanner.getClassPathEntries(ArgumentMatchers.any()))
        .thenReturn(new HashSet<>(Arrays.asList(names)));
  }

  /** Runs the test request and gets list of test methods run */
  private static RecordingRunListener runRequest(Request request) {
    JUnitCore testRunner = new JUnitCore();
    RecordingRunListener listener = new RecordingRunListener();
    testRunner.addListener(listener);
    testRunner.run(request);
    return listener;
  }

  /** Records list of test methods executed */
  private static class RecordingRunListener extends RunListener {
    List<String> runStartedMethods = new ArrayList<>();
    List<String> testStartedMethods = new ArrayList<>();
    List<String> testFinishedMethods = new ArrayList<>();
    List<String> testIgnoredMethods = new ArrayList<>();

    @Override
    public void testRunStarted(Description description) {
      addMethodsFromDescription(runStartedMethods, description);
    }

    @Override
    public void testStarted(Description description) {
      addMethodsFromDescription(testStartedMethods, description);
    }

    @Override
    public void testFinished(Description description) {
      addMethodsFromDescription(testFinishedMethods, description);
    }

    @Override
    public void testIgnored(Description description) {
      addMethodsFromDescription(testIgnoredMethods, description);
    }

    private static void addMethodsFromDescription(
        List<String> methodList, Description description) {
      if (description.isTest()) {
        methodList.add(description.getClassName() + "#" + description.getMethodName());
      }
      for (Description child : description.getChildren()) {
        addMethodsFromDescription(methodList, child);
      }
    }

    // @Override
    // public void testFinished(Description description) {
    //  methods.add(description.getClassName() + "#" + description.getMethodName());
    // }

  }
}
