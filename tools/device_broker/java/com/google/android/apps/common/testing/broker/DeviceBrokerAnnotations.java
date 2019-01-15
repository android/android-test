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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Binding Annotations used by the DeviceBroker.
 *
 */
final class DeviceBrokerAnnotations {
  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AdbPathFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AaptPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AaptPathFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AdbEnvironment {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AdbPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AdbServerPort {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ApksToInstall {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AssumeApksInstalled {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ConsoleAuth {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface NumberOfCores {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface DataPartitionSize {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface EnableGmsUsageReporting{}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface EnableGmsChannelOverride {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface EnableGps{}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface EmulatorStartupTimeoutFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ExtraCerts {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface BootstrapInstrumentationPackage {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface AdditionalTestPackages {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface IgnoreTestPackages {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface PrecompiledApksToInstall {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SystemApksToInstall {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface PreverifyApks {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ApksToInstallFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface PrecompiledApksToInstallFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SystemApksToInstallFlag {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface TestServicesApksToInstall {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface InstallBasicServices {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface InstallTestServices {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface DeviceSerialNumber {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface DexdumpPath {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface SimAccessRulesFile {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface SimAccessRulesFileFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ResourceDexdumpName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ResourceDexdumpPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface DexdumpPathFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface DataDir {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LauncherScriptFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LongPressTimeout {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface EmulateNetworkType{}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ExecutorLocation {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface EnableDisplay {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface VncRecorderName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface VncRecorderLocation {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface FlagEnableVncRecording {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface GeneratedLauncherScript {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface HttpProxy {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface InitialLocale {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface InitialIME {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface IsLocal {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LabDeviceProxyUrl {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LeaseTiming {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LogcatFilters {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface LogcatPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface AaptResourceName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ResourceAaptPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface OpenGl {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface KvmDevice {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface PackageName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ScanTargetPackageForTests {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SelectedBroker {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface OdoApkLocation {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface OdoApkResourceName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ServicesApkLocation {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface TestServicesApkLocation {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ServicesApkResourceName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface TestServicesApkResourceName {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SkipCoverageFilesCheck {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SpongeOutputDirectory {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface StaticAaptPathFlag {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SubprocessExecution {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface SubprocessLogDir {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface TestTempDir {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface TestTimeoutOverride {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface DeviceControllerPath {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface UniquePort {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface UserdataOverride {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ReuseApks {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface ServerMappings {}

  @BindingAnnotation @Target({FIELD, METHOD, PARAMETER}) @Retention(RUNTIME)
  public @interface GrantRuntimePermissions {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface Dex2OatOnCloudEnabled {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface UseWaterfall {}

  @BindingAnnotation
  @Target({FIELD, METHOD, PARAMETER})
  @Retention(RUNTIME)
  public @interface ExecReporterAnnotation {}

  private DeviceBrokerAnnotations() {}
}
