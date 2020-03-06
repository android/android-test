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
import static java.util.Arrays.asList;

import com.google.android.apps.common.testing.broker.AbstractRegexpLineProcessor.RegexpPresentProcessor;
import com.google.android.apps.common.testing.broker.AbstractRegexpLineProcessor.RegexpProcessorBuilder;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AdditionalTestPackages;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AssumeApksInstalled;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.BootstrapInstrumentationPackage;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.IgnoreTestPackages;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.InstallBasicServices;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ScanTargetPackageForTests;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SkipCoverageFilesCheck;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestTimeoutOverride;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.UniquePort;
import com.google.android.apps.common.testing.broker.LogcatFilter.Level;
import com.google.android.apps.common.testing.broker.LogcatStreamer.Buffer;
import com.google.android.apps.common.testing.broker.LogcatStreamer.OutputFormat;
import com.google.android.apps.common.testing.broker.SubprocessCommunicator.Builder;
import com.google.android.apps.common.testing.broker.shell.ShellUtils;
import com.google.android.apps.common.testing.proto.TestInfo.TestSuitePb;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Provides control over brokered devices via ADB.
 *
 */
public class AdbController {

  /** The names that GoogleInstrumentationTestRunner goes by in AndroidManifest.xml files. */
  private static final ImmutableList<String> GOOGLE_INSTRUMENTATION_NAMES =
      ImmutableList.of(
          ".GoogleInstrumentationTestRunner",
          "com.google.android.apps.common.testing.testrunner.GoogleInstrumentationTestRunner");

  /**
   * The list of supported {@link Instrumentation} classes.
   *
   * <p>The ordering is important and indicates preference. For example, if both AndroidJUnitRunner
   * and GoogleInstrumentationTestRunner (G3ITR) are present then G3ITR is preferred. Hence, G3ITR
   * is listed before AndroidJUnitRunner.
   */
  static final ImmutableList<String> SUPPORTED_INSTRUMENTATION_NAMES =
      ImmutableList.<String>builder()
          .addAll(GOOGLE_INSTRUMENTATION_NAMES)
          .add("com.google.android.apps.common.testing.testrunner.GoogleInstrumentationTestRunner")
          .add(".GoogleInstrumentationTestRunner")
          .add("androidx.test.runner.AndroidJUnitRunner")
          .add("android.support.test.runner.AndroidJUnitRunner")
          .add(".AndroidJUnitRunner")
          .build();

  /**
   * Orders {@link Instrumentation} instances by preference, as defined by the ordering in {@link
   * #SUPPORTED_INSTRUMENTATION_NAMES}.
   */
  private static final Comparator<Instrumentation> INSTRUMENTATION_PREFERENCE =
      // Reversing SUPPORTED_INSTRUMENTATION_NAMES and then using indexOf ensures that the first
      // element in the list is asigned the highest value, but that entries at the end of the list,
      // or not in the list at all are assigned the lowest value. We then reverse the ordering to
      // rank the values in descending order.
      Comparator.comparing(
              Instrumentation::getInstrumentationClass,
              Comparator.comparingInt(Lists.reverse(SUPPORTED_INSTRUMENTATION_NAMES)::indexOf))
          .reversed();

  private static final ImmutableList<String> ORCHESTRATOR_ENABLED_RUNNERS =
      ImmutableList.of(
          "com.google.android.apps.common.testing.testrunner.GoogleInstrumentationTestRunner",
          "androidx.test.runner.AndroidJUnitRunner");


  private static final String APK_MISSING_INSTRUMENTATION =
      "Apk associated with package %s does not contain class %s. Firstly, make sure that you "
          + "are using the correct --instrumentation_filter (it should be set to the path of your "
          + "target app) and that the apk under test includes coverage support. If your app is "
          + "being proguarded, make sure that your proguard spec includes %s.";

  private static final String PROGUARD_KEEP_EMMA = "'-keep %s com.vladium.** {*;}'";
  private static final String PROGUARD_KEEP_JACOCO = "'-keep %s org.jacoco.** {*;}'";

  private static final Pattern PATH_PATTERN = Pattern.compile("^package\\:(.*)");

  private static final Logger logger =
      Logger.getLogger(AdbController.class.getName());

  private static final String ORCHESTRATOR_COMPONENT_NAME =
      "androidx.test.orchestrator/androidx.test.orchestrator.AndroidTestOrchestrator";

  private static final String IO_ERROR_MSG =
      "List of files from %s could not be retrieved. Most likely and I/O error has occured.";

  public static final String ANDROID_TEST_SERVICES_PACKAGE = "androidx.test.services";

  private final Provider<Integer> portPicker;
  private final Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider;
  private final Provider<SimpleLineListProcessor> adbLineListProvider;
  private final RegexpProcessorBuilder regexpProcessorBuilder;
  private final Provider<AdbInstrumentationListProcessor> instrumentationListProcessorProvider;
  private final BrokeredDevice device;
  private boolean isConnectedToAdb = false;
  private final Map<String, Object> exportedProperties = Maps.newHashMap();
  private final Provider<ShellVariableProcessor> shellVariableProcessorProvider;
  private final Provider<AndroidPropertyProcessor> propertyProcessorProvider;
  private final TestInfoRepository testRepo;
  private int defaultTimeout = 120;
  private int shortDefaultTimeout = 20;
  private final Optional<Integer> testTimeoutOverride;
  private final boolean scanTargetPackageForTests;
  private final boolean skipCoverageFilesCheck;
  private final boolean installBasicServices;
  private final List<String> assumeApksInstalled;
  private final InstrumentationRepository instrumentationRepo;

