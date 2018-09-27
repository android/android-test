/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DexdumpPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.IgnoreTestPackages;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PackageName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SystemApksToInstall;
import com.google.android.apps.common.testing.broker.shell.Command;
import com.google.android.apps.common.testing.broker.shell.CommandException;
import com.google.android.apps.common.testing.broker.shell.CommandResult;
import com.google.android.apps.common.testing.proto.TestInfo.TestSuitePb;
import com.google.android.apps.common.testing.suite.dex.DumpUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.android.apps.common.testing.suite.dex.DumpUtils.Pair;
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.inject.Inject;

// For Copybara OSS
import java.util.Iterator;
import com.google.common.collect.Iterators;
// */

/** Tracks test suites contained in apks. */
public class TestInfoRepository {
  private static final int BUFF_SIZE = 1024 * 12; // 0.5 mb

  private final Map<String, String> packageNameToApkPath = Maps.newConcurrentMap();
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final LoadingCache<String, String> apkPathToPackageNameCache;
  private final Map<String, ImmutableSet<String>> targetToClasses = Maps.newConcurrentMap();
  private final List<String> initialApks;
  private final List<String> systemApks;
  private final String dexdumpPath;
  private final List<String> ignoreTestPackages;
  private final Environment environment;

  private static final Logger logger = Logger.getLogger(TestInfoRepository.class.getName());

  @Inject
  public TestInfoRepository(
      @PackageName LoadingCache<String, String> apkPathToPackageNameCache,
      @ApksToInstall List<String> initialApks,
      @DexdumpPath String dexdumpPath,
      @SystemApksToInstall List<String> systemApks,
      @IgnoreTestPackages List<String> ignoreTestPackages,
      Environment environment) {
    this.apkPathToPackageNameCache = apkPathToPackageNameCache;
    this.initialApks = initialApks;
    this.dexdumpPath = dexdumpPath;
    this.systemApks = systemApks;
    this.ignoreTestPackages = ignoreTestPackages;
    this.environment = environment;
  }

  /**
   * Returns a proto holding information about tests found in the given instrumentation. Apks with
   * multiple dex files are supported.
   */
  public TestSuitePb listTests(
      Instrumentation instrumentation, List<String> additionalTestPackages) {
    Set<String> apksToScan = new HashSet<>();
    if (!ignoreTestPackages.contains(instrumentation.getAndroidPackage())) {
      String testApkPath = getApkPathForPackage(instrumentation.getAndroidPackage());
      apksToScan.add(testApkPath);
    }
    for (String testPackage : additionalTestPackages) {
      if (!ignoreTestPackages.contains(testPackage)) {
        String apkPath = getApkPathForPackage(testPackage);
        apksToScan.add(apkPath);
      }
    }

    List<InputStream> dexDumpFiles = Lists.newArrayList();
    // The test apk may have multiple dex files, so give it the same multi-dex treatment as the
    // target app.
    for (String apkPath : apksToScan) {
      dexDumpFiles.addAll(getDexDumpFilesFromApk(apkPath));
    }

    Pair<TestSuitePb, ImmutableSet<String>> dexDump =
        DumpUtils.parseDexDump(dexDumpFiles.toArray(new InputStream[0]));
    targetToClasses.put(instrumentation.getTargetPackage(), dexDump.getSecond());
    TestSuitePb result = dexDump.getFirst();
    logger.info(
        String.format(
            "Found %d test(s). Scanned %d dex file(s) in %d APK(s)",
            result.getInfoCount(), dexDumpFiles.size(), apksToScan.size()));
    return result;
  }

