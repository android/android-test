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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AaptPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbServerPort;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdditionalTestPackages;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AssumeApksInstalled;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.BootstrapInstrumentationPackage;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ConsoleAuth;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DataPartitionSize;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceSerialNumber;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.Dex2OatOnCloudEnabled;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DexdumpPathFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EmulateNetworkType;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EmulatorStartupTimeoutFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EnableDisplay;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EnableGps;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ExtraCerts;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.GrantRuntimePermissions;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.HttpProxy;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.IgnoreTestPackages;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialIME;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialLocale;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InstallBasicServices;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InstallTestServices;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.KvmDevice;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LauncherScriptFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LogcatFilters;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LongPressTimeout;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.NumberOfCores;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.OpenGl;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PrecompiledApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PreverifyApks;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ReuseApks;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ScanTargetPackageForTests;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SelectedBroker;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SimAccessRulesFileFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SkipCoverageFilesCheck;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SystemApksToInstallFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestTimeoutOverride;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.UseWaterfall;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Module that exposes all flag definitions for device broker.
 *
 * <p>Attention - although in OpenSource we use JCommander to parse flags, internally Google uses a
 * different Flag library (closely related to the OpenSource gflags for C++). For this reason, this
 * class may seem a little odd. The oddity is intentional - we want to allow this class to be the
 * source of truth for flag names, descriptions, and default values. Internally another class uses
 * this class to define flags within Google. That is why the Builder has package protected fields
 * and methods.
 *
 * <p>If you wish to add flags to this class from OpenSource, please follow a pattern for a
 * similarly typed flag in this class, even if it seems to deviate from standard practices. It will
 * make it easier for us to expose the flag in our internal codebase (and more likely to accept the
 * change.)
 */
@VisibleForTesting
class DeviceBrokerOptions extends AbstractModule {

  private static final Map<String, String> SYSTEM_ENV = System.getenv();

  static final String ENVIRONMENT_KEY_DEVICE_BROKER_TYPE = "DEVICE_BROKER_TYPE";
  private static final String ENVIRONMENT_KEY_ADB_SERVER_PORT = "ANDROID_ADB_SERVER_PORT";
  private static final String ENVIRONMENT_KEY_DEVICE_SERIAL = "ANDROID_SERIAL";
  private static final String ENVIRONMENT_KEY_ENABLE_APK_REUSE = "ENABLE_APK_REUSE";
  private static final String ENVIRONMENT_DEVICE_SCRIPT = "DEVICE_SCRIPT";
  private static final String ENVIRONMENT_HTTP_PROXY = "http_proxy";
  private static final String ENVIRONMENT_INITIAL_LOCALE = "initial_locale";
  private static final String ENVIRONMENT_INITIAL_IME = "initial_ime";

  private static final int TIMEOUT_10_MINUTES = 10 * 60;

  private final String aaptPath;
  private final DeviceBrokerType deviceBrokerType;
  private final Boolean enableDisplay;
  private final String deviceScript;
  private final List<String> apksToInstall;
  private final List<String> assumeApksInstalled;
  private final Integer numberOfCores;
  private final List<String> additionalTestPackages;
  private final List<String> ignoreTestPackages;
  private final String bootstrapInstrumentationPackage;
  private final List<String> extraCerts;
  private final Boolean enableApkReuse;
  private final List<String> precompiledApksToInstall;
  private final List<String> systemApksToInstall;
  private final Boolean installBasicServices;
  private final Boolean installTestServices;
  private final String adbPath;
  private final String dexdumpPath;
  private final String deviceSerialNumber;
  private final ImmutableList<LogcatFilter> logcatFilters;
  private final Boolean preverifyApks;
  private final Boolean grantRuntimePermissions;
  private final Integer adbServerPort;
  private final OpenGlDriver openGlDriver;
  private final String kvmDevice;
  private final NetworkType networkType;
  private final Integer testTimeoutOverride;
  private final Integer emulatorStartupTimeout;
  private final String httpProxy;

  private final Boolean skipCoverageFilesCheck;
  private final Boolean scanTargetPackageForTests;
  private final Boolean enableConsoleAuth;
  private final Boolean enableGps;
  private final String initialLocale;
  private final String initialIme;
  private final Integer dataPartitionSize;
  private final Integer longPressTimeout;
  private final Boolean dex2OatOnCloudEnabled;
  private final String simAccessRulesFile;
  private final Boolean useWaterfall;

  @Override
  protected void configure() {
    // do not put logic here use provider methods.
  }

