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
import static java.util.Arrays.asList;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.AaptPath;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;

/** Provides functionality available through the aapt tool. */
class AaptUtil {

  private static final int MAX_TRIES = 3;
  private static final Logger logger = Logger.getLogger(AaptUtil.class.getName());

  private final Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider;
  private final Provider<AaptDumpPackageLineProcessor> aaptDumpProcessorProvider;
  private final String aaptPath;

  @Inject
  AaptUtil(Provider<SubprocessCommunicator.Builder> communicatorBuilderProvider,
      Provider<AaptDumpPackageLineProcessor> aaptDumpProcessorProvider, @AaptPath String aaptPath) {
    this.communicatorBuilderProvider = checkNotNull(communicatorBuilderProvider);
    this.aaptDumpProcessorProvider = checkNotNull(aaptDumpProcessorProvider);
    this.aaptPath = checkNotNull(aaptPath);
    checkArgument(new File(aaptPath).exists(), "aapt not present at: %s", aaptPath);
  }

  /**
   * Returns the package name of the provided apk.
   */
  public String getPackageNameFromApk(String apkPath) {
    checkNotNull(apkPath);
    checkArgument(new File(apkPath).exists(), "Apk ain't there: %s", apkPath);
    AaptDumpPackageLineProcessor stdoutProcessor = aaptDumpProcessorProvider.get();

    String packageName = null;
    for (int i = 0; i < MAX_TRIES; i++) {
      SubprocessCommunicator.Builder subComBuilder =
          communicatorBuilderProvider.get().withStdoutProcessor(stdoutProcessor);
      makeUncheckedAaptCall(subComBuilder, "dump", "badging", apkPath);
      packageName = stdoutProcessor.getResult();
      if (null != packageName) {
        return packageName;
      }
    }

    throw new RuntimeException("aapt output did not contain the package name.");
  }

  private void makeUncheckedAaptCall(SubprocessCommunicator.Builder subComBuilder, String... args) {
    List<String> aaptArgs = Lists.newArrayList(aaptPath);
    aaptArgs.addAll(asList(args));
    int exitCode = subComBuilder.withArguments(aaptArgs).build().communicate();
    if (exitCode != 0) {
      logger.warning(String.format("Aapt call failed. Exit code: %s. Args: %s", exitCode,
          Arrays.toString(args)));
    }
  }
}
