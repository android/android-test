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
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Closeables.closeQuietly;

import com.google.android.apps.common.testing.broker.AdbController.AdbControllerFactory;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbEnvironment;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdbServerPort;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ConsoleAuth;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DataPartitionSize;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceControllerPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.DeviceSerialNumber;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.Dex2OatOnCloudEnabled;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EmulateNetworkType;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EmulatorStartupTimeoutFlag;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EnableDisplay;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.EnableGps;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ExtraCerts;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.GeneratedLauncherScript;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.GrantRuntimePermissions;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialIME;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InitialLocale;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InstallTestServices;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.KvmDevice;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LogcatFilters;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LogcatPath;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.LongPressTimeout;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.NumberOfCores;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.OpenGl;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.PreverifyApks;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SimAccessRulesFile;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SubprocessLogDir;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SystemApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestServicesApksToInstall;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.UniquePort;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.UseWaterfall;
import com.google.android.apps.common.testing.proto.EmulatorProtos.EmulatorMetaDataPb;
import com.google.android.apps.common.testing.proto.EmulatorProtos.PerformanceDataPb;
import com.google.android.apps.common.testing.proto.EmulatorProtos.PropertyPb;
import com.google.android.apps.common.testing.proto.EmulatorProtos.TimerPb;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.LineProcessor;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A broker that uses generated wrapper scripts to launch emulated devices.
 *
 */
@Singleton
class WrappedEmulatedDeviceBroker implements DeviceBroker {

  private static final Logger logger = Logger.getLogger(
      WrappedEmulatedDeviceBroker.class.getName());
  private static final Joiner SPACE_SEP = Joiner.on(" ");
  private static final Joiner COMMA_SEP = Joiner.on(",");
  private static final Set<FieldDescriptor.Type> SIMPLE_TYPES = EnumSet.complementOf(
      EnumSet.of(FieldDescriptor.Type.GROUP, FieldDescriptor.Type.BYTES,
                 FieldDescriptor.Type.MESSAGE));
  private static final AtomicInteger LAUNCH_COUNT = new AtomicInteger();
  private static final String FLAG_RECEIVER_FOREGROUND = "268435456"; // 0x10000000

  private final String emulatorLauncherPath;
  private final String adbPath;
  private final Provider<Integer> portPicker;
  private final Provider<String> logcatFilePathProvider;
  private final Provider<List<LogcatFilter>> logcatFiltersProvider;
  private final AdbControllerFactory adbControllerFactory;
  private final Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider;
  private final List<String> apksToInstall;
  private final List<String> systemApksToInstall;
  private final List<String> testServicesApksToInstall;
  private final boolean installTestServices;
  private final List<String> extraCerts;
  private final boolean enableDisplay;
  private final File subprocessLogDir;
  private final boolean enablePreverify;
  private final NetworkType networkType;
  private final boolean enableConsoleAuth;
  private final boolean enableGps;
  private int vncServerPort;
  private final Optional<Integer> adbServerPort;
  private final Map<String, String> adbEnvironment;
  private final Optional<Integer> numberOfCores;
  private final OpenGlDriver openGlDriver;
  private final String kvmDevice;
  private final String deviceSerialNumber;
  private final String initialLocale;
  private final String initialIME;
  private final Integer emulatorStartupTimeout;
  private final Integer dataPartitionSize;
  private final Integer longPressTimeout;
  private final boolean grantRuntimePermissions;
  private final Environment environment;
  private final boolean enableDex2OatOnCloud;
  private final String simAccessRulesFile;
  private final boolean useWaterfall;

  enum ScriptAction {
    START("start"), STOP("kill");

    private final String commandName;

    ScriptAction(String commandName) {
      this.commandName = commandName;
    }

    public String getCommandName() {
      return commandName;
    }
  }