  private DeviceBrokerOptions(Builder b) {
    aaptPath = checkNotNull(b.aaptPath);
    deviceBrokerType = checkNotNull(b.deviceBrokerType);
    enableDisplay = checkNotNull(b.enableDisplay);
    deviceScript = checkNotNull(b.deviceScript);
    apksToInstall = ImmutableList.copyOf(b.apksToInstall);
    assumeApksInstalled = ImmutableList.copyOf(b.assumeApksInstalled);
    numberOfCores = checkNotNull(b.numberOfCores);
    additionalTestPackages = ImmutableList.copyOf(b.additionalTestPackages);
    ignoreTestPackages = ImmutableList.copyOf(b.ignoreTestPackages);
    bootstrapInstrumentationPackage = checkNotNull(b.bootstrapInstrumentationPackage);
    extraCerts = ImmutableList.copyOf(b.extraCerts);
    enableApkReuse = checkNotNull(b.enableApkReuse);
    precompiledApksToInstall = ImmutableList.copyOf(b.precompiledApksToInstall);
    systemApksToInstall = ImmutableList.copyOf(b.systemApksToInstall);
    installBasicServices = checkNotNull(b.installBasicServices);
    installTestServices = checkNotNull(b.installTestServices);
    adbPath = checkNotNull(b.adbPath);
    dexdumpPath = checkNotNull(b.dexdumpPath);
    deviceSerialNumber = checkNotNull(b.deviceSerialNumber);
    logcatFilters = checkNotNull(b.logcatFilters);
    preverifyApks = checkNotNull(b.preverifyApks);
    grantRuntimePermissions = checkNotNull(b.grantRuntimePermissions);
    adbServerPort = checkNotNull(b.adbServerPort);
    openGlDriver = checkNotNull(b.openGlDriver);
    kvmDevice = checkNotNull(b.kvmDevice);
    networkType = checkNotNull(b.networkType);
    testTimeoutOverride = checkNotNull(b.testTimeoutOverride);
    emulatorStartupTimeout = checkNotNull(b.emulatorStartupTimeout);
    httpProxy = checkNotNull(b.httpProxy);
    skipCoverageFilesCheck = checkNotNull(b.skipCoverageFilesCheck);
    scanTargetPackageForTests = checkNotNull(b.scanTargetPackageForTests);
    enableConsoleAuth = checkNotNull(b.enableConsoleAuth);
    enableGps = checkNotNull(b.enableGps);
    initialLocale = checkNotNull(b.initialLocale);
    initialIme = checkNotNull(b.initialIme);
    dataPartitionSize = checkNotNull(b.dataPartitionSize);
    longPressTimeout = checkNotNull(b.longPressTimeout);
    dex2OatOnCloudEnabled = checkNotNull(b.dex2OatOnCloudEnabled);
    useWaterfall = checkNotNull(b.useWaterfall);
    simAccessRulesFile = checkNotNull(b.simAccessRulesFile);
  }

  @Provides
  @AaptPathFlag
  String provideAaptPath() {
    return aaptPath;
  }

  @Provides
  @SelectedBroker
  DeviceBrokerType provideDeviceBrokerType() {
    return deviceBrokerType;
  }

  @Provides
  @EnableDisplay
  Boolean provideEnableDisplay() {
    return enableDisplay;
  }

  @Provides
  @LauncherScriptFlag
  String provideLauncherScript() {
    return deviceScript;
  }

  @Provides
  @ApksToInstallFlag
  List<String> provideApksToInstall() {
    return apksToInstall;
  }

  @Provides
  @AssumeApksInstalled
  List<String> provideAssumeApksInstalled() {
    return assumeApksInstalled;
  }

  @Provides
  @NumberOfCores
  Optional<Integer> provideNumberOfCores() {
    if (numberOfCores == 0) {
      return Optional.absent();
    }
    return Optional.of(numberOfCores);
  }

  @Provides
  @AdditionalTestPackages
  List<String> provideAdditionalTestPackages() {
    return additionalTestPackages;
  }

  @Provides
  @IgnoreTestPackages
  List<String> provideIgnoreTestPackages() {
    return ignoreTestPackages;
  }

  @Provides
  @BootstrapInstrumentationPackage
  String provideBootstrap() {
    return bootstrapInstrumentationPackage;
  }

  @Provides
  @ExtraCerts
  List<String> providesExtraCerts() {
    return extraCerts;
  }

  @Provides
  @ReuseApks
  Boolean provideReuseApk() {
    return enableApkReuse;
  }

  @Provides
  @PrecompiledApksToInstallFlag
  List<String> providePrecompiledApks() {
    return precompiledApksToInstall;
  }

  @Provides
  @SystemApksToInstallFlag
  List<String> provideSystemApks() {
    return systemApksToInstall;
  }

  @Provides
  @InstallBasicServices
  Boolean provideInstallBasicServices() {
    return installBasicServices;
  }

  @Provides
  @InstallTestServices
  Boolean provideInstallTestServices() {
    return installTestServices;
  }

  @Provides
  @AdbPathFlag
  String provideAdbPath() {
    return adbPath;
  }

  @Provides
  @DexdumpPathFlag
  String provideDexDumpFlag() {
    return dexdumpPath;
  }

  @Provides
  @DeviceSerialNumber
  String provideDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  @Provides
  @LogcatFilters
  List<LogcatFilter> provideLogcatFilters() {
    return logcatFilters;
  }

