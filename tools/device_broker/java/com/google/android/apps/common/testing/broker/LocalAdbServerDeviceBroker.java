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

import com.google.android.apps.common.testing.broker.AdbController.AdbControllerFactory;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbEnvironment;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbServerPort;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceControllerPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceSerialNumber;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.GrantRuntimePermissions;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialIME;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialLocale;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InstallTestServices;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LogcatPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PackageName;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PreverifyApks;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ReuseApks;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestServicesApksToInstall;
import com.google.android.apps.common.testing.broker.LogcatStreamer.OutputFormat;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;

/**
 * A broker that provides access to devices connected to the local adb server.
 *
 */
@Singleton
class LocalAdbServerDeviceBroker implements DeviceBroker {
  private static final Logger logger = Logger.getLogger("LocalDeviceBroker");
  private static final Joiner SPACE_SEP = Joiner.on(" ");
  private static final int DEFAULT_ADB_SERVER_PORT = 5037;
  private static final String FLAG_RECEIVER_FOREGROUND = "268435456"; // 0x10000000

  @VisibleForTesting
  static final String DALVIK_VM_DEXOPT_FLAGS = "dalvik.vm.dexopt-flags";
  @VisibleForTesting
  static final String PREVERIFY_ON_SUBSTRING = "v=n";
  @VisibleForTesting
  static final String PREVERIFY_ON = "v=y,o=v";
  @VisibleForTesting
  static final String PREVERIFY_OFF = "v=n,o=v";

  private final Optional<Integer> adbServerPort;
  private final String adbPath;
  private final AdbControllerFactory adbControllerFactory;
  private final List<String> apksToInstall;
  private final List<String> testServicesApksToInstall;
  private final boolean installTestServices;
  private final Provider<String> logcatFilePathProvider;
  private final Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider;
  private final Provider<AdbDevicesLineProcessor> adbDevicesLineProcessor;
  private final LoadingCache<String, String> packageNameCache;
  private final Map<String, String> adbEnvironment;
  private final boolean enablePreverify;
  private final boolean enableApkReuse;
  private final String initialLocale;
  private final String initialIME;
  private String deviceSerialNumber;
  private Set<String> leasedDeviceSerialNumbers = Sets.newHashSet();
  private LogcatStreamer logcatStreamer;
  private final boolean grantRuntimePermissions;

  @Inject
  LocalAdbServerDeviceBroker(
      @AdbServerPort Optional<Integer> adbServerPort,
      @DeviceControllerPath String adbPath,
      AdbControllerFactory adbControllerFactory,
      @ApksToInstall List<String> apksToInstall,
      @TestServicesApksToInstall List<String> testServicesApksToInstall,
      @InstallTestServices boolean installTestServices,
      @LogcatPath Provider<String> logcatFilePathProvider,
      Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider,
      @DeviceSerialNumber String deviceSerialNumber,
      Provider<AdbDevicesLineProcessor> adbDevicesLineProcessor,
      @PackageName LoadingCache<String, String> packageNameCache,
      @AdbEnvironment Map<String, String> adbEnvironment,
      @PreverifyApks boolean enablePreverify,
      @ReuseApks boolean enableApkReuse,
      @InitialLocale String initialLocale,
      @InitialIME String initialIME,
      @GrantRuntimePermissions boolean grantRuntimePermissions) {
    this.adbPath = adbPath;
    this.adbControllerFactory = adbControllerFactory;
    this.apksToInstall = apksToInstall;
    this.testServicesApksToInstall = testServicesApksToInstall;
    this.installTestServices = installTestServices;
    this.logcatFilePathProvider = logcatFilePathProvider;
    this.communicatorBuilderProvider = communicatorBuilderProvider;
    this.deviceSerialNumber = deviceSerialNumber;
    this.adbServerPort = adbServerPort;
    this.adbDevicesLineProcessor = adbDevicesLineProcessor;
    this.packageNameCache = packageNameCache;
    this.adbEnvironment = adbEnvironment;
    this.enablePreverify = enablePreverify;
    this.enableApkReuse = enableApkReuse;
    this.initialLocale = initialLocale;
    this.initialIME = initialIME;
    this.grantRuntimePermissions = grantRuntimePermissions;
  }

