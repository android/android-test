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
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/** Unit tests running on local host for TestRequestBuilder. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.ALL_SDKS)
public class TestRequestBuilderLocalTest {

  public static class TestFixture {

    @Test
    @SmallTest
    public void match() {}

    @Test
    public void noMatch() {}
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

    List<String> results = runRequest(builder.build());

    assertThat(results).containsExactly(TestFixture.class.getName() + "#match");
  }

  @Test
  public void setTestsRegExFilter_withClassPathScanning() throws IOException {
    builder.setTestsRegExFilter("TestFixture#match").addPathToScan("foo");
    setClassPathScanningResults(TestFixture.class.getName(), "com.android.SomeOtherClass");

    List<String> results = runRequest(builder.build());

    assertThat(results).containsExactly(TestFixture.class.getName() + "#match");
  }

  @Test
  public void addTestSizeFilter() throws IOException {
    builder.addTestSizeFilter(TestSize.SMALL);
    builder.addTestClass(TestFixture.class.getName());

    List<String> results = runRequest(builder.build());

    assertThat(results).containsExactly(TestFixture.class.getName() + "#match");
  }

  private void setClassPathScanningResults(String... names) throws IOException {
    when(mockClassPathScanner.getClassPathEntries(ArgumentMatchers.any()))
        .thenReturn(new HashSet<>(Arrays.asList(names)));
  }

  /** Runs the test request and gets list of test methods run */
  private static ArrayList<String> runRequest(Request request) {
    JUnitCore testRunner = new JUnitCore();
    RecordingRunListener listener = new RecordingRunListener();
    testRunner.addListener(listener);
    testRunner.run(request);
    return listener.methods;
  }

  /** Records list of test methods executed */
  private static class RecordingRunListener extends RunListener {
    ArrayList<String> methods = new ArrayList<>();

    @Override
    public void testFinished(Description description) {
      methods.add(description.getClassName() + "#" + description.getMethodName());
    }
  }
}