  @Provides
  @PreverifyApks
  Boolean providePreverify() {
    return preverifyApks;
  }

  @Provides
  @SimAccessRulesFileFlag
  String provideSimAccessRulesFileFlag() {
    return simAccessRulesFile;
  }

  @Provides
  @GrantRuntimePermissions
  Boolean provideGrantRuntime() {
    return grantRuntimePermissions;
  }

  @Provides
  @AdbServerPort
  Optional<Integer> provideAdbServerPort() {
    if (adbServerPort == 0) {
      return Optional.absent();
    }
    return Optional.of(adbServerPort);
  }

  @Provides
  @OpenGl
  OpenGlDriver provideOpenGL() {
    return openGlDriver;
  }

  @Provides
  @KvmDevice
  String provideKvm() {
    return kvmDevice;
  }

  @Provides
  @EmulateNetworkType
  NetworkType provideNetType() {
    return networkType;
  }

  @Provides
  @TestTimeoutOverride
  Optional<Integer> provideTestTimeout() {
    if (testTimeoutOverride == 0) {
      return Optional.absent();
    }
    return Optional.of(testTimeoutOverride);
  }

  @Provides
  @EmulatorStartupTimeoutFlag
  Integer provideEmulatorSTartupTimeout() {
    return emulatorStartupTimeout;
  }

  @Provides
  @HttpProxy
  String provideHttpProxy() {
    return httpProxy;
  }

  @Provides
  @SkipCoverageFilesCheck
  Boolean provideSkipCoverage() {
    return skipCoverageFilesCheck;
  }

  @Provides
  @ScanTargetPackageForTests
  Boolean provideScanTargetPackageForTests() {
    return scanTargetPackageForTests;
  }

  @Provides
  @ConsoleAuth
  Boolean provideEnableConsoleAuth() {
    return enableConsoleAuth;
  }

  @Provides
  @EnableGps
  Boolean provideEnableGps() {
    return enableGps;
  }

  @Provides
  @InitialLocale
  String provideLocale() {
    return initialLocale;
  }

  @Provides
  @InitialIME
  String provideInitialIme() {
    return initialIme;
  }

  @Provides
  @DataPartitionSize
  Integer provideDataPartition() {
    return dataPartitionSize;
  }

  @Provides
  @LongPressTimeout
  Integer proivdeLongPressTimeout() {
    return longPressTimeout;
  }

  @Provides
  @Dex2OatOnCloudEnabled
  Boolean provideDex2OatOnCloud() {
    return dex2OatOnCloudEnabled;
  }

  @Provides
  @UseWaterfall
  Boolean providesUseWaterfall() {
    return useWaterfall;
  }

  public static class Builder {
    public Builder() {}

    DeviceBrokerOptions build() {
      return new DeviceBrokerOptions(this);
    }

    Builder withCommandlineArgs(String[] testArgs) {
      JCommander jCommander = new JCommander(this);
      jCommander.setAcceptUnknownOptions(true);
      jCommander.setAllowParameterOverwriting(true);
      jCommander.parse(testArgs);
      return this;
    }

    static final String AAPT_FLAG = "aapt";
    static final String AAPT_FLAG_DESC = "The path to the aapt binary";

    @Parameter(names = "--" + AAPT_FLAG, description = AAPT_FLAG_DESC)
    public String aaptPath = "";

    Builder withAaptPath(String aaptPath) {
      this.aaptPath = checkNotNull(aaptPath);
      return this;
    }

    static final String DEVICE_BROKER_TYPE_FLAG = "device_broker_type";
    static final String DEVICE_BROKER_TYPE_FLAG_DESC = "The type of device broker to use in tests";

    @Parameter(
      names = "--" + DEVICE_BROKER_TYPE_FLAG,
      description = DEVICE_BROKER_TYPE_FLAG_DESC,
      converter = EnumConverters.DeviceBrokerTypeConverter.class
    )
    public DeviceBrokerType deviceBrokerType =
        SYSTEM_ENV.containsKey(ENVIRONMENT_KEY_DEVICE_BROKER_TYPE)
            ? DeviceBrokerType.valueOf(SYSTEM_ENV.get(ENVIRONMENT_KEY_DEVICE_BROKER_TYPE))
            : DeviceBrokerType.WRAPPED_EMULATOR;

    Builder withDeviceBrokerType(DeviceBrokerType type) {
      this.deviceBrokerType = checkNotNull(type);
      return this;
    }

    static final String ENABLE_DISPLAY_FLAG = "enable_display";
    static final String ENABLE_DISPLAY_FLAG_DESC =
        "If the broker supports it and user has set $DISPLAY env variable, device will be "
            + "displayed to the user.";

    @Parameter(
      names = "--" + ENABLE_DISPLAY_FLAG,
      description = ENABLE_DISPLAY_FLAG_DESC,
      arity = 1
    )
    public Boolean enableDisplay = Boolean.TRUE;

