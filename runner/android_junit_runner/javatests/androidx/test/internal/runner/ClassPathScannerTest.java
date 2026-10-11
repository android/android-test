/*
 * Copyright (C) 2026 The Android Open Source Project
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

import static androidx.test.internal.runner.ClassPathScanner.getDefaultClasspaths;
import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * Verifies that {@link DexBytecodeScanner} pre-filtering discovers the exact same JUnit3 and JUnit4
 * test classes as legacy {@link dalvik.system.DexFile} scanning while skipping non-test classes.
 */
@RunWith(AndroidJUnit4.class)
public class ClassPathScannerTest {

  @Test
  public void getClassPathEntries_preFilterMatchesLegacyDiscoveredTestsAndSkipsNonTests()
      throws IOException {
    if (Boolean.parseBoolean(getArguments().getString("expectObfuscatedJUnit"))) {
      assertThat(Test.class.getName()).isNotEqualTo("org.junit.Test");
    }
    Collection<String> apkPaths = getDefaultClasspaths(getInstrumentation());

    Set<String> legacyCandidates =
        new ClassPathScanner(apkPaths, /* useDexBytecodePreFilter= */ false).getClassPathEntries();
    Set<String> preFilteredCandidates =
        new ClassPathScanner(apkPaths, /* useDexBytecodePreFilter= */ true).getClassPathEntries();

    Set<String> preFilteredTests = discoverTests(preFilteredCandidates);
    assertThat(preFilteredTests).isNotEmpty();
    assertThat(preFilteredTests).isEqualTo(discoverTests(legacyCandidates));
    assertThat(preFilteredCandidates.size()).isLessThan(legacyCandidates.size() / 5);
  }

  /** Returns the display names of the runners JUnit builds for {@code classNames}. */
  private static Set<String> discoverTests(Collection<String> classNames) {
    RunnerBuilder logOnlyBuilder =
        new AndroidLogOnlyBuilder(/* ignoreSuiteMethods= */ false, ImmutableList.of());
    TestLoader loader =
        TestLoader.Factory.create(
            /* classLoader= */ null, logOnlyBuilder, /* scanningPath= */ true);
    Set<String> displayNames = new LinkedHashSet<>();
    for (Runner runner : loader.getRunnersFor(classNames)) {
      displayNames.add(runner.getDescription().getDisplayName());
    }
    return displayNames;
  }
}
