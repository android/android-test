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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.broker.AdbController.AdbControllerFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Represents a device that has been provisioned by a DeviceBroker.
 *
 */
public class BrokeredDevice {
  private static final String MISSING_PORT_INFO =
      "device was defaulted to emulator based on boot props but we do not have emulator port " +
      "info.";
  private static final Logger logging = Logger.getLogger(BrokeredDevice.class.getName());

  // Goldfish corresponds to the old kernel, ranchu the newer qemu2 kernel.
  private static final ImmutableList<String> EMULATOR_HARDWARE_STRINGS =
      ImmutableList.of("goldfish", "ranchu");

  private final AdbControllerFactory adbControllerFactory;
  private final DeviceType deviceType;
  private final ImmutableMap<String, String> environmentVariablesForAdb;
  private final String adbPath;
  private final ImmutableList<LogcatFilter> logcatFilters;
  private final String logcatPath;
  private final String serialId;
  private final int adbServerPort;
  private final int emulatorAdbPort;
  private final int emulatorTelnetPort;
  private final ImmutableMap<String, HostAndPort> accessibleAddressMap;
  private final Optional<Integer> vncServerPort;
  private final ImmutableMap<String, Object> exportedProperties;
  private final AtomicReference<ImmutableMap<String, String>> bootPropertiesRef =
      new AtomicReference<ImmutableMap<String, String>>(null);
  private final AtomicReference<ImmutableMap<String, String>> envVarsRef =
      new AtomicReference<ImmutableMap<String, String>>(null);
  private int apiVersion = -1;
  private String servicesPath;

  /**
   * Indicates the type of the device as known by the device broker.
   */
  public enum DeviceType {PHYSICAL, EMULATOR}


  /** A mapping of hermetic server names to the addresses the device will use to access them. */
  public ImmutableMap<String, HostAndPort> getAccessibleAddressMap() {
    return accessibleAddressMap;
  }

  /**
   * The minimal set of environmental variables to use when invoking ADB commands.
   */
  public ImmutableMap<String, String> getEnvironmentVariablesForAdb() {
    return environmentVariablesForAdb;
  }

  /**
   * Properties about this device that should be exported in the test invocation.
   *
   * These properties should be specific to this brokered device. The a test
   * suite can track all the brokered devices used by tests and export this
   * information to sponge once per test run.
   */
  public ImmutableMap<String, Object> getExportedProperties() {
    return exportedProperties;
  }

  /**
   * The management port for the emulator.
   *
   * Note this method only works for emulated devices.
   * If you're using this to invoke ADB, please consider using
   * {@link  #getEnvironmentVariablesForAdb} instead.
   *
   * @return the telnet management port for the emulator
   * @throws IllegalStateException if the device is not emulated.
   */
  public int getEmulatorTelnetPort() {
    checkState(getDeviceType() == DeviceType.EMULATOR,
        "Attempted to get telnet port on a non-emulated device.");
    checkState(isPort(emulatorTelnetPort), MISSING_PORT_INFO);
    return emulatorTelnetPort;
  }

  /**
   * The port the emulator is using to talk to ADB.
   *
   * Note this method only works for emulated devices.
   * If you're using this to invoke ADB, please consider using
   * {@link  #getEnvironmentVariablesForAdb} instead.
   *
   * @return the port the emulator uses for adb connections.
   * @throws IllegalStateException if the device is not emulated.
   */
  public int getEmulatorAdbPort() {
    checkState(deviceType == DeviceType.EMULATOR,
        "Attempted to get adb port on a non-emulated device.");
    checkState(isPort(emulatorAdbPort), MISSING_PORT_INFO);

    return emulatorAdbPort;
  }

  /**
   * The type of device.
   *
   * If you're using this to invoke ADB, please consider using
   * {@link  #getEnvironmentVariablesForAdb} instead.
   *
   * @return the type of this device.
   */
  public DeviceType getDeviceType() {
    if (null == deviceType) {
      if (getApiVersion() < 8) {
        String product = getDeviceBootProperties().get("ro.product.name");
        if (product.toLowerCase().contains("sdk")) {
          return DeviceType.EMULATOR;
        } else {
          return DeviceType.PHYSICAL;
        }
      } else {
        String hardware = getDeviceBootProperties().get("ro.hardware");
        if (EMULATOR_HARDWARE_STRINGS.contains(hardware.toLowerCase())) {
          return DeviceType.EMULATOR;
        } else {
          return DeviceType.PHYSICAL;
        }
      }
    }

    return deviceType;
  }

  /**
   * The ADB assigned serial-id.
   *
   * All connections to ADB should reference this id.
   * The ADB server may be hosting multiple devices.
   *
   * Treat this value as opaque. Do not assume that if it doesnt start with
   * emulator-555X that your not talking to an emulated device.
   *
   * @return the serial id of the device
   */
  public String getSerialId() {
    return serialId;
  }