    Builder withEnableDisplay(Boolean enableDisplay) {
      this.enableDisplay = checkNotNull(enableDisplay);
      return this;
    }

    static final String DEVICE_SCRIPT_FLAG = "device_script";
    static final String DEVICE_SCRIPT_FLAG_DESC =
        "optional path to device start script, if none is provided, the data directory is searched";

    @Parameter(names = "--" + DEVICE_SCRIPT_FLAG, description = DEVICE_SCRIPT_FLAG_DESC)
    public String deviceScript =
        SYSTEM_ENV.containsKey(ENVIRONMENT_DEVICE_SCRIPT)
            ? SYSTEM_ENV.get(ENVIRONMENT_DEVICE_SCRIPT)
            : "";

    Builder withDeviceScript(String deviceScript) {
      this.deviceScript = checkNotNull(deviceScript);
      return this;
    }

    static final String SIM_ACCESS_RULES_FILE_FLAG = "sim_access_rules_file";
    static final String SIM_ACCESS_RULES_FILE_FLAG_DESC =
        "optional path to a sim access rules proto file. "
            + "Used to grant UICC carrier privileges to apps.";

    @Parameter(
      names = "--" + SIM_ACCESS_RULES_FILE_FLAG,
      description = SIM_ACCESS_RULES_FILE_FLAG_DESC
    )
    public String simAccessRulesFile = "";

    Builder withSimAccessRulesFile(String simAccessRulesFile) {
      this.simAccessRulesFile = checkNotNull(simAccessRulesFile);
      return this;
    }

    static final String APKS_TO_INSTALL_FLAG = "apks_to_install";
    static final String APKS_TO_INSTALL_FLAG_DESC =
        "optional apks to install on the device in the order listed.";

    @Parameter(names = "--" + APKS_TO_INSTALL_FLAG, description = APKS_TO_INSTALL_FLAG_DESC)
    public List<String> apksToInstall = new ArrayList<>();

    Builder withApksToInstall(List<String> apksToInstall) {
      checkNotNull(apksToInstall);
      this.apksToInstall.clear();
      this.apksToInstall.addAll(apksToInstall);
      return this;
    }

    static final String ASSUME_APKS_INSTALLED_FLAG = "assume_apks_installed";
    static final String ASSUME_APKS_INSTALLED_FLAG_DESC =
        "A list of packages to assume are already up-to-date on the device. If \"all\" appears "
            + "anywhere in the list, then all APKs are skipped.";

    @Parameter(
      names = "--" + ASSUME_APKS_INSTALLED_FLAG,
      description = ASSUME_APKS_INSTALLED_FLAG_DESC
    )
    public List<String> assumeApksInstalled = new ArrayList<>();

    Builder withAssumeApksInstalled(List<String> assume) {
      checkNotNull(assume);
      this.assumeApksInstalled.clear();
      this.assumeApksInstalled.addAll(assume);
      return this;
    }

    static final String CORES_FLAG = "cores";
    static final String CORES_FLAG_DESC =
        "Number of cores used for android_test, only meaningful for qemu2 devices.";

    @Parameter(names = "--" + CORES_FLAG, description = CORES_FLAG_DESC)
    public Integer numberOfCores = 0;

    Builder withNumberOfCores(Integer cores) {
      this.numberOfCores = checkNotNull(cores);
      return this;
    }

    static final String ADDITIONAL_TEST_PACKAGES_FLAG = "additional_test_packages";
    static final String ADDITIONAL_TEST_PACKAGES_FLAG_DESC =
        "A list of Android package names to include when searching for the package containing "
            + "the test cases.";

    @Parameter(
      names = ADDITIONAL_TEST_PACKAGES_FLAG,
      description = ADDITIONAL_TEST_PACKAGES_FLAG_DESC
    )
    List<String> additionalTestPackages = new ArrayList<>();

    Builder withAdditionalTestPackages(List<String> packages) {
      checkNotNull(packages);
      this.additionalTestPackages.clear();
      this.additionalTestPackages.addAll(packages);
      return this;
    }

    static final String IGNORE_TEST_PACKAGES_FLAG = "ignore_test_packages";
    static final String IGNORE_TEST_PACKAGES_FLAG_DESC =
        "A list of Android package names to exclude when searching for the package containing "
            + "the test cases. The package \"com.android.emulator.smoketests\" is automatically "
            + "added to this list.";

    @Parameter(
      names = "--" + IGNORE_TEST_PACKAGES_FLAG,
      description = IGNORE_TEST_PACKAGES_FLAG_DESC
    )
    public List<String> ignoreTestPackages = new ArrayList<>();

    Builder withIgnoreTestPackages(List<String> ignore) {
      checkNotNull(ignore);
      this.ignoreTestPackages.clear();
      this.ignoreTestPackages.addAll(ignore);
      return this;
    }