  @Override
  public BrokeredDevice leaseDevice() {
    boolean doExplicitConnect = true;
    if (deviceSerialNumber.equals("")) {
      deviceSerialNumber = getSerialNumberFromAdbDevices();
      // since we see it in adb devices, do not need to connect to it
      doExplicitConnect = false;
    } else if (deviceSerialNumber.startsWith("emulator-")) {
      doExplicitConnect = false;
    }
    String logcatPath = logcatFilePathProvider.get();

    BrokeredDevice device = new BrokeredDevice.Builder()
        .withAdbPath(adbPath)
        .withAdbServerPort(adbServerPort.or(
            DEFAULT_ADB_SERVER_PORT))
        .defaultDeviceType() // figure it out on demand.
        .withLogcatPath(logcatPath)
        .withSerialId(deviceSerialNumber)
        .withAdbControllerFactory(adbControllerFactory)
        .withAdbEnvironment(adbEnvironment)
        .build();

    AdbController adbController = adbControllerFactory.create(device);
    if (doExplicitConnect) {
      adbController.adbConnect();
    }

    startLogcatStream(adbController, logcatPath);

    if (!leasedDeviceSerialNumbers.contains(deviceSerialNumber)) {
      adbController.makeAdbCall("root");
      adbController.makeAdbCall("wait-for-device");
      installApks(adbController);
      leasedDeviceSerialNumbers.add(deviceSerialNumber);
    }

    logger.info("About to broadcast ACTION_MOBILE_NINJAS_START");
    Map<String, String> extras = Maps.newHashMap();
    extras.put("initial_locale", initialLocale);
    extras.put("initial_ime", initialIME);
    device.getAdbController().broadcastAction(
        "ACTION_MOBILE_NINJAS_START",
        FLAG_RECEIVER_FOREGROUND,
        "com.google.android.apps.common.testing.services.bootstrap",
        extras);
    return device;
  }

  private void startLogcatStream(AdbController adbController, String logcatPath) {
    logcatStreamer =
        adbController.startLogcatStream(
            new File(logcatPath),
            LogcatStreamer.Buffer.MAIN,
            OutputFormat.THREADTIME,
            Lists.<LogcatFilter>newArrayList());
  }

  private void installApks(AdbController adbController) {
    final String originalFlags = adbController.getDeviceProperty(DALVIK_VM_DEXOPT_FLAGS);

    final boolean originalVerifyState =
        originalFlags == null || !originalFlags.contains(PREVERIFY_ON_SUBSTRING);
    final boolean swapFlags = originalVerifyState ^ enablePreverify;
    if (swapFlags) {
      if (enablePreverify) {
        setDextoptFlags(adbController, PREVERIFY_ON);
      } else {
        setDextoptFlags(adbController, PREVERIFY_OFF);
      }
    }

    List<String> allApksToInstall = apksToInstall;
    if (installTestServices) {
      allApksToInstall.addAll(testServicesApksToInstall);
    }

    for (String apk : allApksToInstall) {
      String packageName = packageNameCache.getUnchecked(apk);

      if (enableApkReuse) {
        adbController.installApkIfNecessary(packageName, apk, grantRuntimePermissions);
      } else {
        logger.info("Apk reuse is disabled, will uninstall and reinstall apk " + apk);
        adbController.uninstallApp(packageName);
        adbController.installApk(apk, grantRuntimePermissions);
      }
    }

    if (swapFlags) {
      setDextoptFlags(adbController, originalFlags);
    }
  }

  private void setDextoptFlags(AdbController adbController, String flags) {
    adbController.setDeviceProperty("dalvik.vm.dexopt-flags", flags);
  }

  private String getSerialNumberFromAdbDevices() {
    // Run "adb devices".
    List<String> command = Lists.newArrayList(adbPath, "devices");
    Map<String, String> environment = Maps.newHashMap(adbEnvironment);
    environment.put("ANDROID_ADB_SERVER_PORT",
          String.valueOf(adbServerPort.or(DEFAULT_ADB_SERVER_PORT)));

    AdbDevicesLineProcessor processor = adbDevicesLineProcessor.get();
    communicatorBuilderProvider.get()
        .withArguments(command)
        .withTimeout(120, TimeUnit.SECONDS)
        .withStdoutProcessor(processor)
        .withEnvironment(environment)
        .build()
        .communicate();

    // Parse output.
    List<String> result = processor.getResult();
    if (result.isEmpty()) {
      throw new RuntimeException(
          "No devices found. Please ensure that 'adb devices' shows at least one device.");
    }
    if (result.size() > 1) {
      StringBuilder errorStringBuilder = new StringBuilder()
          .append("More than one device found.")
          .append("Select a device by providing --test_arg=--device_serial_number=\"<serial>\".\n")
          .append("List of available devices:\n");
      for (String serial : result) {
        errorStringBuilder.append("- " + serial + "\n");
      }
      throw new RuntimeException(errorStringBuilder.toString());
    }
    return result.get(0);
  }

  @Override
  public void freeDevice(BrokeredDevice device) {
    if (logcatStreamer != null) {
      logcatStreamer.stopStream();
    }
  }

  @Override
  public Map<String, Object> getExportedProperties() {
    Map<String, Object> properties = Maps.newHashMap();
    properties.put("adb_path", adbPath);
    properties.put("adb_server_port", adbServerPort.or(
      DEFAULT_ADB_SERVER_PORT));
    properties.put("device_serial_number", deviceSerialNumber);
    properties.put("apks_to_install", SPACE_SEP.join(apksToInstall));
    return properties;
  }

}
