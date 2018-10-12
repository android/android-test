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

import android.support.annotation.VisibleForTesting;
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
@VisibleForTesting
public class ClassPathScanner {

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
    private final List<ClassNameFilter> filters = new ArrayList<ClassNameFilter>();

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

    InclusivePackageNamesFilter(Collection<String> pkgNames) {
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

    ExcludePackageNameFilter(String pkgName) {
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

    private Set<String> excludedClassNames;

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
   * Gets the names of all entries contained in given file, that match given filter.
   *
   * @throws IOException
   */
  private void addEntriesFromPath(Set<String> entryNames, String path, ClassNameFilter filter)
      throws IOException {
    DexFile dexFile = null;
    try {
      dexFile = new DexFile(path);
      Enumeration<String> classNames = getDexEntries(dexFile);
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

  /**
   * Retrieves the entry names from given {@link DexFile}.
   *
   * @param dexFile
   * @return {@link Enumeration} of {@link String}s
   */
  @VisibleForTesting
  Enumeration<String> getDexEntries(DexFile dexFile) {
    return dexFile.entries();
  }

  /**
   * Retrieves set of classpath entries that match given {@link ClassNameFilter}.
   *
   * @throws IOException
   */
  public Set<String> getClassPathEntries(ClassNameFilter filter) throws IOException {
    // use LinkedHashSet for predictable order
    Set<String> entryNames = new LinkedHashSet<String>();
    for (String path : classPath) {
      addEntriesFromPath(entryNames, path, filter);
    }
    return entryNames;
  }
}