    static final String BOOTSTRAP_INSTRUMENTATION_PACKAGE_FLAG =
        "bootstrap_instrumentation_package";
    static final String BOOTSTRAP_INSTRUMENTATION_PACKAGE_FLAG_DESC =
        "The Android package name containing the instrumentation class to use to run the tests. "
            + "This flag is only needed if there are multiple Android packages containing a  "
            + "\"GoogleInstrumentationTestRunner\" class. If the test case classes are in a "
            + "different  package than this one, then this package name should also be specified "
            + "in the \"ignore_test_packages\" flag.";

    @Parameter(
      names = "--" + BOOTSTRAP_INSTRUMENTATION_PACKAGE_FLAG,
      description = BOOTSTRAP_INSTRUMENTATION_PACKAGE_FLAG_DESC
    )
    public String bootstrapInstrumentationPackage = "";

    Builder withBootstrapInstrumentationPackage(String pkg) {
      this.bootstrapInstrumentationPackage = checkNotNull(pkg);
      return this;
    }

    static final String EXTRA_CERTS_FLAG = "extra_certs";
    static final String EXTRA_CERTS_FLAG_DESC =
        "Extra certificates to install. Include these in your data deps and use a relative path";

    @Parameter(names = "--" + EXTRA_CERTS_FLAG, description = EXTRA_CERTS_FLAG_DESC)
    public List<String> extraCerts = new ArrayList<>();

    Builder withExtraCerts(List<String> extras) {
      checkNotNull(extras);
      extraCerts.clear();
      extraCerts.addAll(extras);
      return this;
    }

    static final String ENABLE_APK_REUSE_FLAG = "enable_apk_reuse";
    static final String ENABLE_APK_REUSE_FLAG_DESC = "enables reuse of already installed apks.";

    @Parameter(
      names = "--" + ENABLE_APK_REUSE_FLAG,
      description = ENABLE_APK_REUSE_FLAG_DESC,
      arity = 1
    )
    public Boolean enableApkReuse =
        !"false".equals(SYSTEM_ENV.get(ENVIRONMENT_KEY_ENABLE_APK_REUSE));

    Builder withEnableApkReuse(Boolean enable) {
      enableApkReuse = checkNotNull(enable);
      return this;
    }

    static final String PRECOMPILED_APKS_TO_INSTALL_FLAG = "precompiled_apks_to_install";
    static final String PRECOMPILED_APKS_TO_INSTALL_FLAG_DESC =
        "optional precompiled apks to install on the device in the order listed.";

    @Parameter(
      names = "--" + PRECOMPILED_APKS_TO_INSTALL_FLAG,
      description = PRECOMPILED_APKS_TO_INSTALL_FLAG_DESC
    )
    public List<String> precompiledApksToInstall = new ArrayList<>();

    Builder withPrecompiledApksToInstall(List<String> apks) {
      checkNotNull(apks);
      precompiledApksToInstall.clear();
      precompiledApksToInstall.addAll(apks);
      return this;
    }

    static final String SYSTEM_APKS_TO_INSTALL_FLAG = "system_apks_to_install";
    static final String SYSTEM_APKS_TO_INSTALL_FLAG_DESC =
        "optional apks to install onto /system partition in the order listed.";

    @Parameter(
      names = "--" + SYSTEM_APKS_TO_INSTALL_FLAG,
      description = SYSTEM_APKS_TO_INSTALL_FLAG_DESC
    )
    public List<String> systemApksToInstall = new ArrayList<>();

    Builder withSystemApksToInstall(List<String> apks) {
      checkNotNull(apks);
      systemApksToInstall.clear();
      systemApksToInstall.addAll(apks);
      return this;
    }

    static final String INSTALL_BASIC_SERVICES_FLAG = "install_basic_services";
    static final String INSTALL_BASIC_SERVICES_FLAG_DESC = "install backdoor basic services apk";

    @Parameter(
      names = "--" + INSTALL_BASIC_SERVICES_FLAG,
      description = INSTALL_BASIC_SERVICES_FLAG_DESC,
      arity = 1
    )
    public Boolean installBasicServices = Boolean.TRUE;

    Builder withInstallBasicServices(Boolean installBasicServices) {
      this.installBasicServices = checkNotNull(installBasicServices);
      return this;
    }

    static final String INSTALL_TEST_SERVICES_FLAG = "install_test_services";
    static final String INSTALL_TEST_SERVICES_FLAG_DESC = "install test services apk";

    @Parameter(
      names = "--" + INSTALL_TEST_SERVICES_FLAG,
      description = INSTALL_TEST_SERVICES_FLAG_DESC,
      arity = 1
    )
    public Boolean installTestServices = Boolean.FALSE;

    Builder withInstallTestServices(Boolean installTestServices) {
      this.installTestServices = checkNotNull(installTestServices);
      return this;
    }

    static final String ADB_FLAG = "adb";
    static final String ADB_FLAG_DESC = "The dynamically linked adb binary";

    @Parameter(names = "--" + ADB_FLAG, description = ADB_FLAG_DESC)
    public String adbPath = "";

    Builder withAdbPath(String adbPath) {
      this.adbPath = checkNotNull(adbPath);
      return this;
    }

