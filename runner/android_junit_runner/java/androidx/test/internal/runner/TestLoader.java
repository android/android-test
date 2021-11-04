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

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * An abstract class for loading JUnit3 and JUnit4 test classes given a set of potential class
 * names.
 */
public abstract class TestLoader {

  /** Factory for {@link TestLoader} */
  public static class Factory {

    private Factory() {}

    public static TestLoader create(
        @Nullable ClassLoader classLoader, RunnerBuilder runnerBuilder, boolean scanningPath) {

      if (classLoader == null) {
        classLoader = TestLoader.class.getClassLoader();
      }

      if (scanningPath) {
        return new ScanningTestLoader(classLoader, runnerBuilder);
      } else {
        return new DirectTestLoader(classLoader, runnerBuilder);
      }
    }
  }

  protected abstract Runner doCreateRunner(String className);

  /**
   * Get the {@link Collection) of {@link Runner runners}.
   */
  public List<Runner> getRunnersFor(Collection<String> classNames) {
    final Map<String, Runner> runnersMap = new LinkedHashMap<>();

    for (String className : classNames) {
      if (!runnersMap.containsKey(className)) {
        Runner runner = doCreateRunner(className);
        if (runner != null) {
          runnersMap.put(className, runner);
        }
      }
    }
    return new ArrayList<>(runnersMap.values());
  }
}
