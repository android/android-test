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
import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AaptPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AaptPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AaptResourceName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbEnvironment;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DataDir;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceControllerPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DexdumpPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DexdumpPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ExecutorLocation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.GeneratedLauncherScript;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LauncherScriptFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LogcatPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.OdoApkLocation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.OdoApkResourceName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PackageName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PrecompiledApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ResourceAaptPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ResourceDexdumpName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ResourceDexdumpPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SelectedBroker;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ServerMappings;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ServicesApkLocation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SimAccessRulesFile;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SimAccessRulesFileFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SpongeOutputDirectory;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SubprocessExecution;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SubprocessLogDir;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SystemApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SystemApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestServicesApkLocation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestServicesApkResourceName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestServicesApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestTempDir;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.UseWaterfall;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Providers;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * The internal DeviceBroker guice module.
 *
 * <p>The proper way to get a device broker is through {@link DeviceBrokerFactory#getInstance()}
 *
 */
class DeviceBrokerModule extends AbstractModule {

  private static final Logger logging = Logger.getLogger(DeviceBrokerModule.class.getName());
  private static final Map<String, String> SYSTEM_ENV = System.getenv();

  private static final String ENVIRONMENT_KEY_ANDROID_ADB = "ANDROID_ADB";
  private static final String CORP_ADB_PATH = "/usr/bin/adb";
  private final ImmutableMap<String, HostAndPort> serverMappings;

  DeviceBrokerModule(ImmutableMap<String, HostAndPort> serverMappings) {
    this.serverMappings = checkNotNull(serverMappings);
  }

  DeviceBrokerModule() {
    this(ImmutableMap.<String, HostAndPort>of());
  }

  @Provides
  @ServerMappings
  public ImmutableMap<String, HostAndPort> provideServerMappings() {
    // TODO: the proxy port is inside of here - use it.
    return serverMappings;
  }

  @Provides
  @Singleton
  @PackageName
  public LoadingCache<String, String> providePackageNameCache(final AaptUtil aaptUtil) {
    return CacheBuilder.newBuilder()
        .build(new CacheLoader<String, String>() {
          @Override
          public String load(String key) {
            return aaptUtil.getPackageNameFromApk(key);
          }
        });
  }

  @Provides
  @Singleton
  @DexdumpPath
  public String provideDexdumpPath(@DexdumpPathFlag String pathFlag,
      @ResourceDexdumpPath Provider<String> resourcePath) {
    if ("".equals(pathFlag)) {
      return resourcePath.get();
    } else {
      return pathFlag;
    }
  }

  @Provides
  @SpongeOutputDirectory
  public String provideSpongeOutputDirectory(Environment env) {
    if (env.getOutputsDir().isPresent()) {
      return env.getOutputsDir().get().toString();
    }
    try {
      return env.createTempDir("outputs").toString();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

  }

  @Provides
  @SubprocessLogDir
  public File provideSubprocessLogFile(@SpongeOutputDirectory String outputDir) throws IOException {
    File logdir = new File(outputDir, "broker_logs");
    if (!logdir.exists()) {
      Files.createParentDirs(logdir);
      logdir.mkdir();
    }
    return logdir;
  }

  @Provides
  @Singleton
  @SubprocessExecution
  public ExecutorService provideSubprocessExecutor() {
    return Executors.newCachedThreadPool(new ThreadFactoryBuilder().
        setDaemon(true).setNameFormat("subprocess-executor-%s").build());
  }

  @Provides
  @AaptPath
  @Singleton
  public String provideAaptPath(
      @AaptPathFlag String aaptPath) {
    return aaptPath;
  }

  /** This provides all apks that are to be installed on the emulator except for system apks. */
  @Provides
  @ApksToInstall
  public List<String> provideApksToInstall(
      @ApksToInstallFlag List<String> apksToInstall,
      @DataDir File dataDir,
      @PrecompiledApksToInstallFlag List<String> precompiledApksToInstall,
      @SystemApksToInstall List<String> systemApks) {

    List<String> allApksToInstall = new ArrayList<>();
    allApksToInstall.addAll(apksToInstall);
    allApksToInstall.addAll(precompiledApksToInstall);
    List<String> finalApksToInstall = findApks(allApksToInstall, dataDir);
    finalApksToInstall.removeAll(systemApks);

    // Remove duplicates but keep the same relative order.
    return new ArrayList<>(new LinkedHashSet<>(finalApksToInstall));
  }

  @Provides
  @TestServicesApksToInstall
  public List<String> provideTestServicesApksToInstall(
      @TestServicesApkLocation Provider<String> testServicesApkLocation,
      @OdoApkLocation String odoApkLocation) {
    Preconditions.checkNotNull(testServicesApkLocation.get());
    Preconditions.checkNotNull(odoApkLocation);
    List<String> allApksToInstall =
        Lists.newArrayList(testServicesApkLocation.get(), odoApkLocation);
    return allApksToInstall;
  }

  @Provides
  @SystemApksToInstall
  public List<String> provideSystemApksToInstall(
      @SystemApksToInstallFlag List<String> systemApksToInstall,
      @DataDir File dataDir,
      @TestServicesApkLocation Provider<String> testServicesApkLocation) {
    return findApks(systemApksToInstall, dataDir);
  }

  private List<String> findApks(List<String> allApksToInstall, File dataDir){
    List<String> apks = new ArrayList<>();

    for (String apkPath : allApksToInstall) {
      if (!new File(apkPath).isAbsolute()) {
        File apkFile = new File(dataDir, apkPath);
        if (apkFile.exists()) {
          apks.add(apkFile.getPath());
        } else {
          String fileNameWithoutExtension = Files.getNameWithoutExtension(apkFile.getName());
          // If dex2oat_on_cloud was used, the filename generated by bazel contains _dex2oat_signed
          // So look for the either that file or the actual file name.
          String filePattern =
              String.format(
                  "%s|%s_dex2oat_signed.apk", apkFile.getName(), fileNameWithoutExtension);
          List<File> foundApks = new DirectorySearcher(dataDir, filePattern).findMatches();
          checkState(1 == foundApks.size(), String.format(
              "Must find 1 apk matching %s. Found: %s", apkFile.getName(), foundApks));
          apks.add(foundApks.get(0).getPath());
        }
      } else {
        apks.add(apkPath);
      }
    }
    return apks;
  }

  @Provides
  @SimAccessRulesFile
  public String provideSimAccessRulesFile(
      @DataDir File dataDir, @SimAccessRulesFileFlag String simAccessRulesFile) {
    if (isNullOrEmpty(simAccessRulesFile)) {
      return "";
    }
    checkState(
        !new File(simAccessRulesFile).isAbsolute(),
        "sim_access_rules_file needs to use relative path to android_test_support data dir");
    File rulesFile = new File(dataDir, simAccessRulesFile);
    checkState(rulesFile.exists(), "sim_access_rules_file not found: %s", simAccessRulesFile);
    return rulesFile.getAbsolutePath();
  }

  @Provides
  @DataDir
  public File provideWorkspace(Environment env) {
    return new File(env.getRunfilesDir());
  }

  @Provides
  @Singleton
  @AdbPath
  public String provideAdbPath(@DataDir File dataDir, @AdbPathFlag String adbPathFlag,
      @SelectedBroker DeviceBrokerType type) {
    String value = SYSTEM_ENV.get(ENVIRONMENT_KEY_ANDROID_ADB);
    if (value != null) {
      return value;
    } else if (DeviceBrokerType.LOCAL_ADB_SERVER == type && new File(CORP_ADB_PATH).exists()) {
      logging.info("Using Corp ADB from: " + CORP_ADB_PATH);
      return CORP_ADB_PATH;
    } else if (!isNullOrEmpty(adbPathFlag)) {
      return new File(adbPathFlag).getAbsolutePath();
    }
    List<File> adbPathFiles = new DirectorySearcher(dataDir, "adb$").findMatches();
    checkState(!adbPathFiles.isEmpty(), "No adb binary found!");
    checkState(1 == adbPathFiles.size(), "Ambiguous adb files found: " + adbPathFiles);
    return adbPathFiles.get(0).getAbsolutePath();
  }

  @Provides
  @Singleton
  @DeviceControllerPath
  public String provideDeviceControllerPath(
      @UseWaterfall boolean useWaterfall, @DataDir File dataDir, @AdbPath String realAdbPath) {

    File controller;
    if (useWaterfall) {
      controller =
          new File(
              dataDir,
              "tools/android/emulator/support/waterfall/waterfall_bin");
    } else {
      controller = new File(dataDir, "tools/android/emulator/support/adb.turbo");
    }

    if (controller.exists()) {
      return controller.getAbsolutePath();
    } else {
      return realAdbPath;
    }
  }


  @Provides
  @GeneratedLauncherScript
  public String provideLauncherScript(
      @LauncherScriptFlag String flagBasedScript, @DataDir File dataDir, Environment env) {
    if (!"".equals(flagBasedScript)) {
      File scriptFile = new File(flagBasedScript);
      if (!scriptFile.isAbsolute()) {
        return new File(dataDir,  flagBasedScript).getPath();
      } else {
        return flagBasedScript;
      }
    } else {
      List<File> launcherScriptFiles =
          new DirectorySearcher(
                  new File(env.getRunfilesDir()),
                  "^((android|google"
                      + ")_\\d+_(arm|arm_v7a|x86))|(.*_(usb|wifi))$")
              .findMatches();
      checkState(1 == launcherScriptFiles.size(),
          "Must find 1 launcher script matching. Found: %s", launcherScriptFiles);
      return launcherScriptFiles.get(0).getAbsolutePath();
    }
  }

  @Provides
  @LogcatPath
  public String provideLogcatFile(@SpongeOutputDirectory String outputDir) throws IOException {
    File logdir = new File(outputDir, "device_logcat");
    if (!logdir.exists()) {
      Files.createParentDirs(logdir);
      logdir.mkdir();
    }
    return File.createTempFile("logcat", ".txt", logdir).getAbsolutePath();
  }

  @Provides
  @Singleton
  public DeviceBroker provideDeviceBroker(
      @SelectedBroker DeviceBrokerType type,
      Map<DeviceBrokerType, Provider<DeviceBroker>> deviceBrokers,
      Set<DeviceBrokerDecorator> decorators) {
    DeviceBroker broker = deviceBrokers.get(type).get();
    for (DeviceBrokerDecorator dbd : decorators) {
      broker = dbd.decorate(broker);
    }
    return broker;
  }

  @Provides
  @TestTempDir
  @Singleton
  public File provideTestTempDir(Environment env) {
    try {
      return env.createTempDir(null);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public void configure() {

    bindConstant()
        .annotatedWith(ResourceDexdumpName.class)
    .to("/com/google/android/apps/common/testing/broker/dexdump_annotations");

    bindConstant()
        .annotatedWith(TestServicesApkResourceName.class)
        .to(
                "/services/test_services.apk");

    bindConstant()
        .annotatedWith(OdoApkResourceName.class)
        .to(
                "/runner/android_test_orchestrator/stubapp/stubapp.apk");

    bind(AdbController.AdbControllerFactory.class).to(AdbController.FullControlAdbControllerFactory.class);

    bind(String.class)
        .annotatedWith(ExecutorLocation.class)
        .toProvider(
            new ResourceProvider(
                Providers.of("/com/google/android/apps/common/testing/broker/executor.sh")))
        .in(Scopes.SINGLETON);

    bind(String.class).annotatedWith(OdoApkLocation.class).toProvider(
        new ResourceProvider(getProvider(Key.get(String.class, OdoApkResourceName.class))))
        .in(Scopes.SINGLETON);

    bind(String.class)
        .annotatedWith(TestServicesApkLocation.class)
        .toProvider(
            new ResourceProvider(
                getProvider(Key.get(String.class, TestServicesApkResourceName.class))))
        .in(Scopes.SINGLETON);

    bind(String.class).annotatedWith(ResourceDexdumpPath.class).toProvider(
        new ResourceProvider(getProvider(Key.get(String.class, ResourceDexdumpName.class))))
        .in(Scopes.SINGLETON);

    Multibinder.newSetBinder(binder(), DeviceBrokerDecorator.class);
    MapBinder.newMapBinder(binder(), DeviceBrokerType.class, DeviceBroker.class);

    MapBinder<String, String> adbEnv =
        MapBinder.newMapBinder(binder(), String.class, String.class, AdbEnvironment.class);
    adbEnv.addBinding(ENVIRONMENT_KEY_ANDROID_ADB).to(Key.get(String.class, AdbPath.class));
  }
}
