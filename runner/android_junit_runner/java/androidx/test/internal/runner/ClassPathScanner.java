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

import android.app.Instrumentation;
import dalvik.system.DexFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds class entries in provided paths to scan.
 *
 * <p>Adapted from tools/tradefederation/..ClassPathScanner
 */
public class ClassPathScanner {

  private static final String TAG = "ClassPathScanner";

  // Default excluded test packages
  private static final String[] DEFAULT_EXCLUDED_PACKAGES = {
    "junit",
    "org.junit",
    "org.hamcrest",
    "org.mockito", // exclude Mockito for performance and to prevent JVM related errors
    "androidx.test.internal.runner.junit3", // always skip AndroidTestSuite
    "androidx.test.runner.suites", // always skip AndroidClassPathSuite to avoid infinite classpath
    // scanning loop
    "org.jacoco", // exclude Jacoco to prevent class loading issues
    "net.bytebuddy" // exclude byte buddy to prevent Mockito 2.0 class loading issues
  };

  /**
   * A filter for classpath entry paths
   *
   * <p>Patterned after {@link java.io.FileFilter}
   */
  public interface ClassNameFilter {
    /**
     * Tests whether or not the specified abstract pathname should be included in a class path entry
     * list.
     *
     * @param className the relative path of the class path entry
     */
    boolean accept(String className);
  }

  /** A {@link ClassNameFilter} that accepts all class names. */
  public static class AcceptAllFilter implements ClassNameFilter {

    /** {@inheritDoc} */
    @Override
    public boolean accept(String className) {
      return true;
    }
  }

  /** A {@link ClassNameFilter} that chains one or more filters together */
  public static class ChainedClassNameFilter implements ClassNameFilter {
    private final List<ClassNameFilter> filters = new ArrayList<>();

    public void add(ClassNameFilter filter) {
      filters.add(filter);
    }

    public void addAll(ClassNameFilter... filters) {
      this.filters.addAll(Arrays.asList(filters));
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(String className) {
      for (ClassNameFilter filter : filters) {
        if (!filter.accept(className)) {
          return false;
        }
      }
      return true;
    }
  }

  /** A {@link ClassNameFilter} that rejects inner classes. */
  public static class ExternalClassNameFilter implements ClassNameFilter {
    /** {@inheritDoc} */
    @Override
    public boolean accept(String pathName) {
      return !pathName.contains("$");
    }
  }

  /** A {@link ClassNameFilter} that only accepts package names within the given namespaces. */
  public static class InclusivePackageNamesFilter implements ClassNameFilter {

    private final Collection<String> pkgNames;

    public InclusivePackageNamesFilter(Collection<String> pkgNames) {
      this.pkgNames = new ArrayList<>(pkgNames.size());
      for (String packageName : pkgNames) {
        if (!packageName.endsWith(".")) {
          this.pkgNames.add(String.format("%s.", packageName));
        } else {
          this.pkgNames.add(packageName);
        }
      }
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(String pathName) {
      for (String packageName : pkgNames) {
        if (pathName.startsWith(packageName)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * A {@link ClassNameFilter} that only rejects a given package names within the given namespace.
   */
  public static class ExcludePackageNameFilter implements ClassNameFilter {

    private final String pkgName;

    public ExcludePackageNameFilter(String pkgName) {
      if (!pkgName.endsWith(".")) {
        this.pkgName = String.format("%s.", pkgName);
      } else {
        this.pkgName = pkgName;
      }
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(String pathName) {
      return !pathName.startsWith(pkgName);
    }
  }

  static class ExcludeClassNamesFilter implements ClassNameFilter {

    private final Set<String> excludedClassNames;

    public ExcludeClassNamesFilter(Set<String> excludedClassNames) {
      this.excludedClassNames = excludedClassNames;
    }

    @Override
    public boolean accept(String className) {
      return !excludedClassNames.contains(className);
    }
  }

  private final Set<String> classPath = new HashSet<>();

  /**
   * Constructs a new instance of a {@link ClassPathScanner}.
   *
   * @param paths filepaths that should be scanned (.apk and .dex files)
   */
  public ClassPathScanner(String... paths) {
    this(Arrays.asList(paths));
  }

  /**
   * Constructs a new instance of a {@link ClassPathScanner}.
   *
   * @param paths a list of paths that should be scanned (.apk and .dex files)
   */
  public ClassPathScanner(Collection<String> paths) {
    classPath.addAll(paths);
  }

  /**
   * Build the default classpaths to scan for the Instrumentation.
   *
   * <p>This will only scan for tests in the current Apk aka testContext. Note that this represents
   * a change from InstrumentationTestRunner where getTargetContext().getPackageCodePath() aka app
   * under test was also scanned.
   */
  public static Collection<String> getDefaultClasspaths(Instrumentation instrumentation) {
    Collection<String> classPaths = new ArrayList<>();
    // add the test apk to claspath
    classPaths.add(instrumentation.getContext().getPackageCodePath());
    return classPaths;
  }

  /**
   * Gets the names of all entries contained in given file, that match given filter.
   *
   * @throws IOException
   */
  private void addEntriesFromPath(Set<String> entryNames, String path, ClassNameFilter filter)
      throws IOException {
    DexFile dexFile = null;
    try {
      try {
        dexFile = new DexFile(path);
      } catch (IOException ioe) {
        if (path.endsWith(".zip")) {
          dexFile = DexFile.loadDex(path, path.substring(0, path.length() - 3) + "dex", 0);
        } else {
          throw ioe;
        }
      }
      Enumeration<String> classNames = dexFile.entries();
      while (classNames.hasMoreElements()) {
        String className = classNames.nextElement();
        if (filter.accept(className)) {
          entryNames.add(className);
        }
      }
    } finally {
      if (dexFile != null) {
        dexFile.close();
      }
    }
  }

  public static List<String> getDefaultExcludedPackages() {
    return Arrays.asList(DEFAULT_EXCLUDED_PACKAGES);
  }

  /**
   * Retrieves set of classpath entries, excluding a set of default packages and inner classes.
   *
   * @throws IOException if failed to read classes from classpath
   */
  public Set<String> getClassPathEntries() throws IOException {
    ChainedClassNameFilter filter = new ChainedClassNameFilter();
    for (String pkg : DEFAULT_EXCLUDED_PACKAGES) {
      filter.add(new ExcludePackageNameFilter(pkg));
    }
    filter.add(new ExternalClassNameFilter());
    return getClassPathEntries(filter);
  }

  /**
   * Retrieves set of classpath entries that match given {@link ClassNameFilter}.
   *
   * @throws IOException if failed to read classes from classpath
   */
  public Set<String> getClassPathEntries(ClassNameFilter filter) throws IOException {
    // use LinkedHashSet for predictable order
    Set<String> entryNames = new LinkedHashSet<>();
    for (String path : classPath) {
      addEntriesFromPath(entryNames, path, filter);
    }
    return entryNames;
  }
}