  @VisibleForTesting
  AdbController(
      Provider<Integer> portPicker,
      Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider,
      BrokeredDevice device,
      Provider<SimpleLineListProcessor> adbLineListProvider,
      Provider<AdbInstrumentationListProcessor> instrumentationListProcessorProvider,
      Provider<ShellVariableProcessor> shellVariableProcessorProvider,
      Provider<AndroidPropertyProcessor> propertyProcessorProvider,
      RegexpProcessorBuilder regexpProcessorBuilder,
      TestInfoRepository testRepo,
      Optional<Integer> testTimeoutOverride,
      boolean scanTargetPackageForTests,
      boolean skipCoverageFilesCheck,
      boolean installBasicServices,
      List<String> assumeApksInstalled,
      List<String> additionalTestPackages,
      List<String> ignoreTestPackages,
      String bootstrapInstrumentationPackage) {
    this.portPicker = checkNotNull(portPicker);
    this.communicatorBuilderProvider = checkNotNull(communicatorBuilderProvider);
    this.device = checkNotNull(device);
    this.instrumentationListProcessorProvider = checkNotNull(instrumentationListProcessorProvider);
    this.adbLineListProvider = checkNotNull(adbLineListProvider);
    this.shellVariableProcessorProvider = checkNotNull(shellVariableProcessorProvider);
    this.propertyProcessorProvider = checkNotNull(propertyProcessorProvider);
    this.regexpProcessorBuilder = checkNotNull(regexpProcessorBuilder);
    this.testRepo = testRepo;
    this.testTimeoutOverride = checkNotNull(testTimeoutOverride);
    this.scanTargetPackageForTests = scanTargetPackageForTests;
    this.skipCoverageFilesCheck = skipCoverageFilesCheck;
    this.installBasicServices = installBasicServices;
    this.assumeApksInstalled = assumeApksInstalled;
    this.instrumentationRepo =
        InstrumentationRepository.builder()
            .withInstrumentationsProvider(
                (Provider<List<Instrumentation>>) this::listInstrumentations)
            .withAdditionalTestPackages(additionalTestPackages)
            .withIgnoreTestPackages(ignoreTestPackages)
            .withBootstrapInstrumentationPackage(bootstrapInstrumentationPackage)
            .build();
  }

  /**
   * A map containing information about actions done by this controller for export to sponge.
   *
   * A test can export these properties to sponge after performing its work. These properties
   * can also include log output.
   */
  public ImmutableMap<String, Object> getExportedProperties() {
    return ImmutableMap.copyOf(exportedProperties);
  }

  /**
   * Set the default timeout (in seconds).
   *
   * If an ADB command takes longer than the time specified here, it is considered to have failed.
   */
  public void setDefaultTimeout(int timeoutInSeconds) {
    checkArgument(timeoutInSeconds > 0);
    defaultTimeout = timeoutInSeconds;
  }

  /**
   * Returns the default timeout (in seconds) before a task is considered to have failed.
   */
  public int getDefaultTimeout() {
    return defaultTimeout;
  }

  /**
   * Returns the default timeout (in seconds) before a short-running task is considered to have
   * failed.
   */
  public int getShortDefaultTimeout() {
    return shortDefaultTimeout;
  }

  /**
   * Install the given apk on the device with -g if api level > 23 and
   * grantRuntimePermissions is true.
   */
  public void installApk(String apkPath, boolean grantRuntimePermissions) {
    installApk(apkPath, getAdbInstallArgs(), grantRuntimePermissions);
  }

  /**
   * Install the given apk on the device.
   */
  public void installApk(String apkPath) {
    installApk(apkPath, getAdbInstallArgs(), true /* grantRuntimePermissions */);
  }

  /**
   * Install given apk on the device with specified arguments.
   */
  public void installApk(String apkPath, List<String> additionalArgs,
    boolean grantRuntimePermissions) {
    checkNotNull(apkPath);
    checkArgument(new File(apkPath).exists(), "apk file does not exist");

    LineProcessor<List<String>> stdoutProcessor = adbLineListProvider.get();
    SubprocessCommunicator.Builder builder =
        communicatorBuilderProvider.get().withStdoutProcessor(stdoutProcessor)
        .withStderrProcessor(stdoutProcessor);

    List<String> adbArgs = new ArrayList<String>();
    adbArgs.add("install");
    adbArgs.addAll(additionalArgs);
    if (device.getApiVersion() >= 23 && grantRuntimePermissions) {
      adbArgs.add("-g");
    }
    adbArgs.add(apkPath);

    int maxInstallTimeSeconds = getDefaultTimeout();
    if (device.getApiVersion() > 19) {
      // ART (dex2oat)
      File apkFile = new File(apkPath);
      long apkSize = apkFile.length();
      if (apkSize > (2 << 24)) {
        maxInstallTimeSeconds *= 3;
        logger.info(
            String.format(
                "%s: is large (%s bytes) - setting install timeout to %s to account for dex2oat",
                apkPath, apkSize, maxInstallTimeSeconds));
      }
    }

    List<String> prefixedArgs = prefixArgsWithDeviceSerial(adbArgs.toArray(new String[0]));
    try {
      makeCheckedCall(builder, prefixedArgs, maxInstallTimeSeconds);
    } catch (IllegalStateException e) {
      throw new RuntimeException(Joiner.on("\n").join(stdoutProcessor.getResult()), e);
    }
    checkAdbInstallResult(stdoutProcessor);
  }

  private List<String> getAdbInstallArgs() {
    if (device.getApiVersion() > 16) {  // android.os.Build.JELLY_BEAN.
      return ImmutableList.of("-d", "-r");
    }
    return ImmutableList.of("-r");
  }

  private void checkAdbInstallResult(LineProcessor<List<String>> processor) {
    if (!isTextPresentInProcessorResult(processor, "Success")) {
      throw new RuntimeException("adb install failed. Output: " + processor.getResult());
    }
  }