    static final String DEXDUMP_PATH_FLAG = "dexdump_path";
    static final String DEXDUMP_PATH_FLAG_DESC = "the path to the dexdump binary";

    @Parameter(names = "--" + DEXDUMP_PATH_FLAG, description = DEXDUMP_PATH_FLAG_DESC)
    public String dexdumpPath = "";

    Builder withDexdumpPath(String dexdumpPath) {
      this.dexdumpPath = checkNotNull(dexdumpPath);
      return this;
    }

    static final String DEVICE_SERIAL_NUMBER_FLAG = "device_serial_number";
    static final String DEVICE_SERIAL_NUMBER_FLAG_DESC =
        "The serial number of the device as shown by running 'adb devices'.";

    @Parameter(
      names = "--" + DEVICE_SERIAL_NUMBER_FLAG,
      description = DEVICE_SERIAL_NUMBER_FLAG_DESC
    )
    public String deviceSerialNumber =
        SYSTEM_ENV.containsKey(ENVIRONMENT_KEY_DEVICE_SERIAL)
            ? SYSTEM_ENV.get(ENVIRONMENT_KEY_DEVICE_SERIAL)
            : "";

    Builder withDeviceSerialNumber(String serial) {
      this.deviceSerialNumber = checkNotNull(serial);
      return this;
    }

    static final String LOGCAT_FILTER_FLAG = "logcat_filter";
    static final String LOGCAT_FILTER_FLAG_DESC =
        "A comma-separated list of filter specs to be passed to 'adb logcat'.";

    @Parameter(
      names = "--" + LOGCAT_FILTER_FLAG,
      description = LOGCAT_FILTER_FLAG_DESC,
      converter = LogcatFilterConverter.Multiple.class
    )
    public ImmutableList<LogcatFilter> logcatFilters = LogcatFilter.fromStringList("*:I");

    Builder withLogcatFilters(String filters) {
      this.logcatFilters = LogcatFilter.fromStringList(checkNotNull(filters));
      return this;
    }

    Builder withLogcatFilters(List<LogcatFilter> filters) {
      this.logcatFilters = ImmutableList.copyOf(checkNotNull(filters));
      return this;
    }

    static final String PREVERIFY_APKS_FLAG = "preverify_apks";
    static final String PREVERIFY_APKS_FLAG_DESC =
        "controls whether dex verify/optimization will be applied to installed apks";

    @Parameter(
      names = "--" + PREVERIFY_APKS_FLAG,
      description = PREVERIFY_APKS_FLAG_DESC,
      arity = 1
    )
    public Boolean preverifyApks = Boolean.FALSE;

    Builder withPreverifyApks(Boolean preverifyApks) {
      this.preverifyApks = checkNotNull(preverifyApks);
      return this;
    }

    static final String GRANT_RUNTIME_PERMISSIONS_FLAG = "grant_runtime_permissions";
    static final String GRANT_RUNTIME_PERMISSIONS_FLAG_DESC =
        "APK's will be installed with -g for api level >= 23, so that the apps have all the "
            + "necessary permissions during runtime";

    @Parameter(
      names = "--" + GRANT_RUNTIME_PERMISSIONS_FLAG,
      description = GRANT_RUNTIME_PERMISSIONS_FLAG_DESC,
      arity = 1
    )
    public Boolean grantRuntimePermissions = Boolean.TRUE;

    Builder withGrantRuntimePermissions(Boolean grantRuntimePermissions) {
      this.grantRuntimePermissions = checkNotNull(grantRuntimePermissions);
      return this;
    }

    static final String ADB_SERVER_PORT_FLAG = "adb_server_port";
    static final String ADB_SERVER_PORT_FLAG_DESC =
        "The port on which your local adb server is running.";

    @Parameter(names = "--" + ADB_SERVER_PORT_FLAG, description = ADB_SERVER_PORT_FLAG_DESC)
    public Integer adbServerPort =
        SYSTEM_ENV.containsKey(ENVIRONMENT_KEY_ADB_SERVER_PORT)
            ? Integer.valueOf(SYSTEM_ENV.get(ENVIRONMENT_KEY_ADB_SERVER_PORT))
            : 0;

    Builder withAdbServerPort(Integer port) {
      this.adbServerPort = checkNotNull(port);
      return this;
    }

    static final String OPEN_GL_DRIVER_FLAG = "open_gl_driver";
    static final String OPEN_GL_DRIVER_FLAG_DESC =
        "The driver that will be used for open gl emulation. Note: not all host hardware and not "
            + "all API levels support all modes.";

    @Parameter(
      names = "--" + OPEN_GL_DRIVER_FLAG,
      description = OPEN_GL_DRIVER_FLAG_DESC,
      converter = EnumConverters.OpenGlDriverConverter.class
    )
    public OpenGlDriver openGlDriver = OpenGlDriver.DEFAULT;

