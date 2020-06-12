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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.ClassPathScanner.InclusivePackageNamesFilter;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ClassPathScanner}. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ClassPathScannerTest {

  private ClassPathScanner classPathScanner;

  @Before
  public void setUp() throws Exception {
    classPathScanner = new ClassPathScanner(getClassPathToScan());
  }

  private static String[] getClassPathToScan() {
    String classpathArg =
        InstrumentationRegistry.getArguments().getString(RunnerArgs.ARGUMENT_CLASSPATH_TO_SCAN);
    if (classpathArg != null) {
      return classpathArg.split(File.pathSeparator);
    } else {
      return new String[] {getApplicationContext().getPackageCodePath()};
    }
  }

  @Test
  public void inclusivePackageNamesFilter() throws IOException {
    Set<String> result =
        classPathScanner.getClassPathEntries(
            new InclusivePackageNamesFilter(Arrays.asList("androidx.test.annotation")));
    assertThat(result).hasSize(1);
    assertThat(result).contains("androidx.test.annotation.Beta");
  }
}