  public TestSuitePb listTestsInFiles(Instrumentation instrumentation, List<File> dexFiles) {
    try {
      List<InputStream> dexDumpFiles = Lists.newArrayList();
      for (File dex : dexFiles) {
        dexDumpFiles.add(runDexDump(dex.getCanonicalPath()));
      }

      Pair<TestSuitePb, ImmutableSet<String>> dexDump =
          DumpUtils.parseDexDump(dexDumpFiles.toArray(new InputStream[dexDumpFiles.size()]));
      targetToClasses.put(instrumentation.getTargetPackage(), dexDump.getSecond());

      return dexDump.getFirst();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<InputStream> getDexDumpFilesFromApk(String apkPath) {
    List<InputStream> dexDumpFiles = Lists.newArrayList();
    try {
      logger.info("Scanning: " + apkPath);
      ZipFile apk = new ZipFile(new File(apkPath));
      FluentIterable<ZipEntry> dexEntries = listDexFilesFrom(apk);
      checkState(
          dexEntries.size() > 0,
          "%s: Apk is expected to have at least one dex file! APKs without dex files can be built"
              + " but cannot be installed. Often this is an interaction between the"
              + " binary_under_test attribute containing all classes that are also in the srcs"
              + " and deps attributes of the android_test target itself.",
          apkPath);
      // As of Apr 2015, dexdump tool extracts info only from the first dex file (classes.dex) of
      // an apk. If the apk contains only one dex, running dexdump on the apk is good enough.
      // Otherwise, we run dexdump on each of the dex files.
      if (dexEntries.size() == 1) {
        dexDumpFiles.add(runDexDump(apkPath));
      } else {
        for (String dex : getDexesFrom(apk, dexEntries)) {
          dexDumpFiles.add(runDexDump(dex));
        }
      }
      return dexDumpFiles;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  List<String> getDexesFrom(final ZipFile apk, FluentIterable<ZipEntry> dexFiles)
      throws IOException {
    return dexFiles
        .transform(
            new Function<ZipEntry, String>() {
              @Override
              public String apply(ZipEntry entry) {
                try {
                  File dexFile = environment.createTempFile("target", entry.getName());
                  Files.copy(
                      apk.getInputStream(entry),
                      dexFile.toPath(),
                      StandardCopyOption.REPLACE_EXISTING);
                  return dexFile.getPath();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }
            })
        .toList();
  }

  private static FluentIterable<ZipEntry> entries(final ZipFile file) {
    checkNotNull(file);
    return new FluentIterable<ZipEntry>() {
      @Override
      public Iterator<ZipEntry> iterator() {
        return (Iterator<ZipEntry>) Iterators.forEnumeration(file.entries());
      }
    };
  }
  // */

  @VisibleForTesting
  static FluentIterable<ZipEntry> listDexFilesFrom(ZipFile apk) {
    return entries(apk)
        .filter(
            new Predicate<ZipEntry>() {
              @Override
              public boolean apply(ZipEntry input) {
                return input.getName().startsWith("classes") && input.getName().endsWith(".dex");
              }
            });
  }

  /**
   * Indicates whether or not a given android package is present in the test repo.
   *
   * @param androidPackage the package name from the AndroidManifest.xml file
   */
  public boolean containsInfoForPackage(String androidPackage) {
    maybeInitializeRepo();
    return packageNameToApkPath.containsKey(androidPackage);
  }

  /**
   * Returns the apk path for the given android package.
   */
  public String getApkPathForPackage(String androidPackage) {
    checkState(containsInfoForPackage(androidPackage), "Unknown package: %s", androidPackage);
    return packageNameToApkPath.get(androidPackage);
  }

  /**
   * Returns {@code true} if the instrumentation (may include both the app under test as well as the
   * test apk) with the given package contains the given class name.
   */
  public boolean targetContainsClass(String androidPackage, String fullClassName) {
    checkState(containsInfoForPackage(androidPackage), "Unknown package: %s", androidPackage);
    ImmutableSet<String> allClasses = targetToClasses.get(androidPackage);
    return allClasses.contains(fullClassName);
  }

  /**
   * Adds an apk to the repository of test information.
   *
   * Note: this method is cheap, it doesnt actually scan the apk for tests when invoked, it just
   * extracts the android package name from the AndroidManifest.xml
   */
  @VisibleForTesting
  void addApkToRepository(File apk) {
    checkNotNull(apk);
    packageNameToApkPath.put(apkPathToPackageNameCache.getUnchecked(apk.getPath()), apk.getPath());
  }

  private synchronized void maybeInitializeRepo() {
    if (!initialized.get()) {
      for (String apk : initialApks) {
        addApkToRepository(new File(apk));
      }
      for (String apk : systemApks) {
        addApkToRepository(new File(apk));
      }
      initialized.set(true);
    }
  }

  private String [] getDumpArgs(String targetFile) {
    String [] args = {dexdumpPath, "-a", "cms", "-l", "xml_private", targetFile};
    return args;
  }

  private InputStream runDexDump(String targetFile) {
    try {
      File dumpOutput = environment.createTempFile("dex-dump", ".xml");

      CommandResult dumpCommand = new Command(getDumpArgs(targetFile))
          .execute(
              new ByteArrayInputStream(Command.NO_INPUT),
              Command.NO_OBSERVER,
              new BufferedOutputStream(new FileOutputStream(dumpOutput), BUFF_SIZE),
              new FileOutputStream("/dev/null"));
      checkState(dumpCommand.getTerminationStatus().success(), "dump apk failed");
      return new BufferedInputStream(new FileInputStream(dumpOutput), BUFF_SIZE);
    } catch (IOException|CommandException e) {
      throw new RuntimeException(e);
    }
  }
}