    Builder withOpenGlDriver(OpenGlDriver type) {
      this.openGlDriver = checkNotNull(type);
      return this;
    }

    static final String KVM_DEVICE_FLAG = "kvm_device";
    static final String KVM_DEVICE_FLAG_DESC = "The path to the /dev/kvm pseudo device.";

    @Parameter(names = "--" + KVM_DEVICE_FLAG, description = KVM_DEVICE_FLAG_DESC)
    public String kvmDevice = "/dev/kvm";

    Builder withKvmDevice(String kvmDevice) {
      this.kvmDevice = checkNotNull(kvmDevice);
      return this;
    }

    @EmulateNetworkType static final String NETWORK_TYPE_FLAG = "network_type";
    static final String NETWORK_TYPE_FLAG_DESC =
        "Emulate different network conditions. Defaults to fastnet";

    @Parameter(
      names = "--" + NETWORK_TYPE_FLAG,
      description = NETWORK_TYPE_FLAG_DESC,
      converter = EnumConverters.NetworkTypeConverter.class
    )
    public NetworkType networkType = NetworkType.FASTNET;

    Builder withNetworkType(NetworkType type) {
      this.networkType = checkNotNull(type);
      return this;
    }

    static final String TEST_TIMEOUT_OVERRIDE_FLAG = "test_timeout_override";
    static final String TEST_TIMEOUT_OVERRIDE_FLAG_DESC =
        "use the given timeout (seconds) for all tests";

    @Parameter(
      names = "--" + TEST_TIMEOUT_OVERRIDE_FLAG,
      description = TEST_TIMEOUT_OVERRIDE_FLAG_DESC
    )
    public Integer testTimeoutOverride = 0;

    Builder withTestTimeoutOverride(Integer timeout) {
      this.testTimeoutOverride = checkNotNull(timeout);
      return this;
    }

    static final String EMULATOR_STARTUP_TIMEOUT_FLAG = "emulator_startup_timeout";
    static final String EMULATOR_STARTUP_TIMEOUT_FLAG_DESC =
        "Timeout (seconds) for starting up the emulator.";

    @Parameter(
      names = "--" + EMULATOR_STARTUP_TIMEOUT_FLAG,
      description = EMULATOR_STARTUP_TIMEOUT_FLAG_DESC
    )
    public Integer emulatorStartupTimeout = TIMEOUT_10_MINUTES;

    Builder withEmulatorStartupTimeout(Integer timeout) {
      this.emulatorStartupTimeout = checkNotNull(timeout);
      return this;
    }

    static final String HTTP_PROXY_FLAG = "http_proxy";
    static final String HTTP_PROXY_FLAG_DESC =
        "The proxy the emulator should use to connect to the network.";

    @Parameter(names = "--" + HTTP_PROXY_FLAG, description = HTTP_PROXY_FLAG_DESC)
    public String httpProxy =
        SYSTEM_ENV.containsKey(ENVIRONMENT_HTTP_PROXY)
            ? SYSTEM_ENV.get(ENVIRONMENT_HTTP_PROXY)
            : "";

    Builder withHttpProxy(String proxy) {
      this.httpProxy = checkNotNull(proxy);
      return this;
    }

    static final String SKIP_COVERAGE_FILES_CHECK_FLAG = "skip_coverage_files_check";
    static final String SKIP_COVERAGE_FILES_CHECK_FLAG_DESC =
        "Don't check for the presence of com.vladium.emma.rt.RT and related code coverage "
            + "classes, because they are in an external APK not scanned by the test "
            + "infrastructure.";

    @Parameter(
      names = "--" + SKIP_COVERAGE_FILES_CHECK_FLAG,
      description = SKIP_COVERAGE_FILES_CHECK_FLAG_DESC,
      arity = 1
    )
    public Boolean skipCoverageFilesCheck = Boolean.FALSE;

    Builder withSkipCoverageFilesCheck(Boolean skipCoverageFilesCheck) {
      this.skipCoverageFilesCheck = checkNotNull(skipCoverageFilesCheck);
      return this;
    }

    static final String SCAN_TARGET_PACKAGE_FOR_TESTS_FLAG = "scan_target_package_for_tests";
    static final String SCAN_TARGET_PACKAGE_FOR_TESTS_FLAG_DESC =
        "control whether test cases in the target package apk are included in test execution";

    @Parameter(
      names = "--" + SCAN_TARGET_PACKAGE_FOR_TESTS_FLAG,
      description = SCAN_TARGET_PACKAGE_FOR_TESTS_FLAG_DESC,
      arity = 1
    )
    public Boolean scanTargetPackageForTests = Boolean.TRUE;

    Builder withScanTargetPackageForTests(Boolean scanTargetPackageForTests) {
      this.scanTargetPackageForTests = checkNotNull(scanTargetPackageForTests);
      return this;
    }

    static final String ENABLE_CONSOLE_AUTH_FLAG = "enable_console_auth";
    static final String ENABLE_CONSOLE_AUTH_FLAG_DESC =
        "enables console port auth for security reasons.";

