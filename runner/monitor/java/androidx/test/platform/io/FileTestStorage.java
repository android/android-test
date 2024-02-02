/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.platform.io;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that reads/writes the runner data using the raw file system.
 *
 * <p>This API is experimental and is subject to change or removal in future releases.
 */
@ExperimentalTestApi
public final class FileTestStorage implements PlatformTestStorage {

  private static final String TAG = FileTestStorage.class.getSimpleName();
  private final OutputDirCalculator outputDirCalculator;

  public FileTestStorage() {
    outputDirCalculator = new OutputDirCalculator();
  }

  /**
   * Provides an InputStream to a test file dependency.
   *
   * @param pathname path to the test file dependency. Should not be null. Can be either a relative
   *     or absolute path. If relative, the implementation will read the input file from the test
   *     apk's asset directory
   */
  @Override
  public InputStream openInputFile(String pathname) throws IOException {
    File inputFile = new File(pathname);
    if (inputFile.isAbsolute()) {
      return new FileInputStream(inputFile);
    }
    return InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(pathname);
  }

  /**
   * Provides an OutputStream to a test output file.
   *
   * @param pathname path to the test file dependency. Should not be null. Can be either a relative
   *     or absolute path. If relative, the implementation will make a best effort attempt to a
   *     writable output dir based on API level.
   */
  @Override
  public OutputStream openOutputFile(String pathname) throws IOException {
    return openOutputFile(pathname, false);
  }

  @Override
  public OutputStream openOutputFile(String pathname, boolean append) throws IOException {
    File outputFile = new File(pathname);
    if (!outputFile.isAbsolute()) {
      outputFile = new File(outputDirCalculator.getOutputDir(), pathname);
    }
    Log.d("FileTestStorage", "openOutputFile from " + outputFile.getAbsolutePath());
    return new FileOutputStream(outputFile, append);
  }

  /** Implementation of input arguments that reads from InstrumentationRegistry.getArguments */
  @Override
  public String getInputArg(String argName) {
    return InstrumentationRegistry.getArguments().getString(argName);
  }

  /** Implementation of input arguments that reads from InstrumentationRegistry.getArguments */
  @Override
  public Map<String, String> getInputArgs() {
    Map<String, String> argMap = new HashMap<>();
    Bundle bundle = InstrumentationRegistry.getArguments();
    for (String key : bundle.keySet()) {
      argMap.put(key, bundle.getString(key));
    }
    return argMap;
  }

  /** Test output properties is not supported when raw file I/O is used. */
  @Override
  public void addOutputProperties(Map<String, Serializable> properties) {
    Log.w(TAG, "Output properties is not supported.");
  }

  /**
   * Test output properties is not supported when raw file I/O is used.
   *
   * <p>An empty map is always returned.
   */
  @Override
  public Map<String, Serializable> getOutputProperties() {
    Log.w(TAG, "Output properties is not supported.");
    return Collections.emptyMap();
  }

  /**
   * Provides an InputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the internal input file. Should not be null. This is an absolute file
   *     path on the device, and it's the infrastructure/client's responsibility to make sure the
   *     file path is readable.
   */
  @Override
  public InputStream openInternalInputFile(String pathname) throws IOException {
    return openInputFile(pathname);
  }

  /**
   * Provides an OutputStream to an internal file used by the testing infrastructure.
   *
   * @param pathname path to the test file dependency. Should not be null. Can be either a relative
   *     or absolute path. If relative, the implementation will read the input file from the test
   *     apk's asset directory
   */
  @Override
  public OutputStream openInternalOutputFile(String pathname) throws IOException {
    return openOutputFile(pathname);
  }

  @Override
  public boolean isTestStorageFilePath(@NonNull String pathname) {
    String outputDir = outputDirCalculator.getOutputDir().getAbsolutePath();
    return pathname.startsWith(outputDir);
  }
}
