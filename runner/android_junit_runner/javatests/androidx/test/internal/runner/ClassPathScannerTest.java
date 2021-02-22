/*
 * Copyright (C) 2012 The Android Open Source Project
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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.ClassPathScanner.AcceptAllFilter;
import java.io.IOException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link ClassPathScanner}.
 *
 * <p>is the instrumentation target, and is a multidex binary. Its critical this test is executed
 * across APIs <=20 and newer APIs.
 */
@RunWith(AndroidJUnit4.class)
public class ClassPathScannerTest {

  private ClassPathScanner classPathScanner;

  @Before
  public void setUp() throws Exception {
    // scan the target aka app under test
    classPathScanner =
        new ClassPathScanner(ClassPathScanner.getDefaultClasspaths(getInstrumentation()));
  }

  /** Verify that all classes are scanned in a multidex apk */
  @Test
  public void multidex() throws IOException {
    Set<String> result = classPathScanner.getClassPathEntries(new AcceptAllFilter());
    assertThat(result)
        .containsAtLeast(
            "androidx.test.multidex.app.MultiDexTestClassA",
            "androidx.test.multidex.app.MultiDexTestClassB",
            "androidx.test.multidex.app.MultiDexTestClassC",
            "androidx.test.multidex.app.MultiDexTestClassD",
            "androidx.test.multidex.app.MultiDexApplication");

    // ensure classes from binary under test are not included
    // this relies on build adding "androidx.test.testing.fixtures.CustomTestFilter" to target app
    // only
    assertThat(result).doesNotContain("androidx.test.testing.fixtures.CustomTestFilter");
  }
}