  /**
   * The server port adb is running on.
   *
   * If you're using this to invoke ADB, please consider using
   * {@link  #getEnvironmentVariablesForAdb} instead.
   *
   * @return the port the adb server is listening on.
   */
  public int getAdbServerPort() {
    return adbServerPort;
  }

  /**
   * The server vnc Server is running on.
   *
   * @return the port the vnc server is listening on.
   */
  public Optional<Integer> getVncServerPort() {
    return vncServerPort;
  }

  /**
   * A path to the adb program.
   *
   * Please use this adb executable to issue adb commands. It is guaranteed to
   * be present whereever the test is run. It will also log commands and their
   * output for debugging purposes.
   */
  public String getAdbPath() {
    return adbPath;
  }

  /**
   * A path to the adb services apk
   *
   * <p>Not guaranteed to be set at runtime
   */
  public String getServicesPath() {
    return servicesPath;
  }

  /** Sets the location of the services apk path */
  public String setServicesPath(String servicesPath) {
    return this.servicesPath = servicesPath;
  }

  /**
   * A path to a logcat file containing logs of the entire test run.
   */
  public String getLogcatPath() {
    return logcatPath;
  }

  /** The filters applied to logcat. */
  public ImmutableList<LogcatFilter> getLogcatFilters() {
    return logcatFilters;
  }

  /**
   * Provides access to common adb functions against this device.
   */
  public AdbController getAdbController() {
    return adbControllerFactory.create(this);
  }

  /**
   * Provides a map of boot properties for the device.
   * This is roughly the equivalent of adb shell getprop | grep ro
   */
  public ImmutableMap<String, String> getDeviceBootProperties() {
    if (null == bootPropertiesRef.get()) {
      ImmutableMap.Builder<String, String> bootProperties = ImmutableMap.builder();

      for (Map.Entry<String, String> prop : getAdbController().getDeviceProperties().entrySet()) {
        if (prop.getKey().startsWith("ro.")) {
          bootProperties.put(prop.getKey(), prop.getValue());
        }
      }
      bootPropertiesRef.set(bootProperties.build());
    }

    return bootPropertiesRef.get();
  }

  /**
   * The api level of the device.
   *
   * @return the api level of the brokered device.
   */
  public int getApiVersion() {
    if (-1 == apiVersion) {
      String stringVersion = getDeviceBootProperties().get("ro.build.version.sdk");

      if (null != stringVersion) {
        try {
          apiVersion = Integer.parseInt(stringVersion.trim());
        } catch (NumberFormatException nfe) {
          logging.warning(String.format(
              "Device property 'ro.build.version.sdk' (value=%s) could not be parsed into int.",
              stringVersion));
        }
      }
    }
    return apiVersion;
  }


  /**
   * Provides a map of environment variables accessable to processes on the device.
   */
  public ImmutableMap<String, String> getShellVariables() {
    ImmutableMap<String, String> envVars = envVarsRef.get();
    if (null == envVars) {
      envVars = ImmutableMap.copyOf(getAdbController().deviceShellVariables());
      envVarsRef.set(envVars);
    }
    return envVars;
  }

  private BrokeredDevice(Builder builder) {
    this.serialId = checkNotNull(builder.serialId, "Serial Id");
    this.adbPath = checkNotNull(builder.adbPath, "adb path");
    this.servicesPath = builder.servicesPath;
    this.logcatPath = checkNotNull(builder.logcatPath, "logcat path");
    checkArgument(
        isPort(builder.adbServerPort),
        "Invalid adb server port: %s",
        builder.adbServerPort);
    this.adbServerPort = builder.adbServerPort;

    Map<String, String> adbEnvironment = Maps.newHashMap();
    adbEnvironment.put("ANDROID_ADB_SERVER_PORT", String.valueOf(adbServerPort));

    checkState(null != builder.deviceType || builder.defaultDeviceType, "device type is null "
        + "and we were told not to default.");
    if (null == builder.deviceType) {
      if (isPort(builder.emulatorAdbPort)) {
        adbEnvironment.put("ANDROID_EMULATOR_ADB_PORT", String.valueOf(builder.emulatorAdbPort));
      }
      if (isPort(builder.emulatorTelnetPort)) {
        adbEnvironment.put("ANDROID_EMULATOR_CONSOLE_PORT",
            String.valueOf(builder.emulatorTelnetPort));
      }
    } else if (DeviceType.EMULATOR == builder.deviceType) {
      checkArgument(
          isPort(builder.emulatorAdbPort),
          "Emulated devices must have a valid adb port: %s",
          builder.emulatorAdbPort);
      checkArgument(
          isPort(builder.emulatorTelnetPort),
          "Emulated devices must have a telnet port: %s",
          builder.emulatorTelnetPort);
      adbEnvironment.put("ANDROID_EMULATOR_CONSOLE_PORT",
          String.valueOf(builder.emulatorTelnetPort));
      adbEnvironment.put("ANDROID_EMULATOR_ADB_PORT", String.valueOf(builder.emulatorAdbPort));
    } else {
      checkArgument(0 == builder.emulatorAdbPort && 0 == builder.emulatorTelnetPort,
          "Real devices must not have emulator ports.");
    }
    adbEnvironment.putAll(builder.adbEnvironment);

    this.deviceType = builder.deviceType;
    this.emulatorAdbPort = builder.emulatorAdbPort;
    this.emulatorTelnetPort = builder.emulatorTelnetPort;
    this.vncServerPort = builder.vncServerPort == 0
        ? Optional.<Integer>absent() : Optional.<Integer>of(builder.vncServerPort);
    this.environmentVariablesForAdb = ImmutableMap.copyOf(adbEnvironment);
    this.logcatFilters = checkNotNull(builder.logcatFilters, "logcat filters");
    this.adbControllerFactory = checkNotNull(builder.adbControllerFactory,
        "adb controller factory");
    this.exportedProperties = ImmutableMap.copyOf(builder.exportedProperties);
    this.accessibleAddressMap = ImmutableMap.copyOf(builder.accessibleAddressMap);
  }