  private boolean isTextPresentInProcessorResult(
      LineProcessor<List<String>> processor, String text) {
    for (String line : processor.getResult()) {
      if (line.contains(text)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Installs the application with the specified package name and apk path.
   *
   * <p>Before installing, it checks whether the MD5 checksum of the apk matches
   * with the one from the apk that was last installed on the device.
   * If the app wasn't previously installed or the checksums don't match, it
   * installs the apk.
   */
  public void installApkIfNecessary(String packageName, String apkPath) {
    installApkIfNecessary(packageName, apkPath, getAdbInstallArgs(),
        true /* grantRuntimePermissions*/);
  }

  /**
   * Installs the application with the specified package name and apk path.
   */
  public void installApkIfNecessary(String packageName, String apkPath,
      boolean grantRuntimePermissions) {
    installApkIfNecessary(packageName, apkPath, getAdbInstallArgs(), grantRuntimePermissions);
  }

  /**
   * Installs the application with the specified package name, apk path, and arguments.
   */
  public void installApkIfNecessary(String packageName, String apkPath,
      List<String> additionalArgs, boolean grantRuntimePermissions) {
    if (assumeApksInstalled.contains(packageName) || assumeApksInstalled.contains("all")) {
      logger.info("Skip installation of " + packageName);
      return;
    }
    checkNotNull(packageName);
    checkNotNull(apkPath);
    checkArgument(new File(apkPath).exists(), "apk file does not exist");

    try {
      if (!isApkAlreadyInstalled(apkPath, packageName)) {
        try {
          installApk(apkPath, additionalArgs, grantRuntimePermissions);
        } catch (Throwable t) {
          t.printStackTrace();
          uninstallApp(packageName);
          installApk(apkPath, additionalArgs, grantRuntimePermissions);
        }
      } else {
        logger.info("Local and device apk hashes for [" + packageName
                    + "] are the same, will skip install.");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Unlocks the device screen if it is locked.
   */
  public void unlockScreen() {
    // Send menu key to unlock the device.
    makeAdbCall("shell", "input", "keyevent", "82", "&&", "input", "keyevent", "4");
  }

  /**
   * Returns {@code true} if uninstall of the given app from the device was successful.
   */
  public boolean uninstallApp(String appPackageName) {
    checkNotNull(appPackageName);

    LineProcessor<List<String>> stdoutProcessor = adbLineListProvider.get();
    SubprocessCommunicator.Builder builder =
        communicatorBuilderProvider.get().withStdoutProcessor(stdoutProcessor);

    makeAdbCall(builder, getDefaultTimeout(), "uninstall", appPackageName);

    return isTextPresentInProcessorResult(stdoutProcessor, "Success");
  }

  /**
   * Lists all the instrumentations installed on the device.
   *
   * @return A (possibly empty) list of all instrumentations, ordered by preference.
   */
  public List<Instrumentation> listInstrumentations() {
    AdbInstrumentationListProcessor stdoutProcessor = instrumentationListProcessorProvider.get();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    builder.withStdoutProcessor(stdoutProcessor);

    makeAdbCall(builder, getDefaultTimeout(), "shell", "pm", "list", "instrumentation");
    List<Instrumentation> result =
        stdoutProcessor
            .getResult()
            .stream()
            .sorted(INSTRUMENTATION_PREFERENCE)
            .collect(Collectors.toList());
    return result;
  }

  /**
   * Starts a particular instrumentation.
   *
   *  No args are passed to shell am instrumentation and outputs are not parsed.
   *
   * If when richer instrumentation invokation is needed, new entry points should be added.
   *
   * @param instrumentation the non-null instrumentation to run
   */
  public void startInstrumentation(Instrumentation instrumentation) {
    startInstrumentation(instrumentation, Maps.<String, String>newHashMap(), false);
  }

  void startInstrumentation(Instrumentation instrumentation, Map<String, String> instrArgs,
      boolean wait) {
    checkNotNull(instrumentation, "instrumentation");
    List<String> args = Lists.newArrayList(
        "shell",
        "am",
        "instrument");
    if (wait) {
      args.add("-w");
    }
    for (Map.Entry<String, String> arg : instrArgs.entrySet()) {
      args.add("-e");
      args.add(arg.getKey());
      args.add(arg.getValue());
    }
    args.add(String.format("%s/%s",
          instrumentation.getAndroidPackage(), instrumentation.getInstrumentationClass()));
    makeAdbCall(args.toArray(new String[args.size()]));
  }

  /**
   * Launches an activity thru the activity manager.
   *
   * The map of extras are passed to the activity in the natural order of map iteration.
   *
   * @param action the action to send in the launching intent
   * @param activityName the android_component/activity_class_name
   * @param extras a map of key-value pairs that specify extra values to pass to the activity
   * @param waitForLaunch block till activity is launched.
   */
  public void startActivity(ActivityAction action, String activityName, Map<String, String> extras,
      boolean waitForLaunch) {
    checkNotNull(action);
    checkNotNull(activityName);
    checkNotNull(extras);
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    LineProcessor<Boolean> stderrProcessor = getStartActivityErrorPresentProcessor();
    LineProcessor<Boolean> stdoutProcessor = getStartActivityErrorPresentProcessor();
    builder.withStdoutProcessor(stdoutProcessor);
    builder.withStderrProcessor(stderrProcessor);

    List<String> adbArgs = Lists.newArrayList("shell", "am", "start");
    if (waitForLaunch) {
      adbArgs.add("-W");
    }

    adbArgs.addAll(Lists.newArrayList("-a", action.getActionName(), "-n", activityName));
    if (extras != null) {
      for (Map.Entry<String, String> extra : extras.entrySet()) {
        adbArgs.add("-e");
        adbArgs.add(extra.getKey());
        adbArgs.add(extra.getValue());
      }
    }

    makeAdbCall(builder, getDefaultTimeout(), adbArgs.toArray(new String[0]));
    String checkLogs = "Could not start activity, check adb logs.";
    checkState(!stdoutProcessor.getResult(), checkLogs);
    checkState(!stderrProcessor.getResult(), checkLogs);
  }

  /**
   * Launches an activity thru the activity manager.
   *
   * @param action the action to send in the launching intent
   * @param activityName the android_component/activity_class_name
   * @param waitForLaunch block till activity is launched.
   */
  public void startActivity(ActivityAction action, String activityName, boolean waitForLaunch) {
    startActivity(action, activityName, Collections.<String, String>emptyMap(), waitForLaunch);
  }

  public void broadcastAction(String action, String flags, String packageName,
      Map<String, String> extras) {
    checkNotNull(action);
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    LineProcessor<Boolean> stderrProcessor = getStartActivityErrorPresentProcessor();
    LineProcessor<Boolean> stdoutProcessor = getStartActivityErrorPresentProcessor();
    builder.withStdoutProcessor(stdoutProcessor);
    builder.withStderrProcessor(stderrProcessor);

    List<String> adbArgs = Lists.newArrayList("shell", "am", "broadcast");

    adbArgs.add("-a");
    adbArgs.add(action);
    if (device.getApiVersion() > 16) {
      adbArgs.add("-f");
      adbArgs.add(flags);
    }
    for (Map.Entry<String, String> extra : extras.entrySet()) {
      adbArgs.add("-e");
      adbArgs.add(extra.getKey());
      adbArgs.add(extra.getValue());
    }
    // explicitly send this to our package. (only supported after API level 10)
    if (device.getApiVersion() > 10) {
      adbArgs.add(packageName);
    }
    logger.info("Broadcast cmdline: " + adbArgs);
    makeAdbCall(builder, getDefaultTimeout(), adbArgs.toArray(new String[0]));
    String checkLogs = "Could not broadcast, check adb logs.";
    checkState(!stdoutProcessor.getResult(), checkLogs);
    checkState(!stderrProcessor.getResult(), checkLogs);
  }

  /**
   * Clears all the package data for both the test apk and the app under test.
   */
  public void clearPackageData(Instrumentation instrumentation) {
    clearPackageData(instrumentation.getAndroidPackage());
    clearPackageData(instrumentation.getTargetPackage());
  }

  /**
   * Clear all package data for the package given.
   *
   * @param thePackage package name with data to be cleared
   */
  public void clearPackageData(String thePackage) {
    logger.info(
        String.format(
            "Clearing app (%s) data and the granted runtime permissions for api level >= 23",
            thePackage));
    makeAdbCall("shell", "pm", "clear", thePackage);
  }

  /**
   * Remove only the shared preferences data for both the test apk and the app under test
   * from the device.
   */
  public void clearPackageDataLite(Instrumentation instrumentation) {
    String dataStorageDir = device.getShellVariables().get("ANDROID_DATA");
    checkState(null != dataStorageDir, "No data storage? Vars: %s",
        device.getShellVariables());
    String externalStorageDir = device.getShellVariables().get("EXTERNAL_STORAGE");
    checkState(null != externalStorageDir, "No external storage? Vars: %s",
        device.getShellVariables());
    String androidStorageDir = device.getShellVariables().get("ANDROID_STORAGE");
    checkState(null != androidStorageDir, "No android storage? Vars: %s",
        device.getShellVariables());

    // clear selected directories from app's internal storage
    clearDataDirs(instrumentation,
        dataStorageDir + "/data/",  // for apps targeting < api 23
        dataStorageDir + "/data/user/0/"); // for apps targeting api 23+

    // clear all external storage
    clearExternalStorageDirs(instrumentation,
        externalStorageDir + "/Android/data/",  // for apps targeting api < 23
        androidStorageDir + "/emulated/0/Android/data/", // for apps targeting api 23+
        androidStorageDir + "/1AEF-1A1E/Android/data/"); // for apps targeting api 23+
  }

  private void clearDataDirs(Instrumentation instrumentation, String... baseDirs) {
     final String[] subDirsToDelete = new String[] {"shared_prefs", "databases", "cache", "files",
        "app_dxmaker_cache", "no_backup"};
     for (String baseDir : baseDirs) {
       for (String subDir : subDirsToDelete) {
         makeAdbCall("shell", "rm", "-rf", baseDir + instrumentation.getAndroidPackage() + "/"
             + subDir);
         makeAdbCall("shell", "rm", "-rf", baseDir + instrumentation.getTargetPackage() + "/"
            + subDir);
       }
     }
  }

  private void clearExternalStorageDirs(Instrumentation instrumentation, String... dirs) {
    for (String dir : dirs) {
      makeAdbCall("shell", "rm", "-rf", dir + instrumentation.getAndroidPackage());
      makeAdbCall("shell", "rm", "-rf", dir + instrumentation.getTargetPackage());
    }
  }

  public void resetPermissions(Instrumentation instrumentation) {
    logger.info("Resetting app permissions for API >= 23");
    makeAdbCall("shell", "pm", "reset-permissions", instrumentation.getAndroidPackage());
    makeAdbCall("shell", "pm", "reset-permissions", instrumentation.getTargetPackage());
  }

  @VisibleForTesting
  LineProcessor<Boolean> getStartActivityErrorPresentProcessor() {
    return new RegexpPresentProcessor(Pattern.compile("Error:.*"));
  }

  /**
   * Forwards a port on the device to a free port on the host.
   *
   * @param devicePort the port on the device you wish to forward
   * @return the host port that is forwarded to that device port
   */
  public int forwardToFreePort(int devicePort) {
    checkArgument(0 < devicePort && devicePort <= 65535, "Invalid port: %s", devicePort);
    int hostPort = portPicker.get();
    makeAdbCall("forward", "tcp:" + hostPort, "tcp:" + devicePort);
    return hostPort;
  }

  /**
   * Executes getprop and returns a Map of properties found on the device.
   *
   * <p>Note: if you want boot properties, please use BrokeredDevice.getDeviceBootProperties() these
   * are immutable and therefore efficiently cached on the domain object.
   */
  public ImmutableMap<String, String> getDeviceProperties() {
    AndroidPropertyProcessor propertyProcessor = propertyProcessorProvider.get();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    builder.withStdoutProcessor(propertyProcessor);

    makeAdbCall(builder, getDefaultTimeout(), "shell", "getprop");
    return propertyProcessor.getResult();
  }

  String getDeviceProperty(String name) {
    final List<String> getprop = makeLineOutputProcessorAdbCall("shell", "getprop", name);
    if (getprop.size() > 0) {
      return getprop.get(0);
    } else {
      return "";
    }
  }

  /** Executes setprop for the specified key/value pair. */
  public void setDeviceProperty(String name, String value) {
    if (value.isEmpty()) {
      value = "''";
    }
    makeLineOutputProcessorAdbCall("shell", "setprop", name, value);
  }

  /**
   * Creates an asynchronous logcat stream.
   *
   * The logcat buffer is cleared before the stream is started.
   *
   * @param outputFile the file to stream to
   * @param outputFormat the output format of the logcat
   * @param buffer the buffer to output.
   * @return a logcat streamer
   */
  public LogcatStreamer startLogcatStream(File outputFile, Buffer buffer,
      OutputFormat outputFormat, List<LogcatFilter> logcatFilters) {
    return startLogcatStream(
        outputFile, Collections.singletonList(buffer), outputFormat, logcatFilters);
  }

  /**
   * Creates an asynchronous logcat stream.
   *
   * <p>The logcat buffer is cleared before the stream is started.
   *
   * @param outputFile the file to stream to
   * @param outputFormat the output format of the logcat
   * @param buffers the buffers to output.
   * @return a logcat streamer
   */
  public LogcatStreamer startLogcatStream(
      File outputFile,
      List<Buffer> buffers,
      OutputFormat outputFormat,
      List<LogcatFilter> logcatFilters) {
    checkNotNull(outputFile);
    checkNotNull(buffers);
    checkNotNull(outputFormat);
    checkNotNull(logcatFilters);
    if (!outputFile.getParentFile().exists()) {
      checkState(outputFile.getParentFile().mkdirs());
    }
    for (LogcatFilter filter : logcatFilters) {
      if (!"*".equals(filter.getTagName())) {
        if (filter.getLevel() == Level.DEBUG
            || filter.getLevel() == Level.VERBOSE) {
          // Log.isLoggable(tag, level) only returns true for info and
          // above. If the user wants us to run include logging for
          // a given tag at the DEBUG/VERBOSE level, lets make sure isLoggable
          // checks pass.
          setDeviceProperty(
              "log.tag." + filter.getTagName(),
              filter.getLevel().name().toUpperCase());
        }
      }
    }

    makeAdbCall("logcat", "-b", Joiner.on(',').join(buffers).toLowerCase(), "-c");
    LogcatStreamer streamer =
        new LogcatStreamer(
            prefixArgsWithDeviceSerial(),
            buffers,
            outputFormat,
            logcatFilters,
            outputFile,
            device.getEnvironmentVariablesForAdb());
    streamer.startStream();
    return streamer;
  }

  /**
   * Retrieves environmental variables from the device.
   * Strongly consider getting this information from BrokeredDevice.getDeviceShellVariables().
   */
  Map<String, String> deviceShellVariables() {
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    ShellVariableProcessor stdoutProcessor = shellVariableProcessorProvider.get();
    builder.withStdoutProcessor(stdoutProcessor);
    makeAdbCall(builder, getDefaultTimeout(), "shell", "printenv");
    return stdoutProcessor.getResult();
  }

  /**
   * Pushes a file or directory to the device.
   */
  public void push(File hostSource, String deviceDestination) {
    checkState(hostSource.exists());
    String path = hostSource.getAbsolutePath();
    if (hostSource.isFile()) {
      // Some version of adb opens file with O_NOFOLLOW FLAG.
      // That means symbolic link doesn't work, resolve it now.
      try {
        path = hostSource.getCanonicalPath();
        if (!hostSource.getName().equals(new File(path).getName())) {
          File tmp = new File(Files.createTempDir(), hostSource.getName());
          Files.copy(hostSource, tmp);
          path = tmp.getCanonicalPath();
        }
      } catch (IOException ex) {
        logger.info("IO Exception:" + ex);
        // Do nothing, we hope we happen to have a good version of adb.
        // Error could be caught by makeAdbCall.
        path = hostSource.getAbsolutePath();
      }
    }
    makeAdbCall("push", path, deviceDestination);
  }

  /**
   * Returns a list of files/directories pulled from the device into the provided host destination.
   * If hostDestination is a directory, a list of files inside the directory is returned (empty list
   * if no files exist). If hostDestination is a non-directory file, then a list with one entry (the
   * file) is returned.
   */
  public List<File> pull(String deviceSource, File hostDestination) {
    checkNotNull(deviceSource);
    checkNotNull(hostDestination);
    try {
      makeAdbCall("pull", deviceSource, hostDestination.getPath());
    } catch (IllegalStateException e) {
      // ls the path to provide debugging info in test outputs (all adb calls are logged).
      makeAdbCall("shell", "ls", deviceSource);
      throw e;
    }

    if (!hostDestination.isDirectory()) {
      // Sometimes ADB acts like the copy succeeded, but it really didn't. Check for that here to
      // make sure we never return non-existing files to callers.
      Preconditions.checkState(
          hostDestination.exists(),
          "adb pull from %s failed, destination doesn't exist: %s",
          deviceSource,
          hostDestination.getPath());
      return Lists.newArrayList(hostDestination);
    }

    File[] files = hostDestination.listFiles();
    if (files == null) {
      throw new RuntimeException(String.format(IO_ERROR_MSG, deviceSource));
    }

    // Pull behavior was changed to transfer directory as opposed to its contents.
    // android.googlesource.com/platform/system/core/+/07db1196e7bd5856b5e2ebe4ea6791d2ae8c9e76.
    // This behavior was copied by adb.turbo and AndroidGoogleTest depends on this behavior
    // in order to clean test state.
    // If we pulled a directory, then we return the contents to conform to such expectations.
    if (files.length == 1 && files[0].isDirectory()) {
      files = files[0].listFiles();
      if (files == null) {
        throw new RuntimeException(String.format(IO_ERROR_MSG, deviceSource));
      }
    }

    return Lists.newArrayList(files);
  }

  /**
   * Executes an adb script on the device.
   *
   * This interface is likely to change - it should not be relied on.
   */
  @Beta
  void executeScript(String deviceScriptPath) {
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    makeAdbCall(builder, getDefaultTimeout(), "shell", "chmod", "755", deviceScriptPath);
    builder = communicatorBuilderProvider.get();
    makeAdbCall(builder, getDefaultTimeout(), "shell", deviceScriptPath);
  }

  @VisibleForTesting
  List<String> prefixArgsWithDeviceSerial(String... args) {
    List<String> adbArgs = Lists.newArrayList(device.getAdbPath(), "-s", device.getSerialId());
    adbArgs.addAll(asList(args));
    return adbArgs;
  }

  public void deleteFiles(List<String> deviceFilePaths) {
    // ensure we delete the leafs first and work our way up the hierarchy.
    List<String> sortedPaths = Lists.newArrayList(deviceFilePaths);
    Collections.sort(sortedPaths);
    sortedPaths = Lists.reverse(sortedPaths);

    for (String file : sortedPaths) {
      try {
        makeAdbCall("shell", "rm", file);
      } catch (IllegalStateException ignored) {
        // Ignoring.
      }
    }
  }

  @Deprecated()
  // Do not use outside of this package. Not refactoring, since AdbController will go a away soon.
  public void makeAdbCall(String... adbArgs) {
    makeAdbCall(communicatorBuilderProvider.get(), getDefaultTimeout(), adbArgs);
  }

  private void makeAdbCall(SubprocessCommunicator.Builder builder, int timeout, String... adbArgs) {
    List<String> allArgs = prefixArgsWithDeviceSerial(adbArgs);
    makeCheckedCall(builder, timeout, allArgs);
  }

  private void makeCheckedCall(Builder builder, int timeout, List<String> allArgs) {
    makeCheckedCall(builder, allArgs, timeout);
  }

  private void makeCheckedCall(Builder builder, List<String> allArgs, long timeoutSeconds) {
    int exitCode = builder.withArguments(allArgs)
        .withEnvironment(device.getEnvironmentVariablesForAdb())
        .withTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build()
        .communicate();
    checkState(0 == exitCode, "Adb call failed. Exit Code: %s. Args: %s", exitCode, allArgs);
  }

  public List<String> makeLineOutputProcessorAdbCall(String... adbArgs) {
    SimpleLineListProcessor stdoutProcessor = adbLineListProvider.get();
    SimpleLineListProcessor stderrProcessor = adbLineListProvider.get();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    builder.withStdoutProcessor(stdoutProcessor);
    builder.withStderrProcessor(stderrProcessor);

    try {
      makeAdbCall(builder, getDefaultTimeout(), adbArgs);
    } catch (IllegalStateException e) {
      throw new RuntimeException(String.format("STDOUT: %s, \n STDERR: %s", Joiner.on("\n")
          .join(stdoutProcessor.getResult(), Joiner.on("\n").join(stderrProcessor.getResult())),
          e));
    }

    return stdoutProcessor.getResult();
  }

  public void adbConnect() {
    if (!isConnectedToAdb) {
      // Don't need to do any prefixing.
      makeCheckedCall(
          communicatorBuilderProvider.get(),
          getDefaultTimeout(),
          Lists.newArrayList(device.getAdbPath(), "connect", device.getSerialId()));
      isConnectedToAdb = true;
    }
  }

  /**
   * Provides the necessary context to create an AdbController.
   *
   * If you need to build an AdbController let Guice inject this factory class where you need it.
   */
  static interface AdbControllerFactory {
    public AdbController create(BrokeredDevice device);
  }

  static class FullControlAdbControllerFactory implements AdbControllerFactory {
    private final Provider<Integer> portPicker;
    private final Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider;
    private final Provider<SimpleLineListProcessor> adbLineListProvider;
    private final Provider<AdbInstrumentationListProcessor> instrumentationListProcessorProvider;
    private final Provider<ShellVariableProcessor> shellVariableProcessorProvider;
    private final Provider<AndroidPropertyProcessor> propertyProcessorProvider;
    private final RegexpProcessorBuilder regexpProcessorBuilder;
    private final TestInfoRepository testRepo;
    private final Optional<Integer> testTimeoutOverride;
    private final boolean scanTargetPackageForTests;
    private final boolean skipCoverageFilesCheck;
    private final boolean installBasicServices;
    private final List<String> assumeApksInstalled;
    private final List<String> additionalTestPackages;
    private final List<String> ignoreTestPackages;
    private final String bootstrapInstrumentationPackage;

    @Inject
    FullControlAdbControllerFactory(
        @UniquePort Provider<Integer> portPicker,
        Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider,
        Provider<SimpleLineListProcessor> adbLineListProvider,
        Provider<AdbInstrumentationListProcessor> instrumentationListProcessorProvider,
        Provider<ShellVariableProcessor> shellVariableProcessorProvider,
        Provider<AndroidPropertyProcessor> propertyProcessorProvider,
        RegexpProcessorBuilder regexpProcessorBuilder,
        TestInfoRepository testRepo,
        @TestTimeoutOverride Optional<Integer> testTimeoutOverride,
        @ScanTargetPackageForTests boolean scanTargetPackageForTests,
        @SkipCoverageFilesCheck boolean skipCoverageFilesCheck,
        @InstallBasicServices boolean installBasicServices,
        @AssumeApksInstalled List<String> assumeApksInstalled,
        @AdditionalTestPackages List<String> additionalTestPackages,
        @IgnoreTestPackages List<String> ignoreTestPackages,
        @BootstrapInstrumentationPackage String bootstrapInstrumentationPackage) {
      this.portPicker = portPicker;
      this.communicatorBuilderProvider = communicatorBuilderProvider;
      this.instrumentationListProcessorProvider = instrumentationListProcessorProvider;
      this.adbLineListProvider = adbLineListProvider;
      this.shellVariableProcessorProvider = shellVariableProcessorProvider;
      this.propertyProcessorProvider = propertyProcessorProvider;
      this.regexpProcessorBuilder = regexpProcessorBuilder;
      this.testRepo = testRepo;
      this.testTimeoutOverride = testTimeoutOverride;
      this.scanTargetPackageForTests = scanTargetPackageForTests;
      this.skipCoverageFilesCheck = skipCoverageFilesCheck;
      this.installBasicServices = installBasicServices;
      this.assumeApksInstalled = assumeApksInstalled;
      this.ignoreTestPackages = ignoreTestPackages;
      this.additionalTestPackages = additionalTestPackages;
      this.bootstrapInstrumentationPackage = bootstrapInstrumentationPackage;
    }

    @Override
    public AdbController create(BrokeredDevice device) {
      return new AdbController(
          portPicker,
          communicatorBuilderProvider,
          device,
          adbLineListProvider,
          instrumentationListProcessorProvider,
          shellVariableProcessorProvider,
          propertyProcessorProvider,
          regexpProcessorBuilder,
          testRepo,
          testTimeoutOverride,
          scanTargetPackageForTests,
          skipCoverageFilesCheck,
          installBasicServices,
          assumeApksInstalled,
          additionalTestPackages,
          ignoreTestPackages,
          bootstrapInstrumentationPackage);
    }
  }

  public TestSuitePb getTestMethodsInPaths(Instrumentation instrumentation, List<String> dexPaths) {
    return testRepo.listTestsInFiles(
        instrumentation,
        FluentIterable
            .from(dexPaths)
            .filter(new Predicate<String>() {
              @Override
              public boolean apply(String path) {
                return path.endsWith(".dex");
              }})
            .transform(new Function<String, File>() {
              @Override
              public File apply(String path) {
                return new File(path);
              }
            })
            .toList());
  }

  /**
   * Scans for tests in multiple APKs. The following flags control how we discover tests:
   *
   * <ul>
   *   <li>{@code --additional_test_packages=com.google.foo,com.google.bar}: if additional packages
   *       are specified, we search them for tests even if they aren't the main instrumentation
   *       package.
   *   <li>{@code
   *       --bootstrap_instrumentation_package=com.google.foo/.GoogleInstrumentationTestRunner}:
   *       package to look for the test runner in. Scanned for tests unless excluded using {@code
   *       --ignore_test_packages=com.google.foo}.
   *   <li>{@code --noscan_target_ackage_for_tests}: if specified, we don't look for tests in the
   *       package under test.
   * </ul>
   */
  public TestSuitePb getTestMethods(Instrumentation instrumentation) {
    checkHaveTestInfoFor(instrumentation.getAndroidPackage());

    List<String> additionalTestPackages =
        new ArrayList<>(instrumentationRepo.getAdditionalTestPackages());

    if (scanTargetPackageForTests
        && !instrumentation.getAndroidPackage().equals(instrumentation.getTargetPackage())) {
      checkHaveTestInfoFor(instrumentation.getTargetPackage());
      additionalTestPackages.add(instrumentation.getTargetPackage());
    }

    TestSuitePb suitePb = testRepo.listTests(instrumentation, additionalTestPackages);

    if (suitePb.getInfoCount() == 0) {
      System.err.println(
          "WARNING: Got 0 tests for instrumentation: " + instrumentation);
    }
    return suitePb;
  }

  private void checkHaveTestInfoFor(String packageName) {
    checkState(testRepo.containsInfoForPackage(packageName),
        "Missing information for: %s. May be caused by left-overs from other tests on same"
        + " device. Try `adb uninstall %s` to fix.",
        packageName, packageName);
  }

  /**
   * Scans through all instrumentations installed on the emulator/device to find the package
   * containing the tests to run.
   *
   * <p>Since there can be multiple instrumentations installed on the device, we skip some that are
   * known not to contain the tests, for example any package names specified in the {@code
   * ignoreTestPackages} flag. Starting with API 21
   * instrumentation:com.android.emulator.smoketests/androidx.test.runner.AndroidJUnitRunner
   * is bundled with the system image. We want to ignore that too.
   *
   * <p>An example when {@code ignoreTestPackages} should be used, is when the test runner is in a
   * different package than the tests: in such a case the test runner package is specified in {@code
   * bootstrapInstrumentationPackage} and the same package should be ignored here.
   *
   * @return Instrumentation containing the test runner
   */
  public Instrumentation getGoogleTestInstrumentation() {
    return instrumentationRepo.getTestInstrumentation();
  }

  /**
   * Runs the given instrumentation test method target with code coverage enabled. Prior to invoking
   * {@link #runTest} with coverage enabled, extracts coverage metadata and places it in the
   * provided path for offline processing at the end of the test run.
   *
   * @param coverageMetadataPath absolute path to where metadata should be extracted
   * @param onDeviceCoverageDataPath path (relative to internal use only dir) to the file where you
   *     would like coverage data to be placed
   */
  public List<ExecutedTest> runCoverage(
      Instrumentation instrumentation,
      String testMethodTarget,
      boolean enableDebug,
      HostTestSize size,
      String coverageMetadataPath,
      String onDeviceCoverageDataPath,
      boolean dumpHProfData,
      boolean withAnimation,
      Map<String, String> extraInstrumentationOptions) {
    if (coverageMetadataPath == null) {
      logger.warning(
          "JaCoCo metadata path is null. Is your --instrumentation_filter arg correct? At least "
              + "one binary in the test target, including the generated Test APK, must contain a "
              + "class covered by --instrumentation_filter for coverage data to be generated.");
    }
    checkNotNull(onDeviceCoverageDataPath);

    checkTargetIsInstrumentedForCoverage(instrumentation);

    return runTest(
        instrumentation,
        testMethodTarget,
        true /* collectCodeCoverage */,
        onDeviceCoverageDataPath,
        enableDebug,
        size,
        dumpHProfData,
        withAnimation,
        extraInstrumentationOptions);
  }

  private void checkTargetIsInstrumentedForCoverage(Instrumentation instrumentation) {
    checkState(
        GOOGLE_INSTRUMENTATION_NAMES.contains(instrumentation.getInstrumentationClass()),
        "Test coverage data can only be generated when GoogleInstrumentationTestRunner is used."
            + " Do you have an <instrumentation> entry for it in your AndroidManifest.xml?");

    String targetPackage = instrumentation.getTargetPackage();

    // Both Emma and Jacoco use this RT class as the entry point for collecting coverage data from
    // the running VM. Its absence from the APK is definitely a problem.
    String vladiumClass = "com.vladium.emma.rt.RT";
    if (skipCoverageFilesCheck) {
      logger.info(String.format(
          "Skipping check for the existence of %s and related coverage classes", vladiumClass));
      return;
    }
    if (!testRepo.targetContainsClass(targetPackage, vladiumClass)) {
      logger.warning(
          String.format(
              APK_MISSING_INSTRUMENTATION,
              targetPackage,
              vladiumClass,
              String.format(PROGUARD_KEEP_EMMA, "class")));
    }

    String jacocoAgent = "org.jacoco.agent.rt.RT";
    if (!testRepo.targetContainsClass(targetPackage, jacocoAgent)) {
      logger.warning(
          String.format(
              APK_MISSING_INSTRUMENTATION,
              targetPackage,
              jacocoAgent,
              String.format(PROGUARD_KEEP_JACOCO, "class")));
    }

    String jacocoAgentInterface = "org.jacoco.agent.rt.IAgent";
    if (!testRepo.targetContainsClass(targetPackage, jacocoAgentInterface)) {
      logger.warning(
          String.format(
              APK_MISSING_INSTRUMENTATION,
              targetPackage,
              jacocoAgentInterface,
              String.format(PROGUARD_KEEP_JACOCO, "interface")));
    }
  }

  /**
   * Runs dumpstate on the device and writes it to the given stream.
   *
   * @param outStream stream to write dumpstate output to.
   */
  public void dumpstate(OutputStream outStream) {
    checkNotNull(outStream);
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    StreamWritingProcessor processor = new StreamWritingProcessor(outStream);
    builder.withStdoutProcessor(processor);
    makeAdbCall(builder, getDefaultTimeout(), "shell", "dumpstate");
    processor.getResult(); // closes the output stream.
  }

  /**
   * Restart logd service on device.
   */
  public void restartLogd() {
    makeAdbCall("shell", "stop", "logd");
    makeAdbCall("shell", "start", "logd");
  }

  /**
   * Runs the given test method target from the given instrumentation.
   *
   * @param enableDebug controls whether debug flag is passed to the InstrumentationTestRunner
   * @param size passed to InstrumentationTestRunner (used for capturing state prior to timeout)
   * @return the resulting InstrumentationTestRunner output.
   */
  public List<ExecutedTest> runTest(
      Instrumentation instrumentation,
      String testMethodTarget,
      boolean enableDebug,
      HostTestSize size,
      boolean dumpHProfData,
      boolean withAnimation,
      Map<String, String> extraInstrumentationOptions) {
    return runTest(
        instrumentation,
        testMethodTarget,
        false /* collectCodeCoverage */,
        null /* coverageDataPath */,
        enableDebug,
        size,
        dumpHProfData,
        withAnimation,
        extraInstrumentationOptions);
  }

  private String getClassPathForTestServices() {
    if (device.getServicesPath() != null) {
      return device.getServicesPath();
    }

    String apkLocation;
    try {
      apkLocation = getApkPathForInstalledApp(ANDROID_TEST_SERVICES_PACKAGE);
    } catch (RuntimeException e) {
      // Typically caused when pm doesn't return on time, so we call once again.
      apkLocation = getApkPathForInstalledApp(ANDROID_TEST_SERVICES_PACKAGE);
    }
    device.setServicesPath(apkLocation);
    return apkLocation;
  }

  private List<ExecutedTest> runTest(
      Instrumentation instrumentation,
      String testMethodTarget,
      boolean collectCodeCoverage,
      @Nullable String coverageDataPath,
      boolean enableDebug,
      HostTestSize size,
      boolean dumpHProfData,
      boolean withAnimation,
      Map<String, String> extraInstrumentationOptions) {

    List<String> adbArgs = Lists.newArrayList();

    if (installBasicServices) {
      // We exec the instrumentation through a wrapper launcher defined in basic_services.apk to
      // allow test code to execute shell commands with root|shell user privileges.
      adbArgs.add("CLASSPATH=" + getClassPathForTestServices());
      adbArgs.add("SM_EXIT=1");
      adbArgs.add("app_process / androidx.test.services.shellexecutor.ShellMain");
    }

    adbArgs.add("am");
    adbArgs.add("instrument");

    if (collectCodeCoverage) {
      adbArgs.addAll(Lists.newArrayList("-e", "coverage", "true"));
      adbArgs.addAll(Lists.newArrayList(
          "-e", "coverageDataPath", coverageDataPath));
    }
    if (enableDebug) {
      adbArgs.addAll(Lists.newArrayList("-e", "debug", "true"));
    }

    if (dumpHProfData) {
      adbArgs.addAll(Lists.newArrayList("-e", "hprofDataFile", "hprof.dump"));
    }

    for (String key : extraInstrumentationOptions.keySet()) {
      adbArgs.add("-e");
      adbArgs.add(ShellUtils.shellEscape(key));
      adbArgs.add(ShellUtils.shellEscape(extraInstrumentationOptions.get(key)));
    }

    if (!withAnimation) {
      if (device.getApiVersion() >= 10) {
        adbArgs.add("--no_window_animation");
      } // not supported for less then gingerbread.
    }
    long testTimeout = size.getTestTimeout(TimeUnit.SECONDS);
    if (testTimeoutOverride.isPresent()) {
      testTimeout = testTimeoutOverride.get();
    }

    adbArgs.addAll(
        Lists.newArrayList(
            "-e", "testTimeoutSeconds", String.valueOf(testTimeout)));

    boolean runTestsThroughOrchestrator = installBasicServices
        && ORCHESTRATOR_ENABLED_RUNNERS.contains(instrumentation.getInstrumentationClass());


    if (runTestsThroughOrchestrator) {
      adbArgs.addAll(
          Lists.newArrayList(
              "-r",
              "-w",
              "-e",
              "class",
              ShellUtils.shellEscape(testMethodTarget),
              "-e",
              "targetInstrumentation",
              instrumentation.getFullName(),
              ORCHESTRATOR_COMPONENT_NAME));
    } else {
      adbArgs.addAll(
          Lists.newArrayList(
              "-r",
              "-w",
              "-e",
              "class",
              ShellUtils.shellEscape(testMethodTarget),
              instrumentation.getFullName()));
    }

    if (installBasicServices) {
      // adb shell commands _may_ return their exit code from the device - if they are run on the
      // right system image and the host machine has the right version of adb installed
      // otherwise they wont. SM_EXIT kills itself at the end, so we do not want that
      // exit code bubbling up.
      adbArgs.add("||");
      adbArgs.add("true");
    }

    InstrumentationTestRunnerProcessor stdoutProcessor =
        new InstrumentationTestRunnerProcessor(new EventBus());
    SimpleLineListProcessor stderrProcessor = new SimpleLineListProcessor();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get();
    builder
        .withStdoutProcessor(stdoutProcessor)
        .withStderrProcessor(stderrProcessor);
    String shellArgs = Joiner.on(" ").join(adbArgs);

    List<String> partialArgs = Lists.newArrayList("shell", shellArgs);

    try {
      makeCheckedCall(builder,
          prefixArgsWithDeviceSerial(partialArgs.toArray(new String[partialArgs.size()])),
          testTimeout);
    } catch (IllegalStateException e) {
      StringBuilder allLines = new StringBuilder();
      for (ExecutedTest executedTest : stdoutProcessor.getResult()) {
        allLines.append(executedTest.getAllLines());
      }

      throw new RuntimeException(String.format(
          "Error when executing adb.\n STDOUT: %s\n STDERR: %s",
          allLines,
          Joiner.on("\n").join(stderrProcessor.getResult())), e);
    }

    return stdoutProcessor.getResult();
  }

  private boolean isApkAlreadyInstalled(String apkPath, String appPackageName) throws IOException {
    checkNotNull(apkPath);
    checkNotNull(appPackageName);

    String installedApkPath = getApkPathForInstalledApp(appPackageName);

    if (installedApkPath == null || installedApkPath.equals("")) {
      return false;
    }

    return apkHashMatchesInstalledApk(apkPath, installedApkPath);
  }

  private String getApkPathForInstalledApp(String appPackageName) {
    LineProcessor<String> lineProcessor = regexpProcessorBuilder
        .withPattern(PATH_PATTERN).buildFirstMatchProcessor();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get()
        .withStdoutProcessor(lineProcessor)
        .withStderrProcessor(lineProcessor);

    makeAdbCall(
        builder, getShortDefaultTimeout(), "shell", "pm", "path", appPackageName, "||", "true");
    return lineProcessor.getResult();
  }

  private boolean apkHashMatchesInstalledApk(String apkPath, String installedApkPath)
      throws IOException {
    String apkHash = Files.asByteSource(new File(apkPath)).hash(Hashing.md5()).toString();
    Pattern hashPattern = Pattern.compile(String.format("^%s .*", apkHash));

    LineProcessor<Boolean> md5Processor = regexpProcessorBuilder
        .withPattern(hashPattern).buildRegexpPresentProcessor();
    SubprocessCommunicator.Builder builder = communicatorBuilderProvider.get()
        .withStdoutProcessor(md5Processor)
        .withStderrProcessor(md5Processor);

    makeAdbCall(
        builder,
        getDefaultTimeout(),
        "shell",
        "md5",
        installedApkPath,
        "||",
        "md5sum",
        installedApkPath);

    return md5Processor.getResult();
  }
}