  @Inject
  WrappedEmulatedDeviceBroker(
      @GeneratedLauncherScript String emulatorLauncherPath,
      @DeviceControllerPath String adbPath,
      @UniquePort Provider<Integer> portPicker,
      @LogcatPath Provider<String> logcatFilePathProvider,
      @LogcatFilters Provider<List<LogcatFilter>> logcatFiltersProvider,
      AdbControllerFactory adbControllerFactory,
      Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider,
      @SubprocessLogDir File subprocessLogDir,
      @ApksToInstall List<String> apksToInstall,
      @SystemApksToInstall List<String> systemApksToInstall,
      @TestServicesApksToInstall List<String> testServicesApksToInstall,
      @InstallTestServices boolean installTestServices,
      @EnableDisplay boolean enableDisplay,
      @PreverifyApks boolean enablePreverify,
      @ConsoleAuth boolean enableConsoleAuth,
      @EnableGps boolean enableGps,
      @EmulateNetworkType NetworkType networkType,
      @AdbServerPort Optional<Integer> adbServerPort,
      @AdbEnvironment Map<String, String> adbEnvironment,
      @NumberOfCores Optional<Integer> numberOfCores,
      @OpenGl OpenGlDriver openGlDriver,
      @KvmDevice String kvmDevice,
      @DeviceSerialNumber String deviceSerialNumber,
      @InitialLocale String initialLocale,
      @InitialIME String initialIME,
      @ExtraCerts List<String> extraCerts,
      @DataPartitionSize Integer dataPartitionSize,
      @LongPressTimeout Integer longPressTimeout,
      @GrantRuntimePermissions boolean grantRuntimePermissions,
      @EmulatorStartupTimeoutFlag Integer emulatorStartupTimeout,
      @Dex2OatOnCloudEnabled boolean enableDex2OatOnCloud,
      @SimAccessRulesFile String simAccessRulesFile,
      @UseWaterfall boolean useWaterfall,
      Environment environment) {
    this.emulatorLauncherPath = emulatorLauncherPath;
    this.adbPath = adbPath;
    this.portPicker = portPicker;
    this.logcatFilePathProvider = logcatFilePathProvider;
    this.logcatFiltersProvider = logcatFiltersProvider;
    this.adbControllerFactory = adbControllerFactory;
    this.communicatorBuilderProvider = communicatorBuilderProvider;
    this.apksToInstall = apksToInstall;
    this.systemApksToInstall = systemApksToInstall;
    this.testServicesApksToInstall = testServicesApksToInstall;
    this.installTestServices = installTestServices;
    this.subprocessLogDir = subprocessLogDir;
    this.openGlDriver = openGlDriver;
    this.kvmDevice = kvmDevice;
    this.enableDisplay = enableDisplay;
    this.enablePreverify = enablePreverify;
    this.enableConsoleAuth = enableConsoleAuth;
    this.enableGps = enableGps;
    this.networkType = networkType;
    this.longPressTimeout = longPressTimeout;
    this.vncServerPort = 0;
    this.adbServerPort = adbServerPort;
    this.adbEnvironment = adbEnvironment;
    this.numberOfCores = numberOfCores;
    this.deviceSerialNumber = deviceSerialNumber;
    this.initialLocale = initialLocale;
    this.initialIME = initialIME;
    this.extraCerts = extraCerts;
    this.dataPartitionSize = dataPartitionSize;
    this.grantRuntimePermissions = grantRuntimePermissions;
    this.emulatorStartupTimeout = emulatorStartupTimeout;
    this.environment = environment;
    this.enableDex2OatOnCloud = enableDex2OatOnCloud;
    this.simAccessRulesFile = simAccessRulesFile;
    this.useWaterfall = useWaterfall;
    checkState(!"".equals(emulatorLauncherPath), "No emulator launch script");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BrokeredDevice leaseDevice() {
    if (openGlDriver == OpenGlDriver.HOST) {
      // This is also checked in emulated_device.py which we end up calling,
      // but we like to fail faster that that.
      checkState(
          environment.getDisplay().isPresent(), "Host GPU OpenGL mode requires external $DISPLAY");
    }

    int adbPort = -1;
    if (adbServerPort.isPresent()) {
      adbPort = adbServerPort.get();
    } else {
      adbPort = portPicker.get();
    }

    int emulatorAdbPort = -1;
    if (deviceSerialNumber.startsWith("localhost:")) {
      emulatorAdbPort = Integer.valueOf(deviceSerialNumber.substring("localhost:".length()));
    } else {
      emulatorAdbPort = portPicker.get();
    }

    int emulatorTelnetPort = portPicker.get();
    String logcatFilePath = logcatFilePathProvider.get();
    List<LogcatFilter> logcatFilters = logcatFiltersProvider.get();
    String exportedMetadataPath = null;
    try {
      exportedMetadataPath = File.createTempFile("emulator_meta", ".pb").getPath();
    } catch (IOException ioe) {
      logger.log(Level.WARNING, "Couldn't create file for emulator metadata - not fatal.", ioe);
    }


    runEmulatorLaunchScript(
        ScriptAction.START,
        adbPort,
        emulatorAdbPort,
        emulatorTelnetPort,
        logcatFilePath,
        logcatFilters,
        exportedMetadataPath,
        vncServerPort);

    Map<String, Object> exportedMetadata = null;
    if (null != exportedMetadataPath) {
      exportedMetadata = readMetadata(exportedMetadataPath);
    } else {
      exportedMetadata = newHashMap();
    }
    exportedMetadata.put("test_output_logcat", logcatFilePath);

    BrokeredDevice device = new BrokeredDevice.Builder()
        .withAdbPath(adbPath)
        .withAdbServerPort(adbPort)
        .withAdbEnvironment(adbEnvironment)
        .withDeviceType(BrokeredDevice.DeviceType.EMULATOR)
        .withEmulatorAdbPort(emulatorAdbPort)
        .withEmulatorTelnetPort(emulatorTelnetPort)
        .withLogcatPath(logcatFilePath)
        .withLogcatFilters(logcatFilters)
        .withSerialId("localhost:" + emulatorAdbPort)
        .withVncServerPort(vncServerPort)
        .withAdbControllerFactory(adbControllerFactory)
        .withExportedProperties(exportedMetadata)
        .build();

    List<String> command =
        Lists.newArrayList("shell", "chmod", "666", "/dev/graphics/fb0", "/dev/fb0", "|| true");
    device.getAdbController().makeAdbCall(command.toArray(new String[command.size()]));

    /*
     * Certain files (e.g. /dev/alarm, which is the source for the system wall-time and timezone)
     * are owned by the System user, and we need to grant ourselves write permissions.
     * We can't run with the System UID instead, since the System user is not allowed to write
     * to external storage, as Androiddoesnot want it to own any file descriptors, which
     * could result on a leak.
     */
    command = Lists.newArrayList("shell", "chmod", "777", "/dev/alarm", "|| true");
    device.getAdbController().makeAdbCall(command.toArray(new String[command.size()]));
    device.getAdbController().unlockScreen();
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

  private Map<String, Object> readMetadata(String metadataPath) {
    FileInputStream inputStream = null;
    EmulatorMetaDataPb metadata = null;
    Map<String, Object> exportedData = newHashMap();
    try {
      inputStream = new FileInputStream(new File(metadataPath));
      metadata = EmulatorMetaDataPb.parseFrom(inputStream);
    } catch (IOException ioe) {
      logger.log(Level.WARNING, "Couldn't read emulator metadata from: " + metadataPath, ioe);
      return exportedData;
    } finally {
      closeQuietly(inputStream);
    }
    exportPerformanceProperties(metadata, exportedData);
    exportBootProperties(metadata, exportedData);
    exportSimpleProperties(metadata, exportedData);
    return exportedData;
  }

  private void exportPerformanceProperties(EmulatorMetaDataPb metadata,
      Map<String, Object> exportedData) {
    for (PerformanceDataPb perfData : metadata.getPerfDataList()) {
      for (TimerPb timer : perfData.getTimingList()) {
        exportedData.put(String.format("%s_%s_count", perfData.getActivityName(),
                                             timer.getName()), timer.getNumberOfStarts());
        exportedData.put(String.format("%s_%s_time_ms", perfData.getActivityName(),
                                             timer.getName()), timer.getAccumulatedTime());
      }
    }
  }

  private void exportBootProperties(EmulatorMetaDataPb metadata,
      Map<String, Object> exportedData) {
    List<PropertyPb> bootProperties = metadata.getBootPropertyList();
    List<String> textProperties = Lists.newArrayList();
    for (PropertyPb property : bootProperties) {
      textProperties.add(String.format("%s=%s", property.getName(), property.getValue()));
    }
    Collections.sort(textProperties);
    exportedData.put("boot_properties", Joiner.on(",").join(textProperties));
  }

  @SuppressWarnings("unchecked")
  private void exportSimpleProperties(EmulatorMetaDataPb metadata,
      Map<String, Object> exportedData) {
    for (Map.Entry<FieldDescriptor, Object> metadataField : metadata.getAllFields().entrySet()) {
      FieldDescriptor descriptor = metadataField.getKey();
      Object untypedObject = metadataField.getValue();
      if (descriptor.getName().startsWith("unused")) {
        continue;
      }
      if (!SIMPLE_TYPES.contains(descriptor.getType())) {
        continue;
      }
      if (!descriptor.isRepeated()) {
        exportedData.put(descriptor.getName(), untypedObject);
      } else {
        if (descriptor.getType() == FieldDescriptor.Type.STRING) {
          List<String> values = (List<String>) untypedObject;
          exportedData.put(descriptor.getName(), Joiner.on(",").join(values));
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void freeDevice(BrokeredDevice device) {
    checkNotNull(device, "device");
    device.getAdbController().adbConnect();
    List<String> command = Lists.newArrayList("shell", "chmod", "755", "/dev/alarm", "|| true");
    device.getAdbController().makeAdbCall(command.toArray(new String[command.size()]));
    device.getAdbController().unlockScreen();
    runEmulatorLaunchScript(
        ScriptAction.STOP,
        device.getAdbServerPort(),
        device.getEmulatorAdbPort(),
        device.getEmulatorTelnetPort(),
        device.getLogcatPath(),
        device.getLogcatFilters(),
        null,
        0);
  }


  @Override
  public Map<String, Object> getExportedProperties() {
    Map<String, Object> properties = newHashMap();

    List<String> allApksToInstall = apksToInstall;
    if (installTestServices) {
      allApksToInstall.addAll(testServicesApksToInstall);
    }
    properties.put("launch_script", emulatorLauncherPath);
    properties.put("apks_to_install", SPACE_SEP.join(allApksToInstall));
    properties.put("system_apks_to_install", SPACE_SEP.join(systemApksToInstall));
    properties.put("adb_path", adbPath);
    return properties;
  }

  private String makeUnifiedLauncherLogDir() {
    File launcherDir = new File(subprocessLogDir, "exec-" + LAUNCH_COUNT.incrementAndGet());
    launcherDir.mkdirs();
    return launcherDir.getPath();
  }

  private static boolean isAbsolutePath(String path) {
    return !path.isEmpty() && path.charAt(0) == '/';
  }
  // */

  private synchronized void runEmulatorLaunchScript(
      ScriptAction action,
      int adbServerPort,
      int emulatorAdbPort,
      int emulatorTelnetPort,
      String logcatFilePath,
      @Nullable List<LogcatFilter> logcatFilters,
      @Nullable String exportedMetadataPath,
      int vncServerPort) {

    List<String> allApksToInstall = apksToInstall;
    if (installTestServices) {
      allApksToInstall.addAll(testServicesApksToInstall);
    }
    List<String> command =
        Lists.newArrayList(
            emulatorLauncherPath,
            "--action=" + action.getCommandName(),
            "--adb_server_port=" + adbServerPort,
            "--adb_port=" + emulatorAdbPort,
            "--emulator_port=" + emulatorTelnetPort,
            "--logcat_path=" + logcatFilePath,
            "--apks=" + SPACE_SEP.join(allApksToInstall),
            "--system_apks=" + SPACE_SEP.join(systemApksToInstall),
            "--subprocess_log_dir=" + makeUnifiedLauncherLogDir(),
            "--nolaunch_in_seperate_session",
            "--start_vnc_on_port=" + vncServerPort,
            "--initial_locale=" + initialLocale,
            "--initial_ime=" + initialIME,
            "--kvm_device=" + kvmDevice,
            "--grant_runtime_permissions=" + grantRuntimePermissions);

    if (useWaterfall) {
      command.add("--use_h2o=" + useWaterfall);
      command.add("--adb_bin=" + adbPath);
    }

    if (logcatFilters != null && !logcatFilters.isEmpty()) {
      command.add("--logcat_filter='" + Joiner.on(" ").join(logcatFilters) + "'");
    }

    if (openGlDriver !=  OpenGlDriver.DEFAULT) {
      command.add("--open_gl_driver=" + openGlDriver.name().toLowerCase());
    }


    if (!isNullOrEmpty(simAccessRulesFile)) {
      command.add("--sim_access_rules_file=" + simAccessRulesFile);
    }

    command.add(enableDisplay ? "--enable_display" : "--noenable_display");
    command.add(enablePreverify ? "--preverify_apks" : "--nopreverify_apks");
    command.add(enableConsoleAuth ? "--enable_console_auth" : "--noenable_console_auth");
    command.add(enableGps ? "--enable_gps" : "--noenable_gps");

    if (null != exportedMetadataPath) {
      command.add("--export_launch_metadata_path=" + exportedMetadataPath);
    }

    command.add("--net_type=" + networkType.getType());

    if (!extraCerts.isEmpty()) {
      final String rootDir = environment.getRunfilesDir();

      List<String> absPathExtraCertsList =
          Lists.transform(
              extraCerts,
              new Function<String, String>() {
                @Override
                public String apply(String input) {
                  // The extra certs provided via data dependency in tests are provided under
                  // the "android_test_support" directory.
                  return isAbsolutePath(input) ? input : rootDir + "/android_test_support/" + input;
                  // */
                }
              });
      command.add("--extra_certs=" + COMMA_SEP.join(absPathExtraCertsList));
    }

    if (dataPartitionSize > 0) {
      command.add("--data_partition_size=" + dataPartitionSize);
    }

    if (longPressTimeout > 0) {
      command.add("--long_press_timeout=" + longPressTimeout);
    }

    if (numberOfCores.isPresent()) {
      command.add("--cores=" + numberOfCores.get());
    }

    logger.info("Running emulator launch script: " + command.toString());

    Map<String, String> env = newHashMap(environment.asMap());

    // This is probably unnecessary. Track removal in b/37347554
    if (env.containsKey("JAVA_RUNFILES") && !env.containsKey("DEVICE_RUNFILES")) {
      env.put("DEVICE_RUNFILES", env.get("JAVA_RUNFILES"));
    }

    try {
      // put the emulator's temp file in its own sandbox dir.
      File emulatorTemp = environment.createTempDir("launcher_tmp");
      env.put("TMPDIR", emulatorTemp.getPath());
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    LineProcessor<String> stderrProcessor = new LastLinesProcessor();
    int exitCode =
        communicatorBuilderProvider
            .get()
            .withArguments(command)
            .withTimeout(emulatorStartupTimeout, TimeUnit.SECONDS)
            .withEnvironment(env)
            .withStderrProcessor(stderrProcessor)
            .build()
            .communicate();

    checkState(0 == exitCode, "Bad exit code: %s. Command: %s.\n"
        + "=====Error begin:\n...\n%s\n=====Error end\n",
        exitCode, command, stderrProcessor.getResult());
  }

  private static class LastLinesProcessor implements LineProcessor<String> {

    private final EvictingQueue<String> lastLines = EvictingQueue.create(250);

    @Override
    public boolean processLine(String line) {
      lastLines.add(line.trim());
      return true;
    }

    @Override
    public String getResult() {
      return Joiner.on("\n").join(lastLines);
    }
  }
}