  private static boolean isPort(int port) {
    return 0 < port && port <= 65535;
  }

  static class Builder {
    private String serialId;
    private int adbServerPort;
    private String adbPath;
    private String servicesPath;
    private String logcatPath;
    private DeviceType deviceType;
    private boolean defaultDeviceType;
    private int emulatorTelnetPort;
    private int emulatorAdbPort;
    private int vncServerPort;
    private ImmutableList<LogcatFilter> logcatFilters = LogcatFilter.fromStringList("*:I");
    private AdbControllerFactory adbControllerFactory;
    private Map<String, Object> exportedProperties = Maps.newHashMap();
    private Map<String, String> adbEnvironment = Maps.newHashMap();
    private Map<String, HostAndPort> accessibleAddressMap = Maps.newHashMap();

    public BrokeredDevice build() {
      return new BrokeredDevice(this);
    }

    public Builder withAdbControllerFactory(AdbControllerFactory adbControllerFactory) {
      this.adbControllerFactory = adbControllerFactory;
      return this;
    }

    public Builder withLogcatPath(String logcatPath) {
      this.logcatPath = logcatPath;
      return this;
    }

    public Builder withAdbPath(String adbPath) {
      this.adbPath = adbPath;
      return this;
    }

    public Builder withServicesPath(String servicesPath) {
      this.servicesPath = servicesPath;
      return this;
    }

    /**
     * Specifies the logcat filters to apply to the device logs.
     *
     * @param filters A comma-separated list of logcat filter specifications.
     */
    public Builder withLogcatFilters(String filters) {
      this.logcatFilters = LogcatFilter.fromStringList(filters);
      return this;
    }

    public Builder withLogcatFilters(List<LogcatFilter> filters) {
      this.logcatFilters = ImmutableList.copyOf(filters);
      return this;
    }

    public Builder withSerialId(String serialId) {
      this.serialId = serialId;
      return this;
    }

    public Builder withAdbServerPort(int serverPort) {
      this.adbServerPort = serverPort;
      return this;
    }

    public Builder withEmulatorAdbPort(int emulatorAdbPort) {
      this.emulatorAdbPort = emulatorAdbPort;
      return this;
    }

    public Builder withEmulatorTelnetPort(int emulatorTelnetPort) {
      this.emulatorTelnetPort = emulatorTelnetPort;
      return this;
    }

    public Builder withVncServerPort(int vncServerPort) {
      this.vncServerPort = vncServerPort;
      return this;
    }

    public Builder withDeviceType(DeviceType type) {
      this.defaultDeviceType = false;
      this.deviceType = type;
      return this;
    }

    public Builder defaultDeviceType() {
      this.defaultDeviceType = true;
      this.deviceType = null;
      return this;
    }

    public Builder withExportedProperties(Map<String, Object> exportedProperties) {
      this.exportedProperties = checkNotNull(exportedProperties);
      return this;
    }

    public Builder withAdbEnvironment(Map<String, String> adbEnvironment) {
      this.adbEnvironment = checkNotNull(adbEnvironment);
      return this;
    }

    public Builder withAccessibleAddressMap(Map<String, HostAndPort> accessibleAddressMap) {
      this.accessibleAddressMap = checkNotNull(accessibleAddressMap);
      return this;
    }
  }
}