    @Parameter(
      names = "--" + ENABLE_CONSOLE_AUTH_FLAG,
      description = ENABLE_CONSOLE_AUTH_FLAG_DESC,
      arity = 1
    )
    public Boolean enableConsoleAuth = Boolean.FALSE;

    Builder withEnableConsoleAuth(Boolean enableConsoleAuth) {
      this.enableConsoleAuth = checkNotNull(enableConsoleAuth);
      return this;
    }

    static final String ENABLE_DEX2OAT_ON_CLOUD_FLAG = "dex2oat_on_cloud_enabled";
    static final String ENABLE_DEX2OAT_ON_CLOUD_FLAG_DESC =
        "Enables emulator to run custom dex2oat. Flag passed by bazel";

    @Parameter(
      names = "--" + ENABLE_DEX2OAT_ON_CLOUD_FLAG,
      description = ENABLE_DEX2OAT_ON_CLOUD_FLAG_DESC,
      arity = 1
    )
    public Boolean dex2OatOnCloudEnabled = Boolean.FALSE;

    Builder withDex2OatOnCloudEnabled(Boolean enableDex2Oat) {
      this.dex2OatOnCloudEnabled = checkNotNull(enableDex2Oat);
      return this;
    }

    static final String ENABLE_GPS_FLAG = "enable_gps";
    static final String ENABLE_GPS_FLAG_DESC = "enables emulator gps simulation.";

    @Parameter(names = "--" + ENABLE_GPS_FLAG, description = ENABLE_GPS_FLAG_DESC, arity = 1)
    public Boolean enableGps = Boolean.TRUE;

    Builder withEnableGps(Boolean enableGps) {
      this.enableGps = checkNotNull(enableGps);
      return this;
    }

    static final String INITIAL_LOCALE_FLAG = "initial_locale";
    static final String INITIAL_LOCALE_FLAG_DESC =
        "The locale that would be set during the emulator initialization";

    @Parameter(names = "--" + INITIAL_LOCALE_FLAG, description = INITIAL_LOCALE_FLAG_DESC)
    public String initialLocale =
        SYSTEM_ENV.containsKey(ENVIRONMENT_INITIAL_LOCALE)
            ? SYSTEM_ENV.get(ENVIRONMENT_INITIAL_LOCALE)
            : "en-US";

    Builder withInitialLocale(String initialLocale) {
      this.initialLocale = checkNotNull(initialLocale);
      return this;
    }

    static final String INITIAL_IME_FLAG = "initial_ime";
    static final String INITIAL_IME_FLAG_DESC =
        "The IME that would be set during the emulator initialization";

    @Parameter(names = "--" + INITIAL_IME_FLAG, description = INITIAL_IME_FLAG_DESC)
    public String initialIme =
        SYSTEM_ENV.containsKey(ENVIRONMENT_INITIAL_IME)
            ? SYSTEM_ENV.get(ENVIRONMENT_INITIAL_IME)
            : "com.android.inputmethod.latin/.LatinIME";

    Builder withInitialIME(String initialIme) {
      this.initialIme = checkNotNull(initialIme);
      return this;
    }

    static final String DATA_PARTITION_SIZE_FLAG = "data_partition_size";
    static final String DATA_PARTITION_SIZE_FLAG_DESC =
        "Set a non-default data_partition_size for test.";

    @Parameter(names = "--" + DATA_PARTITION_SIZE_FLAG, description = DATA_PARTITION_SIZE_FLAG_DESC)
    public Integer dataPartitionSize = 0;

    Builder withDataPartitionSize(Integer dataPartitionSize) {
      this.dataPartitionSize = checkNotNull(dataPartitionSize);
      return this;
    }

    static final String LONG_PRESS_TIMEOUT_FLAG = "long_press_timeout";
    static final String LONG_PRESS_TIMEOUT_FLAG_DESC =
        "Set a non-default long press timeout for test (milliseconds).";

    @Parameter(names = "--" + LONG_PRESS_TIMEOUT_FLAG, description = LONG_PRESS_TIMEOUT_FLAG_DESC)
    public Integer longPressTimeout = 5000;

    Builder withLongPressTimeout(Integer longPressTimeout) {
      this.longPressTimeout = checkNotNull(longPressTimeout);
      return this;
    }

    static final String EXPERIMENTAL_USE_WATERFALL_FLAG = "experimental_use_waterfall";
    static final String EXPERIMENTAL_USE_WATERFALL_FLAG_DESC =
        "Uses waterfall to control the devices instead of adb.turbo";

    @Parameter(
        names = "--" + EXPERIMENTAL_USE_WATERFALL_FLAG,
        description = EXPERIMENTAL_USE_WATERFALL_FLAG_DESC,
        arity = 1)
    public Boolean useWaterfall = Boolean.FALSE;

    Builder withUseWaterfall(Boolean useWaterfall) {
      this.useWaterfall = checkNotNull(useWaterfall);
      return this;
    }
  }
}
